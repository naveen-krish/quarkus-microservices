package lab.quarkus.microservices.address.controller;

import lab.quarkus.microservices.address.controller.domain.Address;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api/address")
public class AddressMS {

    @Inject
    Logger logger;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Address getAddress() {
        return new Address();
    }
}