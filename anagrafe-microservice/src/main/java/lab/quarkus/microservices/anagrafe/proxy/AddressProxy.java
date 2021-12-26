package lab.quarkus.microservices.anagrafe.proxy;

import lab.quarkus.microservices.anagrafe.domain.Address;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "address_proxy.dns")
@Path("/api/address")
public interface AddressProxy {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Address getAddress();
}

