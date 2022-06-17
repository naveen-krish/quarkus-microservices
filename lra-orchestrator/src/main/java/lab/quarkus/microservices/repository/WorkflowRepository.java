package lab.quarkus.microservices.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import lab.quarkus.microservices.entity.APIResponse;
import lab.quarkus.microservices.entity.Workflow;

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



    public  Workflow findByLraId(String lraIdUrl){
        return find("lraIdUrl", lraIdUrl).firstResult();
    }
    public void updatePayload(long id,String payload,String lraIdUrl) {
        Workflow workflow = findById(id);
        // System.out.println(" API Record -> "+apiRecord.getResponseData()+" status -> "+apiRecord.getStatus());
        workflow.setPayload(payload);
        workflow.setLraIdUrl(lraIdUrl);
        persist(workflow);

    }
}