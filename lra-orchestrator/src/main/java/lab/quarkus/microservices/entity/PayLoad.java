package lab.quarkus.microservices.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "payload")
@Getter
@Setter
@NoArgsConstructor
public class PayLoad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    int payloadHash;
}
