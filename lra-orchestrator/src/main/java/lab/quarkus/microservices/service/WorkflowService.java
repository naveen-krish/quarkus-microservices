package lab.quarkus.microservices.service;

import lab.quarkus.microservices.entity.*;
import lab.quarkus.microservices.repository.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@ApplicationScoped
public class WorkflowService {

    private static final Logger LOG = Logger.getLogger(WorkflowService.class.getName());

    private final WorkflowRepository workflowRepository;
    private final APIRepository apiRepository;
    private final PayLoadRepository payLoadRepository;
    private final ForwardActivityRepository activityRepository;
    private final ReversalActivityRepository reversalActivityRepository;
    private final StatusRepository statusRepository;
    private final CompensationRepository compensationRepository;

    @Inject
    public WorkflowService(WorkflowRepository workflowRepository, APIRepository apiRepository, PayLoadRepository payLoadRepository, ForwardActivityRepository activityRepository, ReversalActivityRepository reversalActivityRepository, StatusRepository statusRepository, CompensationRepository compensationRepository) {
        this.workflowRepository = workflowRepository;
        this.apiRepository = apiRepository;
        this.payLoadRepository = payLoadRepository;
        this.activityRepository = activityRepository;
        this.reversalActivityRepository = reversalActivityRepository;
        this.statusRepository = statusRepository;
        this.compensationRepository = compensationRepository;
    }


    public Workflow getWorkflowByName(String name) {
        return workflowRepository.findByName(name);

    }


    public List<ForwardActivity> getForwardActivityByName(String name) {
        return activityRepository.findByName(name);
    }
    public List<ReversalActivity> getReversalActivityByName(String name) {
        return reversalActivityRepository.findByName(name);
    }
    public List<APIResponse> getActiveRetryData(String lraIdUrl) {
       // System.out.println(" Compensation LraId -> " + lraIdUrl);
        return apiRepository.findByActiveEndpointName(lraIdUrl);

    }

