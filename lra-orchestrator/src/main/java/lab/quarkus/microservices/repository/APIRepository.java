package lab.quarkus.microservices.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import lab.quarkus.microservices.entity.APIResponse;
import lab.quarkus.microservices.entity.Workflow;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class APIRepository implements PanacheRepository<APIResponse> {

    public List<APIResponse> findByActiveEndpointName(String lraIdUrl) {

       // return find("lraIdUrl = ?1 and status = 'PENDING'", lraIdUrl).list();
       // return find("lraIdUrl = ?1 and status = 'PENDING'  and activityRetryDoneCount != activityRetryCount", lraIdUrl).list();
        return find("lraIdUrl = ?1 and activityStatus = 'PENDING'  and activityRetryDoneCount != activityRetryCount", lraIdUrl).list();

    }

    public List<APIResponse> findByRetryEndpointName(String lraIdUrl) {

        // return find("lraIdUrl = ?1 and status = 'PENDING'", lraIdUrl).list();
        // return find("lraIdUrl = ?1 and status = 'PENDING'  and activityRetryDoneCount != activityRetryCount", lraIdUrl).list();
        return find("lraIdUrl = ?1 and compensationStatus = 'PENDING'  and compensationRetryCount != compensationRetryDoneCount", lraIdUrl).list();

    }

    public  APIResponse findByName(String name){
        return find("sagaName", name).firstResult();
    }


    public long findStatus(String lraIdUrl) {

       long count =  count("lraIdUrl = ?1 and status = 'COMPLETED'", lraIdUrl);
       System.out.println(" TxCount "+count);
       return count;

      //  return find("lraIdUrl = ?1 and status = 'COMPLETED'", lraIdUrl).list().isEmpty();
    }
    public void updateStatus(long id,String status) {
        APIResponse apiRecord = findById(id);
       // System.out.println(" API Record -> "+apiRecord.getResponseData()+" status -> "+apiRecord.getStatus());
        apiRecord.setActivityStatus(status);
        persist(apiRecord);

    }
    public void updateRetryStatus(long id,APIResponse apiResponse) {
//        APIResponse apiRecord = findById(id);
//        int activityRetryCount = apiRecord.getActivityRetryDoneCount();
//        int compensationRetryCount = apiRecord.getCompensationRetryDoneCount();
//        apiRecord.setActivityRetryDoneCount(++activityRetryCount);
//        //apiRecord.setCompensationRetryDoneCount(++compensationRetryCount);
        persist(apiResponse);

    }

    public boolean isTxPending(String lraIdUrl){
        System.out.println(" Tx Pending called ");
        long count  = count("lraIdUrl = ?1 and status = 'PENDING'",lraIdUrl);
        System.out.println(" Tx Pending count -> "+count);
        return count > 0 ? true: false;
    }
}
