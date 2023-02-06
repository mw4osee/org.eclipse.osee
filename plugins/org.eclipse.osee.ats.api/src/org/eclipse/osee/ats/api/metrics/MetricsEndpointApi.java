/*********************************************************************
 * Copyright (c) 2022 Boeing
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/
package org.eclipse.osee.ats.api.metrics;

import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Stephen J. Molaro
 */
@Path("metrics")
public interface MetricsEndpointApi {

   @Path("DevProgress/{targetVersion}")
   @GET
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   public Response devProgressReport(@PathParam("targetVersion") String targetVersion, @QueryParam("startDate") Date startDate, @QueryParam("endDate") Date endDate, @QueryParam("weekday") int weekday, @QueryParam("iterationLength") int iterationLength, @QueryParam("periodic") boolean periodic, @QueryParam("nonPeriodic") boolean nonPeriodic, @QueryParam("periodicTask") boolean periodicTask, @QueryParam("nonPeriodicTask") boolean nonPeriodicTask);

   @Path("SoftwareReqVolatility/{targetVersion}")
   @GET
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   public Response softwareReqVolatility(@PathParam("targetVersion") String targetVersion, @QueryParam("includeUnchangedCode") boolean includeUnchangedCode);
}