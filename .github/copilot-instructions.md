# Copilot / AI Agent Instructions for AutomationLCE Plugin

This file contains concise, actionable guidance to help an AI coding agent be productive in this repository.

1) Big picture
- Purpose: a SailPoint IdentityIQ plugin exposing REST endpoints to process lifecycle events (JOINER/MOVER/LEAVER).
- Key components:
  - REST API surface: `src/com/eshiam/lifecycle/rest/LifecycleResource.java` (base path `/AutomationLCE`).
  - Business logic: `src/com/eshiam/lifecycle/utils/LifecycleUtils.java` (mapping, validation, batch processing, rule/workflow invocation).
  - Models: `src/com/eshiam/lifecycle/model/*` (e.g. `LifecycleInput`, `Access`, `ApplicationAccess`, `BatchResponse`).
  - Packaging/build: `build.xml` + `build.properties` (Ant-based; target `package` compiles against IIQ libs in `iiq.home`).

2) Important patterns & conventions (project-specific)
- Endpoints accept raw `Map<String,Object>` not typed POJOs to avoid IIQ classloader/serialization issues; conversion happens via `LifecycleUtils.mapToLifecycleInput()`.
- Simulation mode: when `SailPointContext` is null (unit tests / non-IIQ runtime), `LifecycleUtils` returns simulated results (`SIMULATED`) so tests and local debugging work without IIQ.
- In-memory status store: `LifecycleUtils` uses `ConcurrentMap` (`lastRunInfo`, `requestResults`) for request lookup and last-run info — ephemeral across restarts.
- Batch parsing: `LifecycleUtils.processBatch()` supports three forms per item: Map, JSON string, and IIQ flattened `key=value` string; parsing attempts JSON first then a heuristic transform.
- Rule/workflow selection: event-specific settings keys used — `joinerRule`, `joinerWorkflow`, `moverRule`, `moverWorkflow`, `leaverRule`, `leaverWorkflow`.
- Result normalization: rule return values may be Map or simple types; `normalizeResultToMap()` preserves simple results inside `{"result": ...}`.

3) Build / run / debug
- Primary build: ensure `build.properties` has correct `iiq.home` and `jdk.home.17`, then run:

  ant -f build.xml clean package

- Notes:
  - Ant `javac` target is Java 17 (`target="17"`).
  - Classpath uses `${iiq.home}/WEB-INF/classes` and `${iiq.home}/WEB-INF/lib/*.jar` — set `iiq.home` to your IdentityIQ webapp location.
  - Plugin name and version are defined in `build.properties` (`pluginName`, `version`).

4) Files to inspect for common changes
- Endpoint changes / new APIs: edit `src/com/eshiam/lifecycle/rest/LifecycleResource.java`.
- Validation / mapping / batch behavior: edit `src/com/eshiam/lifecycle/utils/LifecycleUtils.java` — most logic lives here.
- Add/adjust models: see `src/com/eshiam/lifecycle/model/` (e.g. `Access.java`, `ApplicationAccess.java`).
- Packaging/CI: `build.xml` and `build.properties`.
- Design & examples: `README.md` (contains payload examples, endpoints and behaviour details).

5) Integration points & external dependencies
- Runs rules and workflows via `SailPointContext` (`context.getObjectByName(Rule.class, name)`, `context.runRule(rule, args)`).
- Relies on IdentityIQ jars at compile/runtime. Local development may simulate via null `SailPointContext`.
- Gson is used for JSON parsing in batch processing (`com.google.gson.Gson`).

6) Agent coding tips (concrete examples)
- When adding a REST endpoint, accept `Map<String,Object>` and convert with `LifecycleUtils.mapToLifecycleInput()` to follow existing pattern.
- Preserve simulation behavior: if changing rule execution, keep the `context==null` branch so unit tests continue to run without IIQ.
- For batch-related fixes, add test vectors that cover: Map entries, JSON-string entries, and IIQ flattened strings (see `README.md` examples).
- Update `README.md` when changing public endpoint shapes or config key names — tests and external callers rely on examples there.

7) What to ask the user/maintainer
- Confirm `iiq.home` path used for CI or provide sample test container for integration tests.
- Confirm desired persistence strategy if in-memory `requestResults` should be persisted between restarts.

If anything here is unclear or you want additional examples (unit tests, CI snippets, or a small integration harness), tell me which area to expand. 
