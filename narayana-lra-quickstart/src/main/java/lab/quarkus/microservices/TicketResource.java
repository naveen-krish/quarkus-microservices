package lab.quarkus.microservices;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

// import annotation definitions
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
// import the definition of the LRA context header
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

// import some JAX-RS types
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.HeaderParam;

@Path("/tickets")
@Produces(APPLICATION_JSON)
public class TicketResource {

    @GET
    @Path("/book")
    @LRA(value = LRA.Type.REQUIRED, end = false) // an LRA will be started before method execution if none exists and will not be ended after method execution
    public Response bookTicket(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId) throws Exception{
//        System.out.printf("TicketResource.bookTicket: %s%n", lraId);
//        String ticket = "1234";
//        return Response.ok(ticket).build();
        throw new Exception("Booking Failed...");
    }

    // ask to be notified if the LRA closes
    @PUT // must be PUT
    @Path("/complete")
    @Complete
    public Response completeWork(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId) {
        System.out.printf("TicketResource.completeWork: %s%n", lraId);
        return Response.ok().build();
    }

    // ask to be notified if the LRA cancels
    @PUT // must be PUT
    @Path("/compensate")
    @Compensate
    public Response compensateWork(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId) {
        System.out.printf("TicketResource.compensateWork: %s%n", lraId);
        return Response.ok().build();
    }
}