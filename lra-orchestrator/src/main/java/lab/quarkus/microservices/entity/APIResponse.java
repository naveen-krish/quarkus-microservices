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
@Table(name = "response")
@Getter
@Setter
@NoArgsConstructor
public class APIResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String lraIdUrl;
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private String responseData;
    private String activityUrl;
    private String compensationUrl;
    private String activityStatus;
    private String compensationStatus;
    private String sagaName;
    private boolean isRetriable;
    private int activityRetryCount;
    private int compensationRetryCount;
    private int activityRetryDoneCount;
    private int compensationRetryDoneCount;

}
