package lab.quarkus.microservices.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import lab.quarkus.microservices.entity.Status;
import lab.quarkus.microservices.entity.Workflow;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class StatusRepository implements PanacheRepository<Status> {

    public void createSagaStatus(Status status) {
        persist(status);
    }

    public Status findStatusByLraId(String lraIdUrl){
        return find("lraIdUrl", lraIdUrl).firstResult();
    }

    public List<Status> findStatusByWorkFlow(String workflowName,String lraIdUrl){
        return find("workflowName = ?1 and lraIdUrl=?2 ",workflowName,lraIdUrl).list();
    }

    public void updateSagaStatus(String lraIdUrl,String sagaStatus) {
       Status status = findStatusByLraId(lraIdUrl);
        status.setSagaStatus(sagaStatus);
        persist(status);
    }
}

