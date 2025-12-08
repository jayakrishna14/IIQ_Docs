# Automation LCE (Lifecycle Events) Plugin — Design & Testing Guide

## Overview
This plugin centralizes SailPoint IdentityIQ lifecycle change handling (JOINER/MOVER/LEAVER) and provides robust REST endpoints to run rules and workflows, both individually and in batch. It also provides in-memory status and request results tracking, and allows invoking rules via REST for automation actions.

This README is both a design document for maintainers and a user guide for testers.

---

## Architecture & Key Components
`LifecycleResource` (REST Resource): Exposes endpoints for single events, batch events, debug, status queries, and rule execution via REST.
- `LifecycleUtils`: Centralized helper that maps input payloads to `LifecycleInput`, validates, processes batches, and executes lifecycle rules and workflows.
Note: The plugin stores status and request lookups in-memory by default. If you need persistence across restarts, implement a simple provider that adheres to plugin data needs.
- `BatchResponse`, `BatchResult`, `BatchStatus`, `ErrorCode`, `LifecycleInput` models.

The plugin avoids typed POJO deserialization in REST endpoints to prevent classloader mismatches (use `Map<String,Object>` in endpoints and convert with `LifecycleUtils.mapToLifecycleInput`).

---

## Endpoints
Base path: `/AutomationLCE`

- `POST /AutomationLCE/joiner` — Single joiner event. Accepts either a `lceInput` wrapper or direct fields. Returns a JSON with `status`, `result`, and meta fields.
- `POST /AutomationLCE/joiner/batch` — Batch of identities. Request body: `{ "identities": [<Map>|<String>], ... }` where items can be a Map or stringified JSON or IIQ-style `key=value` format.
- Like above, `POST /AutomationLCE/mover`, `/AutomationLCE/mover/batch`, `/AutomationLCE/leaver`, `/AutomationLCE/leaver/batch`.
- `GET /AutomationLCE/status` — Returns last run info for each event (JOINER/MOVER/LEAVER) and plugin config.
- `GET /AutomationLCE/{event}/status` — Returns last run status for the specified event.
- `GET /AutomationLCE/status/request/{requestId}` — Lookup a request result by requestId.
-- `POST /AutomationLCE/status/runRule` — Execute a named rule for a given `requestId`. Accepts `taskName`, `taskArgs` (map); `requestId` will be added to args and passed to the rule.

---

## Config Keys (Plugin Settings)
These are expected to be plugin settings in the IIQ plugin configuration.

- `joinerRule` — name of the rule used by the joiners.
- `joinerWorkflow` — name of the workflow for joiners.
- `moverRule`, `moverWorkflow`, `leaverRule`, `leaverWorkflow` — similar for mover/leaver.
Note: The plugin stores request results and last run information in memory by default.

---

## Sample Request Payloads

1) Single JOINER request
```
POST /AutomationLCE/joiner
Content-Type: application/json

{
  "lceInput": {
    "eventType": "JOINER",
    "identityName": "jsmith",
    "firstName": "John",
    "lastName": "Smith",
    "email": "john.smith@example.com",
    "applications": [
      {
        "name": "app1",
        "operation": "ADD",
        "access": { "add": ["ROLE_A", "ROLE_B"] }
      }
    ]
  }
}
```

2) Batch request with maps
```
POST /AutomationLCE/joiner/batch
Content-Type: application/json

{
  "identities": [
    {"identityName":"jsmith", "eventType":"JOINER", "email":"jsmith@example.com"},
    {"identityName":"adoe", "eventType":"LEAVER", "email":"adoe@example.com"}
  ]
}
```

3) Batch request with stringified entries (valid; plugin will attempt to parse)
```
{
  "identities": [
    "{\"identityName\":\"jsmith\", \"eventType\":\"JOINER\", \"email\":\"jsmith@example.com\"}",
    "{identityName=adoe, eventType=LEAVER, email=adoe@example.com}"
  ]
}
```

