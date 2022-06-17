package lab.quarkus.microservices.entity;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;

@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonType.class)
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

    @Type(type = "json")
    @Column(columnDefinition = "json")
    private String payload;

    private String lraIdUrl;



}
