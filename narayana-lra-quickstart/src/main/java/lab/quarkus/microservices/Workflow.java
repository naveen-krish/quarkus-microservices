package lab.quarkus.microservices;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;

@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonStringType.class)
})
@Entity
@Table(name = "workflow")
@Getter
@Setter
@NoArgsConstructor
public class Workflow { //extends PanacheEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String workflowName;
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private String workflowSteps;



}