4) Run a Task (runSync)
```
POST /AutomationLCE/status/runRule
Content-Type: application/json

{
  "requestId": "62f8bdf7577145ccbdcbf466ea904b6b",
  "taskName": "ProcessIdentityTaskRule",
  "taskArgs": {"param":"value"}
}
```

5) Run a Task (runNow)
```
{
  "taskName": "Identity Refresh",
  "arguments": {"param":"value"},
  "runMode": "runNow"
}
```

---

Rule Execution: Examples & Notes
The preferred flow is to call a rule directly via the `status/runRule` endpoint. This endpoint will call the specified rule with the provided arguments, passing the requestId and initiator by default.

Supported payload example to call a rule:

```
POST /AutomationLCE/status/runRule
Content-Type: application/json

{
  "requestId": "62f8bdf7577145ccbdcbf466ea904b6b",
  "taskName": "ProcessIdentityTaskRule",
  "taskArgs": {"appName": "app1", "event": "true", "users": ["name1","name2"]}
}
```

When the rule is called, the plugin will add `requestId` and `initiator` into the args before invoking the rule.

---

## Persistence
The plugin stores request results and last run information in-memory. If you need persistence across plugin restarts you should implement a custom persistence provider (e.g. file-based, database or external store) and adapt `LifecycleUtils` to use it.

---

## Debugging & Testing
- When `SailPointContext` is not available (unit test environment), the plugin simulates rule execution (`result: SIMULATED`) so that tests and local runs can verify behavior.
- Use the `GET /AutomationLCE/status` and `GET /AutomationLCE/status/request/{requestId}` APIs to troubleshoot requests and batch runs.
  - The request lookup stores per-request `requestId` entries when creating pending entries — enabling immediate lookup; results are updated once execution completes.

---

## Community & Resources
 - SailPoint Community: https://community.sailpoint.com/
 - IdentityIQ Wiki & docs: https://community.sailpoint.com/t5/IdentityIQ-Wiki/ct-p/IdentityIQWiki
 - Example of running tasks from a rule or workflow: see the `status/runRule` endpoint examples above.

---

## Notes
By default the plugin uses `in-memory` persistence. There is no built-in JDBC or DB-backed persistence in this release.
- For environments with strict classpath limits, please ensure JDBC drivers and IdentityIQ jars are in the container classpath. The plugin gracefully falls back to simulation if IIQ classes or a DB driver are missing at runtime.

---

Maintainers: Jayakrishna

# AutomationLCE Plugin — REST API Documentation

>This plugin exposes robust REST endpoints for SailPoint lifecycle events (JOINER, MOVER, LEAVER), supporting both single and batch operations with strong validation, error codes, and explicit provider registration.

---