    public void insertWorkFlowDetails(String workflowSteps, String workflowName) {
        JSONObject json = new JSONObject(workflowSteps);
        JSONArray jsonarray = json.getJSONArray("saga");
        String url = "";


        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject objects = jsonarray.getJSONObject(i);
            Iterator key = objects.keys();
            while (key.hasNext()) {
                String activityKey = key.next().toString();
                url = objects.getString(activityKey);
                //   JSONObject responseObj = new JSONObject(response);
                APIResponse apiResponse = new APIResponse();
               // apiResponse.setLraIdUrl(lraIdUrl);
                //         apiResponse.setResponseData(responseObj.toString());
                String compensationUrl = objects.getString("compensationEndPoint");
                apiResponse.setCompensationUrl(compensationUrl);
                apiResponse.setActivityUrl(objects.getString("activityEndPoint"));
                apiResponse.setActivityStatus("COMPLETED");
                apiResponse.setCompensationStatus("PENDING");
                apiResponse.setSagaName(workflowName);
                apiResponse.setActivityRetryCount(objects.getInt("activityRetry"));
                apiResponse.setCompensationRetryCount(objects.getInt("compensationRetry"));
                apiResponse.setActivityRetryDoneCount(0);
                apiResponse.setCompensationRetryDoneCount(0);
                apiResponse.setRetriable(true);
                saveResponse(apiResponse);
            }
        }
    }
    @Transactional
    public void insertActivityDetails(String workflowSteps, String workflowName) {
        JSONObject json = new JSONObject(workflowSteps);
        JSONArray jsonarray = json.getJSONArray("saga");
        String url = "";


        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject objects = jsonarray.getJSONObject(i);
            Iterator key = objects.keys();
            while (key.hasNext()) {
                String activityKey = key.next().toString();
                if (activityKey.contains("activityEndPoint")) {
                    ForwardActivity forwardActivity = new ForwardActivity();
                    forwardActivity.setActivityUrl(objects.getString("activityEndPoint"));
                    forwardActivity.setCompensationUrl(objects.getString("compensationEndPoint"));
                    forwardActivity.setActivityStatus("PENDING");
                    forwardActivity.setActivityRetryCount(objects.getInt("activityRetry"));
                    forwardActivity.setSagaName(workflowName);
                    forwardActivity.setActivityRetryDoneCount(0);
                    forwardActivity.setInvoked(false);
                    activityRepository.saveActivity(forwardActivity);
                }else if (activityKey.contains("compensationEndPoint")) {
                    ReversalActivity reversalActivity = new ReversalActivity();
                    reversalActivity.setCompensationUrl(objects.getString("compensationEndPoint"));
                    reversalActivity.setCompensationStatus("PENDING");
                    reversalActivity.setCompensationRetryCount(objects.getInt("compensationRetry"));
                    reversalActivity.setSagaName(workflowName);
                    reversalActivity.setCompensationRetryDoneCount(0);
                    reversalActivityRepository.saveActivity(reversalActivity);
                }


            }
        }
    }
    public List<APIResponse> getCompensationData(String lraIdUrl) {
        return apiRepository.findByRetryEndpointName(lraIdUrl);
    }

    public Compensation getCompensation(String lraIdUrl) {
        return compensationRepository.fetchCompensation(lraIdUrl);
    }
    public String getReversalRetryData(String compensationUrl) {
        return activityRepository.findCompensationDataByUrl(compensationUrl).getResponseData();
    }
    public long countCompensationTobeDone( String lraIdUrl) {
        long totalCompensationCount =  activityRepository.countTotalCompensationDataByLraId(lraIdUrl);
        long retryCompensationCount=0;
        List<ReversalActivity> reversalActivities = reversalActivityRepository.retryCompensationDataByLraId(lraIdUrl);

        for(ReversalActivity reversalActivity:reversalActivities){
        //    System.out.println(" Adding CompensationRetryCount "+reversalActivity.getCompensationRetryCount());
            retryCompensationCount += reversalActivity.getCompensationRetryCount();
        }
     //   System.out.println(" Returing sum of compensation Count "+totalCompensationCount+ " : "+retryCompensationCount);
        return (totalCompensationCount+retryCompensationCount);
    }
    public long getCompensationCompletedStatus(String lraIdUrl) {
        return apiRepository.findStatus(lraIdUrl);
    }

    public Status workflowStatus(String lraIdUrl){
       return statusRepository.findStatusByLraId(lraIdUrl);
    }

    public boolean isSagaCancelled(String workflowName,String lraIdUrl){
        Set<String> lraIdUrlSet = new HashSet<>();
        boolean isSagaCancelled = false;
        List<Status> statusList = statusRepository.findStatusByWorkFlow(workflowName,lraIdUrl);
        List<ForwardActivity> forwardActivityList = activityRepository.findByName(workflowName,lraIdUrl);

        for (ForwardActivity activity : forwardActivityList) {
            lraIdUrlSet.add(activity.getLraIdUrl());
        }
        for (Status status : statusList) {
            if(lraIdUrlSet.contains(status.getLraIdUrl())) {
               // System.out.println(" Matching LRAID -> " + status.getLraIdUrl());
                isSagaCancelled = (status.getSagaStatus().equals("SAGA_CANCELLED") ? true : false);
                break;
            }

        }
            return isSagaCancelled;
    }

    public long inputHashCount(int hash){
        return payLoadRepository.findByHash(hash);
    }

    public long countHashCount(){
        return payLoadRepository.countByName();
    }
    @Transactional
    public void createWorkflow(Workflow workflow) {
        workflowRepository.persist(workflow);

    }

    @Transactional
    public void savePayLoadHash(PayLoad payload) {
        payLoadRepository.persist(payload);
    }

    @Transactional
    public void saveCompensation(Compensation compensation) {
        compensationRepository.persist(compensation);
    }


    @Transactional
    public void saveResponse(APIResponse response) {
        apiRepository.persist(response);
    }

    @Transactional
    public void saveForwardActivity(ForwardActivity forwardActivity) {
        //System.out.println(" Persisting ForwardActivity -> "+forwardActivity.getActivityUrl()+" -> "+forwardActivity.getActivityRetryDoneCount());
        activityRepository.persist(forwardActivity);
    }
    @Transactional
    public void saveReversalActivity(ReversalActivity reversalActivity) {
     //   System.out.println(" Persisting ForwardActivity -> "+reversalActivity.getCompensationUrl()+" -> "+reversalActivity.getCompensationRetryDoneCount());
        reversalActivityRepository.persist(reversalActivity);
    }
    @Transactional
    public void updateWorkflowRecord(long id,String payload,String lraIdUrl) {
        workflowRepository.updatePayload(id,payload,lraIdUrl);

    }

    @Transactional
    public void createSagaStatus(Status status) {
        statusRepository.createSagaStatus(status);
    }

    @Transactional
    public void updateSagaStatus(String lraIdUrl,String status) {
        statusRepository.updateSagaStatus(lraIdUrl,status);

    }
    public Workflow getPayLoadFromWorkflowRecord(String lraIdUrl) {
       return workflowRepository.findByLraId(lraIdUrl);
    }

    @Transactional
    public void updateStatus(long id,String status) {
        apiRepository.updateStatus(id, status);

    }

    public void updateRetryStatus(long id,APIResponse apiResponse) {
        apiRepository.updateRetryStatus(id,apiResponse);

    }

    private Response error(int code, String message) {
        return Response
                .status(code)
                .entity(Json.createObjectBuilder()
                        .add("error", message)
                        .add("code", code)
                        .build()
                )
                .build();
    }
}
