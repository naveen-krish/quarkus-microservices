package lab.quarkus.microservices;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WorkflowRepository implements PanacheRepository<Workflow> {

// //   @Query(value = "SELECT lab.quarkus.microservices.Workflow from workflow u where u.workflowName = ?1")
// @Query(value = "SELECT Workflow from Workflow u where u.workflowName = ?1",nativeQuery=true)
//
// public Workflow findWorkflowByName(String workflowName);

    public  Workflow findByName(String name){
        return find("workflowName", name).firstResult();
    }

}