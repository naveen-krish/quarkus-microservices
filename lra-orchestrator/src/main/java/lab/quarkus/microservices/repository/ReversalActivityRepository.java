package lab.quarkus.microservices.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import lab.quarkus.microservices.entity.ForwardActivity;
import lab.quarkus.microservices.entity.ReversalActivity;
import org.springframework.data.jpa.repository.Query;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ReversalActivityRepository implements PanacheRepository<ReversalActivity> {

    //@Query(value = "select sum(compensationRetryCount) from reversal where lraIdUrl== ?1");
    public List<ReversalActivity> retryCompensationDataByLraId(String lraIdUrl){
        return find("lraIdUrl",lraIdUrl).list();

    }

    public void saveActivity(ReversalActivity reversalActivity){
        persist(reversalActivity);
    }

    public List<ReversalActivity> findByName(String name){
        return find("sagaName", name).list();
    }


}