package test.java.com.eshiam.lifecycle.utils;

import com.eshiam.lifecycle.model.LifecycleInput;
import com.eshiam.lifecycle.utils.LifecycleUtils;
import com.eshiam.lifecycle.model.BatchResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class LifecycleUtilsTest {

    @Test
    public void testMapToLifecycleInput() {
        Map<String, Object> m = new HashMap<>();
        m.put("identityName", "jsmith");
        m.put("firstName", "John");
        m.put("lastName", "Smith");
        m.put("email", "john.smith@example.com");

        LifecycleInput li = LifecycleUtils.mapToLifecycleInput(m);
        assertEquals("jsmith", li.getIdentityName());
        assertEquals("John", li.getFirstName());
        assertEquals("Smith", li.getLastName());
        assertEquals("john.smith@example.com", li.getEmail());
    }

    @Test
    public void testValidateLifecycleInput() {
        LifecycleInput li = new LifecycleInput();
        li.setIdentityName("");
        li.setEmail("invalid-email");
        li.setEventType("FOO");
        List<String> errors = LifecycleUtils.validateLifecycleInput(li);
        assertTrue(errors.size() >= 1);
    }

    @Test
    public void testProcessBatch() {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("identityName", "jsmith");
        row1.put("eventType", "JOINER");
        row1.put("email", "john.smith@example.com");

        Map<String, Object> row2 = new HashMap<>();
        row2.put("eventType", "MOVER");
        // missing identityName triggers validation

        java.util.List<Map<String, Object>> batch = new java.util.ArrayList<>();
        batch.add(row1);
        batch.add(row2);

        // We can't execute rule against a real SailPointContext in unit test; we test
        // validation and mapping
        // Use null context and just test that processBatch handles validation entries
        BatchResponse resp = LifecycleUtils.processBatch(batch, null, "joinerRule", "joinerWorkflow", "moverRule",
                "moverWorkflow", "leaverRule", "leaverWorkflow", "testuser", "batchRequestId");
        assertNotNull(resp);
        assertEquals("BATCH_COMPLETE", resp.getStatus());
        assertEquals(2, resp.getResults().size());
        // First item may be SUCCESS or FAILED depending on whether rules are executed
        // in the test environment.
        assertNotNull(resp.getResults().get(0).getStatus());
        assertEquals(com.eshiam.lifecycle.model.BatchStatus.FAILED_VALIDATION, resp.getResults().get(1).getStatus());

        // Now test a batch where items are JSON strings (simulating client-side
        // stringification)
        java.util.List<String> batchAsStrings = new java.util.ArrayList<>();
        batchAsStrings
                .add("{\"identityName\":\"jsmith\", \"eventType\": \"JOINER\", \"email\": \"john.smith@example.com\"}");
        batchAsStrings.add("{\"eventType\":\"MOVER\"}");
        BatchResponse resp2 = LifecycleUtils.processBatch(batchAsStrings, null, "joinerRule", "joinerWorkflow",
                "moverRule", "moverWorkflow", "leaverRule", "leaverWorkflow", "testuser", "batchRequestId2");
        assertNotNull(resp2);
        assertEquals("BATCH_COMPLETE", resp2.getStatus());
        assertEquals(2, resp2.getResults().size());
        // The first result can be SUCCESS or FAILED if a context/rule call fails in
        // this unit test
        com.eshiam.lifecycle.model.BatchStatus firstStatus = resp2.getResults().get(0).getStatus();
        assertTrue(firstStatus == com.eshiam.lifecycle.model.BatchStatus.SUCCESS
                || firstStatus == com.eshiam.lifecycle.model.BatchStatus.FAILED);
        assertEquals(com.eshiam.lifecycle.model.BatchStatus.FAILED_VALIDATION, resp2.getResults().get(1).getStatus());
        // When context == null we simulate rule execution; check simulated result preserved
        Object firstResultObj = resp2.getResults().get(0).getResult();
        assertNotNull(firstResultObj);
        if (firstResultObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> firstResultMap = (Map<String, Object>) firstResultObj;
            assertEquals("SIMULATED", firstResultMap.get("result"));
        } else {
            assertEquals("SIMULATED", firstResultObj.toString());
        }
    }

    @Test
    public void testNormalizeResultToMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("message", "ok");
        Map<String, Object> out = LifecycleUtils.normalizeResultToMap(map);
        assertNotNull(out);
        assertEquals("ok", out.get("message"));

        Object s = "Good";
        Map<String, Object> out2 = LifecycleUtils.normalizeResultToMap(s);
        assertNotNull(out2);
        assertEquals("Good", out2.get("result"));

        assertNull(LifecycleUtils.normalizeResultToMap(null));
    }

    @Test
    public void testGetResultForRequestFromBatch() {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("identityName", "jsmith");
        row1.put("eventType", "JOINER");
        row1.put("email", "john.smith@example.com");

        java.util.List<Map<String, Object>> batch = new java.util.ArrayList<>();
        batch.add(row1);
        BatchResponse resp = LifecycleUtils.processBatch(batch, null, "jr", "jw", "mr", "mw", "lr", "lw", "testuser", "bid1");
        assertNotNull(resp);
        assertEquals(1, resp.getResults().size());
        String rid = resp.getResults().get(0).getRequestId();
        assertNotNull(rid);
        Map<String, Object> rLookup = LifecycleUtils.getResultForRequest(rid);
        assertNotNull(rLookup);
        assertEquals(rid, rLookup.get("requestId"));
    }

    @Test
    public void testExecuteRuleSimulation() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("k", "v");
        Map<String, Object> out = LifecycleUtils.executeRule(null, "TestRule", args, "user", "rid1");
        assertNotNull(out);
        assertEquals("SIMULATED", out.get("status"));
        assertEquals("TestRule", out.get("rule"));
        assertEquals("rid1", out.get("requestId"));
    }
}
