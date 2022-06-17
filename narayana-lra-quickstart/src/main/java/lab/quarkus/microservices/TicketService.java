package lab.quarkus.microservices;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/tickets")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "tickets_proxy.dns")
public interface TicketService {

    @GET
    @Path("/book")
    String bookTicket();
}