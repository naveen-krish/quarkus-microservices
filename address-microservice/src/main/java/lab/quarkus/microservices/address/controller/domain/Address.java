package lab.quarkus.microservices.address.controller.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.json.bind.annotation.JsonbProperty;

@Getter
@Setter
@NoArgsConstructor
public class Address {

    @JsonbProperty("client_address")
    private String customerAddress = "Beverly Hills USA";
}
