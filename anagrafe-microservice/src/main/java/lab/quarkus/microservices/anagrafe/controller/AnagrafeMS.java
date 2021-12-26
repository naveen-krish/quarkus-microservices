package lab.quarkus.microservices.anagrafe.controller;

import lab.quarkus.microservices.anagrafe.domain.Customer;
import lab.quarkus.microservices.anagrafe.repository.AnagrafeRepository;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;


@Path("/api/customers")
@Tag(name = " Anagrafe Customer API's ")

public class AnagrafeMS {

    @Inject
    AnagrafeRepository repository;
    @Inject
    Logger logger;
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Retrieves all Customer Details",
            description = " Customer Name , contacts etc.."
    )
    public List<Customer> getAllCustomers() {
        List<Customer> customerList = repository.getAllCustomers();
        logger.info(" << Customer Details API >> " + customerList.size());

        return customerList;
    }
}