## Table of Contents
1. [Overview](#overview)
2. [Endpoints](#endpoints)
3. [Request Payloads](#request-payloads)
4. [Response Formats](#response-formats)
5. [Model Structure](#model-structure)
6. [Error Codes & Statuses](#error-codes--statuses)
7. [Usage Notes](#usage-notes)

---

## Overview

AutomationLCE provides REST APIs for lifecycle event automation in SailPoint IdentityIQ. It supports:
- Single event processing (JOINER, MOVER, LEAVER)
- Batch event processing (multiple identities)
- Strong input validation and standardized error codes
- Explicit provider registration for plugin reliability

---

## Endpoints

### Single Event Endpoints
- `POST /AutomationLCE/joiner` — Process a single JOINER event
- `POST /AutomationLCE/mover` — Process a single MOVER event
- `POST /AutomationLCE/leaver` — Process a single LEAVER event

**Request Body:** JSON object representing a single identity (see [LifecycleInput](#model-structure))

### Batch Event Endpoints
- `POST /AutomationLCE/joiner/batch` — Process multiple JOINER events
- `POST /AutomationLCE/mover/batch` — Process multiple MOVER events
- `POST /AutomationLCE/leaver/batch` — Process multiple LEAVER events

### Status & Request Lookup Endpoints
- `GET /AutomationLCE/status` — Returns the last-run summary for each event type and plugin rule/workflow configuration
- `GET /AutomationLCE/{event}/status` — Returns the last-run entry for a particular event: `JOINER`, `MOVER`, or `LEAVER`
- `GET /AutomationLCE/status/request/{requestId}` — Return the status/result for a specific item in a batch or a single execution (searchable by the requestId returned in responses)

**Request Body:** JSON object with an `identities` array (see [Batch Payload](#request-payloads))

---

## Request Payloads

### Single Event Example
```json
{
  "eventType": "JOINER",
  "identityName": "jsmith",
  "firstName": "John",
  "lastName": "Smith",
  "department": "IT",
  "email": "john.smith@example.com",
  "applications": [
    {
      "name": "Active Directory",
      "operation": "ADD",
      "access": {
        "add": ["AD-Standard"],
        "remove": []
      }
    }
  ]
}
```

### Batch Event Example
```json
{
  "identities": [
    {
      "eventType": "JOINER",
      "identityName": "jsmith",
      "firstName": "John",
      "lastName": "Smith",
      "email": "john.smith@example.com"
    },
    {
      "eventType": "MOVER",
      "identityName": "adoe",
      "firstName": "Alice",
      "lastName": "Doe",
      "email": "alice.doe@example.com"
    }
  ]
}
```

---

## Response Formats

### Single Event Success
```json
{
  "status": "SUCCESS",
  "identityName": "jsmith",
  "eventType": "JOINER",
  "requestId": "c1a2b3d4-...",
  "result": {
    "message": "Rule executed successfully",
    "details": { /* rule output */ }
  }
}
```

### Single Event Validation Error
```json
{
  "status": "FAILED_VALIDATION",
  "errors": [
    "Missing identityName",
    "Invalid email format"
  ],
  "errorCode": "VALIDATION_ERROR"
}
```

### Batch Event Success
```json
{
  "status": "BATCH_COMPLETE",
  "results": [
    {
      "identityName": "jsmith",
      "eventType": "JOINER",
      "status": "SUCCESS",
      "requestId": "c1a2b3d4-...",
      "result": { "message": "Rule executed successfully" }
    },
    {
      "identityName": "adoe",
      "eventType": "MOVER",
      "status": "FAILED_VALIDATION",
      "errors": ["Missing department"],
      "errorCode": "VALIDATION_ERROR"
    }
  ]
}

---

## Result Shape & Request Lookup

- Batch results preserve the rule return type. If a rule returns a String then the `result` field will be a JSON string: `"result": "Good"`. If the rule returns a Map/object, the `result` will be returned as a JSON object accordingly.
- Each processed item receives a `requestId` — you can query the status of that request via `GET /AutomationLCE/status/request/{requestId}`.
- While a batch item is being processed an entry is created with a `PENDING` status for the `requestId`. The plugin stores request results in an in-memory map (ephemeral). This is a best-effort store — it resets on plugin/instance restart.

### Example: Querying a request
Request example:
```sh
GET http://<host>/identityiq/rest/AutomationLCE/status/request/79836cd9ea914b43ae79d6a18f11b236
```
Response example:
```json
{
  "identityName": "mclark",
  "eventType": "MOVER",
  "status": "SUCCESS",
  "requestId": "79836cd9ea914b43ae79d6a18f11b236",
  "rule": "LCE_Mover_Rule",
  "workflow": "LCE_Mover_Workflow",
  "result": "Good",
  "errors": null,
  "errorCode": "NONE"
}
```

---

## Task Execution Endpoint (Best Effort)

The plugin exposes a best-effort task execution endpoint which delegates to the underlying SailPoint `SailPointContext` using reflection. This is primarily intended for developer convenience and small automation tasks:

-- `POST /AutomationLCE/status/runRule` — execute a named IIQ rule for a specific request

Request Body example:
```json
{
  "taskName": "MyTask",
  "arguments": {
    "param": "value"
  }
}
```

Response example (simulated in non-IIQ test environment):
```json
{
  "status": "SIMULATED",
  "taskName": "MyTask",
  "message": "No SailPointContext available in this environment; simulation only.",
  "arguments": { "param": "value" }
}
```

Notes about task execution:
`status/runRule` invokes a rule directly. When `SailPointContext` is not available in test environments, rule calls will fail gracefully in the REST response.
- For production use, prefer the documented task APIs or a custom runTask implementation targeting your environment's expected method signatures.
```

---

## Model Structure

### LifecycleInput
| Field         | Type     | Description                       |
|---------------|----------|-----------------------------------|
| eventType     | String   | JOINER, MOVER, or LEAVER          |
| identityName  | String   | Unique identity name              |
| firstName     | String   | First name                        |
| lastName      | String   | Last name                         |
| department    | String   | Department (optional)             |
| email         | String   | Email address                     |
| applications  | Array    | List of ApplicationAccess objects |

### ApplicationAccess
| Field     | Type   | Description                  |
|-----------|--------|------------------------------|
| name      | String | Application name             |
| operation | String | ADD/REMOVE                   |
| access    | Object | Access object (see below)    |

### Access
| Field  | Type   | Description                |
|--------|--------|----------------------------|
| add    | Array  | List of accesses to add    |
| remove | Array  | List of accesses to remove |

### BatchResponse
| Field   | Type   | Description                          |
|---------|--------|--------------------------------------|
| status  | String | BATCH_COMPLETE or error status       |
| results | Array  | Array of BatchResult objects         |

### BatchResult
| Field      | Type   | Description                          |
|------------|--------|--------------------------------------|
| identityName| String | Identity processed                   |
| eventType  | String | Event type (JOINER, MOVER, LEAVER)  |
| status     | Enum   | SUCCESS, FAILED_VALIDATION, FAILED  |
| requestId  | String | Unique request ID                    |
| result     | Object | Rule execution result (on success)   |
| errors     | Array  | Validation or execution errors       |
| errorCode  | Enum   | See below                            |

---

## Error Codes & Statuses

### ErrorCode Enum
- NONE
- VALIDATION_ERROR
- RULE_NOT_FOUND
- WORKFLOW_NOT_FOUND
- EXECUTION_ERROR

### BatchStatus Enum
- SUCCESS
- FAILED_VALIDATION
- FAILED

---

## Usage Notes

- All endpoints consume and produce `application/json`.
- For batch endpoints, each identity is validated and processed independently.
- Error codes and validation errors are returned per identity in batch responses.
- Provider registration is handled explicitly for plugin reliability.
- Use the `/AutomationLCE/debug` endpoint to inspect incoming payload binding (for troubleshooting classloader issues).

---

## Example API Calls (curl)

### Single Joiner
```sh
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "JOINER",
    "identityName": "jsmith",
    "firstName": "John",
    "lastName": "Smith",
    "email": "john.smith@example.com"
  }' \
  http://<host>/identityiq/rest/AutomationLCE/joiner
```

### Batch Joiner
```sh
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "identities": [
      { "eventType": "JOINER", "identityName": "jsmith", "email": "john.smith@example.com" },
      { "eventType": "JOINER", "identityName": "adoe", "email": "alice.doe@example.com" }
    ]
4) Status lookup by single or multiple requestIds
```
POST /AutomationLCE/status
Content-Type: application/json

// Single
{
  "requestId": "62f8bdf7577145ccbdcbf466ea904b6b"
}

// Multiple
{
  "requestId": ["62f8bdf7577145ccbdcbf466ea904b6b", "abcd1234"]
}
```
  }' \
  http://<host>/identityiq/rest/AutomationLCE/joiner/batch
```

---

## Troubleshooting

- If you encounter classloader or type mismatch errors, use Map-based endpoints and call `LifecycleUtils.mapToLifecycleInput` for conversion.
- Use the `/AutomationLCE/debug` endpoint to inspect the actual bound Java type and classloader for incoming JSON.

5) Run a Rule for a request (status/runRule)
```
POST /AutomationLCE/status/runRule
Content-Type: application/json

{
  "requestId": "62f8bdf7577145ccbdcbf466ea904b6b",
  "taskName": "ProcessIdentityTaskRule",
  "taskArgs": {"appName":"app1", "event":"true", "users":["name1","name2"]}
}
```
