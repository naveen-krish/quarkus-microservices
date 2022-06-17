package lab.quarkus.microservices;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

// import annotation definitions
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
// import the definition of the LRA context header
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

// import some JAX-RS types
import javax.ws.rs.PUT;
import javax.ws.rs.HeaderParam;

@Path("/trips")
@Produces(APPLICATION_JSON)
public class TripResource {

    @Inject
    TripService service;

    // annotate the hello method so that it will run in an LRA
    @GET
    @LRA(LRA.Type.REQUIRED) // an LRA will be started before method execution and ended after method execution
    @Path("/book")
    public Response bookTrip(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId) {
        System.out.printf("TripResource.bookTrip %s%n", lraId);
        String ticket = service.bookTrip();
        return Response.ok(ticket).build();
    }

    // ask to be notified if the LRA closes
    @PUT // must be PUT
    @Path("/complete")
    @Complete
    public Response completeWork(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId) {
        System.out.printf("TripResource.completeWork %s%n", lraId);
        return Response.ok().build();
    }

    // ask to be notified if the LRA cancels
    @PUT // must be PUT
    @Path("/compensate")
    @Compensate
    public Response compensateWork(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId) {
        System.out.printf("TripResource.compensateWork %s%n", lraId);
        return Response.ok().build();
    }
}