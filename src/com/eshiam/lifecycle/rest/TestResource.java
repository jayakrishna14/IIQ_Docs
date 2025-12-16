package com.eshiam.lifecycle.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.AllowAll;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("AutomationLCE")
@Produces(MediaType.APPLICATION_JSON)
@AllowAll
public class TestResource extends BasePluginResource {

    private static final Log log = LogFactory.getLog(TestResource.class);

    @Override
    public String getPluginName() {
        log.error("getPluginName called");
        return "AutomationLCE";
    }

    @GET
    @Path("test")
    public Response testEndpoint() {
        log.error("Test endpoint hit");
        return Response.ok("{\"status\":\"OK\",\"message\":\"Plugin is deployed!\"}").build();
    }
}
