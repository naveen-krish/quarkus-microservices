package lab.quarkus.microservices.anagrafe;

import io.quarkus.test.Mock;
import lab.quarkus.microservices.anagrafe.domain.Address;
import lab.quarkus.microservices.anagrafe.proxy.AddressProxy;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.json.bind.annotation.JsonbProperty;

@Mock
@RestClient
public class MockAddressProxy implements AddressProxy {

    @Override
    public Address getAddress() {

        Address address = new Address();
        address.customerAddress = "Beverly Hills USA";
        return address;
    }
}
