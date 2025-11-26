
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
  }' \
  http://<host>/identityiq/rest/AutomationLCE/joiner/batch
```

---

## Troubleshooting

- If you encounter classloader or type mismatch errors, use Map-based endpoints and call `LifecycleUtils.mapToLifecycleInput` for conversion.
- Use the `/AutomationLCE/debug` endpoint to inspect the actual bound Java type and classloader for incoming JSON.

---

## Contact & Support

For questions, issues, or feature requests, please contact the plugin maintainer or open a GitHub issue.
