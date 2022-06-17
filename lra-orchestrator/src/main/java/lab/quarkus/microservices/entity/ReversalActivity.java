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
       // @TypeDef(name = "json", typeClass = JsonStringType.class)
        @TypeDef(name = "json", typeClass = JsonType.class)

})

@Entity
@Table(name = "reversal")
@Getter
@Setter
@NoArgsConstructor
public class ReversalActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String lraIdUrl;
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private String responseData;
    private String compensationUrl;
    private String compensationStatus;
    private String sagaName;
    private boolean isInvoked;
    private int compensationRetryCount;
    private int compensationRetryDoneCount;

}
