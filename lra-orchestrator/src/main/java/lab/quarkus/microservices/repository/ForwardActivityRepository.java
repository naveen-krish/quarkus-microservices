package lab.quarkus.microservices.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import lab.quarkus.microservices.entity.ForwardActivity;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ForwardActivityRepository implements PanacheRepository<ForwardActivity> {

    public void saveActivity(ForwardActivity forwardActivity){
        persist(forwardActivity);
    }

    public List<ForwardActivity> findByName(String name,String lraIdUrl){
        return find("sagaName=?1 and lraIdUrl=?2", name,lraIdUrl).list();
    }

    public List<ForwardActivity> findByName(String name){
        return find("sagaName", name).list();
    }


    public ForwardActivity findByLraId(String lraIdUrl){
        return find("lraIdUrl", lraIdUrl).firstResult();
    }
    public ForwardActivity findCompensationDataByUrl(String compensationUrl){
        return find("compensationUrl", compensationUrl).firstResult();
    }

    public long countTotalCompensationDataByLraId(String lraIdUrl){
       return count("lraIdUrl =?1 and responseData is not null", lraIdUrl);
    }
    public List<ForwardActivity> findByWorkflowName(String workflowName){
        return find("sagaName", workflowName).list();
    }
}

