package lab.quarkus.microservices.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "compensation")
@Getter
@Setter
@NoArgsConstructor
public class Compensation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    String lraIdUrl;
    private long totalCompensationCount;
    private long compensationDoneCount;
}
