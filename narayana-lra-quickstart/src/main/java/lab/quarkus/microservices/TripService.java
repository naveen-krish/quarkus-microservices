package lab.quarkus.microservices;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TripService {

    @Inject
    @RestClient
    TicketService ticketService;

    String bookTrip() {
        return ticketService.bookTicket(); // only one service will be used for the trip booking

        // if other services need to be part of the trip they would be called here
        // and the TripService would associate each step of the booking with the id of the LRA
        // (although I've not shown it being passed in this example) and that would form the
        // basis of the ability to compensate or clean up depending upon the outcome.
        // We may include a more comprehensive/realistic example in a later blog.
    }
}