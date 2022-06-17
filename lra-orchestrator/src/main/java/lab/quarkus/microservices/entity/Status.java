package lab.quarkus.microservices.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "status")
@Getter
@Setter
@NoArgsConstructor
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    String sagaStatus;
    String lraIdUrl;
    String workflowName;
}
