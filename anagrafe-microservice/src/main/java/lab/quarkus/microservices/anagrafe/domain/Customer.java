package lab.quarkus.microservices.anagrafe.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@Schema(description ="Customer Info")
public class Customer {

    private String firstName;
    private String lastName;
    private String address;

    public Customer(String firstName, String lastName, String address){

        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }
}
