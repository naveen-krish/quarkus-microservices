package lab.quarkus.microservices.anagrafe.repository;

import lab.quarkus.microservices.anagrafe.domain.Customer;
import lab.quarkus.microservices.anagrafe.proxy.AddressProxy;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AnagrafeRepository {

    @RestClient
    AddressProxy proxy;

    @Inject
    Logger logger;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Retry(maxRetries = 1, delay = 1000)
    @Fallback(fallbackMethod = "fallbackOnGetAllCustomers")
    public List<Customer> getAllCustomers() {
        logger.info(" getAllCustomers returning Customer Details ");
       return List.of(new Customer("Tony", "Stark", proxy.getAddress().customerAddress));
    }

    public List<Customer> fallbackOnGetAllCustomers() {
        logger.warn(" Returning Partial Response ");
        return List.of(new Customer("Tony", "Stark", ""));
    }

    public Optional<Customer> getCustomerByFirstName(String firstName) {
        return getAllCustomers().stream().filter(customer -> customer.getFirstName().equals(firstName)).findFirst();
    }

}