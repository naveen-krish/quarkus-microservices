package lab.quarkus.microservices.anagrafe.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.json.bind.annotation.JsonbProperty;

@Getter
@Setter
@NoArgsConstructor
public class Address {

    @JsonbProperty("client_address")
    public String customerAddress;
}
