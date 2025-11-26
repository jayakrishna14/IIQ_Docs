package com.eshiam.lifecycle.rest;


import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.object.*;
import sailpoint.api.*;
import sailpoint.rest.plugin.AllowAll;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.RequiredRight;
import sailpoint.tools.GeneralException;

/**
 * @author Adam Creaney
 * 
 *         Class to manage all rest api calls.
 */
@Path("AutomationLCE")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@AllowAll
public class ExamplePluginRestManager extends BasePluginResource {
	
	private static final Log	log	= LogFactory.getLog(ExamplePluginRestManager.class);
	private Response			response;
	
	public String getPluginName() {
		
		return "AutomationLCE";
	}

	
	@GET
	@Path("getExample")
	public Response getExample() throws GeneralException, SQLException {
		
		log.error("Entering getExample...");
		SailPointContext context = SailPointFactory.getCurrentContext();

		QueryOptions qo = new QueryOptions();
		Filter filter = Filter.eq("name", "Aaron Nichols");
		qo.addFilter(filter);
		int count = context.countObjects(Identity.class, qo);

		String responseString = "How many Adams? " + count;
		response = Response.status(Response.Status.OK).entity(responseString).build();

		log.error("Exiting getExample...");

		return response;
	}
	
	@POST
	@Path("postExample")
	public Response postExample(Map<String, Object> request) throws GeneralException, SQLException, ParseException {
		log.error("Entering postExample...");


		log.error("Exiting postExample...");
		
		return response;
	}

	/*		QueryOptions qo = new QueryOptions();
		Filter filter = Filter.eq("name", "Adam.Kennedy");
		qo.addFilter(filter);

		int adams = context.countObjects(Identity.class, qo);
		String responseString = "I found some Adams: " + adams;
	*/
}
