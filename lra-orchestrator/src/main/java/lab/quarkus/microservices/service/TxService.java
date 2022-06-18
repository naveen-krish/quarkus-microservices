package lab.quarkus.microservices.service;

import lab.quarkus.microservices.entity.*;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static lab.quarkus.microservices.LRATxCoordinator.getTxId;

@ApplicationScoped
public class TxService {

    private Client txClient;
    private WebTarget txTarget;

    private Client rollbackClient;
    private WebTarget rollbackTarget;
//
//    private Client anagrageClient;
//    private Client addressClient;
//    private Client accountsClient;
//    private WebTarget anagrafeTarget;
//    private WebTarget addressTarget;
//    private WebTarget accountsTarget;

    @Inject
    WorkflowService worflowService;

    private static final Logger LOG = Logger.getLogger(TxService.class.getName());

    @PostConstruct
    private void init() {
//        anagrageClient = ClientBuilder.newClient();
//        addressClient = ClientBuilder.newClient();
//        accountsClient = ClientBuilder.newClient();

        txClient = ClientBuilder.newClient();
        rollbackClient = ClientBuilder.newClient();

    }

    @PreDestroy
    private void destroy() {
//        anagrageClient.close();
//        addressClient.close();
//        accountsClient.close();
        txClient.close();
        rollbackClient.close();
    }

//    @Transactional
//    Response invokeAnagrafe(String lraIdUrl,String payLoad){
//
//
//    }
    @Transactional
    public Response invokeTx(String lraIdUrl, Map payload) {

        String workflowName = String.valueOf(payload.get("Saga_Name"));
        String txPayload = String.valueOf(payload.get("Tx_payload"));
        int inputHash = txPayload.hashCode();
        int status = 200, activityRetryDoneCount = 0, activityRetryCount = 0,
                activityRetryOriginalCount = 0, compensationRetryDoneCount = 0, compensationRetryCount = 0, compensationRetryOriginalCount = 0;
        String url = "";
        Response txResponse = null;
        boolean isInvoked = false;

        if (worflowService.countHashCount() == 0) {
            PayLoad payLoad = new PayLoad();
            payLoad.setPayloadHash(inputHash);
            worflowService.savePayLoadHash(payLoad);
        }
        //  Status sagaStatus = worflowService.workflowStatus(lraIdUrl);
        Workflow workflow = worflowService.getWorkflowByName(workflowName);
        worflowService.updateWorkflowRecord(workflow.getId(), txPayload, lraIdUrl);
        boolean isSagaCancelled = worflowService.isSagaCancelled(workflowName,lraIdUrl);
  //    System.out.println(" SagaStatus inside TxMethod  cancelledStatus -> " + isSagaCancelled);
////       if( sagaStatus != null) {
////           System.out.println(" SagaStatus -> " + sagaStatus.getSagaStatus());
////       }else{
////           Status newSagaStatus = new Status();
////           newSagaStatus.setLraIdUrl(lraIdUrl);
////           newSagaStatus.setSagaStatus(SagaStatus.SAGA_EXECUTING.name());
////           worflowService.createSagaStatus(newSagaStatus);
//       }

        //    if (sagaStatus != null && sagaStatus.getSagaStatus() != SagaStatus.SAGA_CANCELLED.name()) {
        if (!isSagaCancelled) {
            List<ForwardActivity> forwardActivities = worflowService.getForwardActivityByName(workflowName);
            try {
                for (ForwardActivity forwardActivity : forwardActivities) {
                    activityRetryDoneCount = forwardActivity.getActivityRetryDoneCount();
                    activityRetryCount = activityRetryDoneCount;
                    activityRetryOriginalCount = forwardActivity.getActivityRetryCount();

                    forwardActivity.setLraIdUrl(lraIdUrl);
                    url = forwardActivity.getActivityUrl();

             //        System.out.println(" forwardActivity.getActivityRetryDoneCount() -> " + forwardActivity.getActivityRetryDoneCount() + " forwardActivity.getActivityRetryCount() -> " + forwardActivity.getActivityRetryCount()+" forwardActivity.isInvoked() -> "+forwardActivity.isInvoked());
                    if ((!forwardActivity.isInvoked() && forwardActivity.getActivityRetryCount() == 0) || (forwardActivity.getActivityRetryDoneCount() < forwardActivity.getActivityRetryCount() && !forwardActivity.getActivityStatus().equals(SagaStatus.ACTIVITY_COMPLETED.name()))) {

                        if (!forwardActivity.isInvoked()) {
                            LOG.log(Level.INFO, " [ {0} ] INVOKING TX-ENDPOINT -> http://TX_SERVICE_HOST_URL:PORT/{1} CONFIGURED WITH RETRY_TIMES {2}  ", new Object[]{workflowName, getMicroServiceName(url), forwardActivity.getActivityRetryCount()});
                        } else {
                            int retry_count = forwardActivity.getActivityRetryDoneCount();
                            LOG.log(Level.INFO, " [ {0} ]  RETRY NUMBER: {2}  FOR TX-ENDPOINT -> http://TX_SERVICE_HOST_URL:PORT/{1}  ", new Object[]{workflowName, getMicroServiceName(url), ++retry_count});
                        }
                        if (forwardActivity.getActivityRetryCount() != 0 && forwardActivity.isInvoked()) {
                            forwardActivity.setActivityRetryDoneCount(++activityRetryDoneCount);
                  //          System.out.println(" Updating Retry done count for -> "+forwardActivity.getActivityUrl()+ " "+forwardActivity.getActivityRetryDoneCount());
                        }

                        forwardActivity.setInvoked(true);
                        isInvoked = true;
                        worflowService.saveForwardActivity(forwardActivity);
                        txTarget = txClient.target(url);
                        txResponse = txTarget.request().post(Entity.entity(txPayload, MediaType.APPLICATION_JSON_TYPE));

                        String response = txResponse.readEntity(String.class);
                        LOG.log(Level.INFO, " [ {0} ] TRANSACTION API RESPONSE ->  [ {1} ] ", new Object[]{workflowName, response});
                        JSONObject responseObj = new JSONObject(response);
                        forwardActivity.setResponseData(responseObj.toString());
                        forwardActivity.setActivityStatus(SagaStatus.ACTIVITY_COMPLETED.name());
                    }

                }
            } catch (Exception e) {
                if (activityRetryDoneCount == 0) {
                    LOG.log(Level.INFO, " [ {0} ] SERVICE DOWN TRANSACTION FAILED ON API...  -> TX_SERVICE_HOST_URL/{1} - RETRY PROCESS WILL BE EXECUTED...", new Object[]{workflowName, getMicroServiceName(url)});
                } else {
                    LOG.log(Level.INFO, " [ {0} ] RETRY ON TRANSACTION API FAILED...  -> TX_SERVICE_HOST_URL/{1} - RETRY PROCESS WILL BE EXECUTED...", new Object[]{workflowName, getMicroServiceName(url)});
                }
                //   System.out.println(" ActivityDone & OriginalCount -> "+activityRetryDoneCount+" - "+activityRetryOriginalCount);
                if (activityRetryDoneCount == activityRetryOriginalCount) {
                    LOG.log(Level.INFO, " [ {0} ] SERVICE MAXIMUM RETRY LIMIT EXCEEDED NOT PROCEEDING WITH THE SAGA, TRIGGERING COMPENSATION FLOW...  ", new Object[]{workflowName});
                    worflowService.updateSagaStatus(lraIdUrl, SagaStatus.SAGA_CANCELLED.name());
                    //   return Response.ok().entity(ParticipantStatus.Compensating).build();
                }
                status = 500;
            }

        } else {

            List<ReversalActivity> reversalActivities = worflowService.getReversalActivityByName(workflowName);
            LOG.log(Level.INFO, " [ {0} ] INITIATING COMPENSATING TRANSACTION -> {1} ", new Object[]{workflowName,lraIdUrl});
            long compensationTotalCount = worflowService.countCompensationTobeDone(lraIdUrl);
            Compensation compensation = worflowService.getCompensation(lraIdUrl);
            if (compensation == null) {
                compensation = new Compensation();
                compensation.setLraIdUrl(lraIdUrl);
                compensation.setTotalCompensationCount(compensationTotalCount);
                compensation.setCompensationDoneCount(0);
                worflowService.saveCompensation(compensation);
            }
            long compensationDoneCount = compensation.getCompensationDoneCount();
            long totalCompensationCount = compensation.getTotalCompensationCount();
            if ( compensationDoneCount < totalCompensationCount) {
           //     System.out.println(" compensation.getCompensationDoneCount() < compensation.getTotalCompensationCount() " + compensationDoneCount + " " + totalCompensationCount);

                try {
                    for (ReversalActivity reversalActivity : reversalActivities) {
                        compensationRetryDoneCount = reversalActivity.getCompensationRetryDoneCount();
                        compensationRetryCount = compensationRetryDoneCount;
                        compensationRetryOriginalCount = reversalActivity.getCompensationRetryCount();

                        reversalActivity.setLraIdUrl(lraIdUrl);
                        url = reversalActivity.getCompensationUrl();
                        String compensationData = worflowService.getReversalRetryData(reversalActivity.getCompensationUrl());


                        //   System.out.println(" forwardActivity.getActivityRetryDoneCount() -> " + forwardActivity.getActivityRetryDoneCount() + " forwardActivity.getActivityRetryCount() -> " + forwardActivity.getActivityRetryCount()+" forwardActivity.isInvoked() -> "+forwardActivity.isInvoked());
                        if ((!reversalActivity.isInvoked() && reversalActivity.getCompensationRetryCount() == 0 && compensationData != null) || (reversalActivity.getCompensationRetryDoneCount() < reversalActivity.getCompensationRetryCount()) && compensationData != null) {

                            if (!reversalActivity.isInvoked()) {
                                LOG.log(Level.INFO, " [ {0} ] INVOKING COMPENSATION-ENDPOINT -> http://TX_SERVICE_HOST_URL:PORT/{1} CONFIGURED WITH RETRY_TIMES {2}  ", new Object[]{workflowName, getMicroServiceName(url), reversalActivity.getCompensationRetryCount()});
                            } else {
                                int compensation_retry_count = reversalActivity.getCompensationRetryDoneCount();
                                LOG.log(Level.INFO, " [ {0} ]  RETRY NUMBER: {2} FOR COMPENSATION-ENDPOINT -> http://TX_SERVICE_HOST_URL:PORT/{1}  ", new Object[]{workflowName, getMicroServiceName(url), ++compensation_retry_count});
                            }
                            if (reversalActivity.getCompensationRetryCount() != 0 && reversalActivity.isInvoked())
                                reversalActivity.setCompensationRetryDoneCount(++compensationRetryDoneCount);

                            reversalActivity.setInvoked(true);
                            isInvoked = true;
                            worflowService.saveReversalActivity(reversalActivity);
                            //  String compensationData = worflowService.getReversalRetryData(reversalActivity.getCompensationUrl());
                            JSONObject json = new JSONObject(compensationData);
                            System.out.println(" Reversal Compensation Data -> " + compensationData);

                            rollbackTarget = rollbackClient.target(url);
                            txResponse = rollbackTarget.request().put(Entity.entity(json.get("entity_id"), MediaType.APPLICATION_JSON_TYPE));
                            String response = txResponse.readEntity(String.class);
                            LOG.log(Level.INFO, " [ {0} ] TRANSACTION API RESPONSE ->  [ {1} ] ", new Object[]{workflowName, response});
//                        JSONObject responseObj = new JSONObject(response);
                            reversalActivity.setCompensationStatus("OK");
                            compensation.setCompensationDoneCount(++compensationDoneCount);
                            worflowService.saveCompensation(compensation);
                        }

                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    if (compensationRetryDoneCount == 0)
                        LOG.log(Level.INFO, " [ {0} ] SERVICE DOWN TRANSACTION FAILED ON API...  -> TX_SERVICE_HOST_URL/{1} - RETRY PROCESS WILL BE EXECUTED...", new Object[]{workflowName, getMicroServiceName(url)});
                    else
                        LOG.log(Level.INFO, " [ {0} ] RETRY ON TRANSACTION API FAILED...  -> TX_SERVICE_HOST_URL/{1} - RETRY PROCESS WILL BE EXECUTED...", new Object[]{workflowName, getMicroServiceName(url)});

                    if (compensationRetryDoneCount == compensationRetryOriginalCount) {
                        //       LOG.log(Level.INFO, " [ {0} ] SERVICE MAXIMUM RETRY LIMIT EXCEEDED NOT PROCEEDING SAGA...  -> TX_SERVICE_HOST_URL/{1} ", new Object[]{workflowName, getMicroServiceName(url)});
                        LOG.log(Level.INFO, " [ {0} ] SERVICE MAXIMUM RETRY LIMIT EXCEEDED MARKING FOR RECONCILIATION...  ", new Object[]{workflowName});
                        //   worflowService.updateSagaStatus(lraIdUrl, SagaStatus.SAGA_CANCELLED.name());
                        //  return Response.ok().entity(ParticipantStatus.FailedToCompensate.name()).build();
                    }
                    status = 500;
                }
            }else{
                LOG.log(Level.INFO, " [ {0} ] SAGA COMPLETED FOR TX-ID  {1} ", new Object[]{workflowName,lraIdUrl});
                return Response.ok().entity(ParticipantStatus.Compensated.name()).build();
            }
        }
        if (status == 200)
            return Response.ok().entity(ParticipantStatus.Compensated.name()).build();
        else
            return Response.status(500).build();

    }

//    @Transactional
//    public Response invokeTx(String lraIdUrl, Map payload) {
//
//        String workflowName = String.valueOf(payload.get("Saga_Name"));
//        String txPayload = String.valueOf(payload.get("Tx_payload"));
//        int inputHash = txPayload.hashCode();
//        System.out.println(" Exists -> " + worflowService.countHashCount());
//        System.out.println(" Payload Hash value " + inputHash);
//
//        Response txResponse = null;
//        int status = 200;
//        String url = "";
//
//        if (worflowService.countHashCount() == 0) {
//            PayLoad payLoad = new PayLoad();
//            payLoad.setPayloadHash(inputHash);
//            worflowService.savePayLoadHash(payLoad);
//            System.out.println(" Persisting Hash value " + inputHash);
//
//            LOG.log(Level.INFO, " INITIATING TRANSACTION ... -> [ {0} ] ", lraIdUrl);
//            System.out.println();
//
//            Workflow workflow = worflowService.getWorkflowByName(workflowName);
//            worflowService.updateWorkflowRecord(workflow.getId(), txPayload, lraIdUrl);
//            String workflowSteps = workflow.getWorkflowSteps();
//            JSONObject json = new JSONObject(workflowSteps);
//            JSONArray jsonarray = json.getJSONArray("saga");
//
//            try {
//                for (int i = 0; i < jsonarray.length(); i++) {
//                    JSONObject objects = jsonarray.getJSONObject(i);
//                    Iterator key = objects.keys();
//                    while (key.hasNext()) {
//                        String activityKey = key.next().toString();
//
//                        if (activityKey.contains("activityEndPoint")) {
//                            url = objects.getString(activityKey);
//                            APIResponse apiResponse = worflowService.getAPIByName(workflowName);
//                            if (apiResponse.getActivityRetryDoneCount() != apiResponse.getActivityRetryCount()) {
//                                LOG.log(Level.INFO, " [ {0} ] INVOKING TX-ENDPOINT -> http://TX_SERVICE_HOST_URL:PORT/{1} ", new Object[]{workflowName, getMicroServiceName(url)});
//                                txTarget = txClient.target(url);
//                                txResponse = txTarget.request().post(Entity.entity(txPayload, MediaType.APPLICATION_JSON_TYPE));
//
//                                String response = txResponse.readEntity(String.class);
//                                LOG.log(Level.INFO, " [ {0} ] TRANSACTION API RESPONSE ->  [ {1} ] ", new Object[]{workflowName, response});
//                                JSONObject responseObj = new JSONObject(response);
//
//                                //new APIResponse();
//                                apiResponse.setLraIdUrl(lraIdUrl);
//                                apiResponse.setResponseData(responseObj.toString());
//                                int activityRetryCount = apiResponse.getActivityRetryDoneCount();
//                                apiResponse.setActivityRetryDoneCount(++activityRetryCount);
////                            String compensationUrl = objects.getString("compensationEndPoint");
////                            apiResponse.setCompensationUrl(compensationUrl);
////                            apiResponse.setActivityUrl(objects.getString("activityEndPoint"));
////                            apiResponse.setActivityStatus("COMPLETED");
////                            apiResponse.setCompensationStatus("PENDING");
////                            apiResponse.setSagaName(workflowName);
////                            apiResponse.setActivityRetryCount(objects.getInt("activityRetry"));
////                            apiResponse.setCompensationRetryCount(objects.getInt("compensationRetry"));
////                            apiResponse.setActivityRetryDoneCount(0);
////                            apiResponse.setCompensationRetryDoneCount(0);
//                                apiResponse.setRetriable(true);
//                                worflowService.saveResponse(apiResponse);
//                            }
//                        }
//                    }
//                }
//
//            } catch (Exception e) {
//                LOG.log(Level.INFO, " [ {0} ] SERVICE DOWN TRANSACTION FAILED ON API...  ->  http://TX_SERVICE_HOST_URL:PORT/{1} - COMPENSATION PROCESS WILL BE EXECUTED...", new Object[]{workflowName, getMicroServiceName(url)});
//                System.out.println(e.getMessage());
//                status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
//            }
//        } else {
//            ListIterator<APIResponse> activeTxList = worflowService.getActiveRetryData(lraIdUrl).listIterator();
//            //  ListIterator<APIResponse> compensateTxList = worflowService.getCompensationData(lraIdUrl).listIterator();
//
//            String sagaName = "";
//            try {
//                String compensationData;
//                status = 500;
//
//                while (activeTxList.hasNext()) {
//                    APIResponse apiResponse = activeTxList.next();
//                    compensationData = apiResponse.getResponseData();
//                    sagaName = apiResponse.getSagaName();
//                    url = apiResponse.getActivityUrl();
//                    JSONObject json = new JSONObject(compensationData);
//                    int activityRetryCount = apiResponse.getActivityRetryDoneCount();
//                    int compensationRetryCount = apiResponse.getCompensationRetryDoneCount();
//                    apiResponse.setActivityRetryDoneCount(++activityRetryCount);
//                    System.out.println(" Invoking Retriable Endpoint " + url + "  Retry Count = " + apiResponse.getActivityRetryCount() + " Current Retry Done Count = " + apiResponse.getActivityRetryDoneCount());
//
//                    worflowService.updateRetryStatus(apiResponse.getId(), apiResponse);
//                    //   if(apiResponse.get)
////                    LOG.log(Level.INFO, " [ {0} ] INVOKING COMPENSATION-ENDPOINT -> http://ROLLBACK_SERVICE_HOST_URL:PORT/{2} -> ENTITY -> {1}  ", new Object[]{sagaName, json.get("entity_id"), getMicroServiceName(apiResponse.getCompensationUrl())});
////
////                    rollbackTarget = rollbackClient.target(apiResponse.getCompensationUrl());
////                    Response rollbackResponse = rollbackTarget.request().put(Entity.entity(json.get("entity_id"), MediaType.APPLICATION_JSON_TYPE));
////                    status = rollbackResponse.getStatus();
//                    LOG.log(Level.INFO, " [ {0} ] ROLLBACK API RESPONSE ->  [ {1} ] ", new Object[]{sagaName, status == 200 ? "SUCCESS" : "FAILED"});
//                    //if (status == 200) {
//
//                    if (apiResponse.getActivityRetryDoneCount() == apiResponse.getActivityRetryCount()) {
//                        worflowService.updateStatus(apiResponse.getId(), "COMPLETED");
//                    }
//                }
//
//            } catch (Exception e) {
//                LOG.log(Level.INFO, " [ {0} ] SERVICE DOWN TRANSACTION FAILED ON API...  -> {TX_SERVICE_HOST_URL}/{1} - RETRY PROCESS WILL BE EXECUTED...", new Object[]{sagaName, getMicroServiceName(url)});
//                status = 500;
//            }
//
//            if (status == 200)
//                return Response.ok().entity(ParticipantStatus.Compensated.name()).build();
//            else
//                return Response.status(500).build();
//        }
//        return Response.status(status).build();
//    }

    @Transactional
    public Response compensateTx(String lraIdUrl) {
        ListIterator<APIResponse> compensateTxList = worflowService.getCompensationData(lraIdUrl).listIterator();
        int status = 200;
        String sagaName = "", url = "";

        try {
            String compensationData;
            while (compensateTxList.hasNext()) {
                APIResponse apiResponse = compensateTxList.next();
                compensationData = apiResponse.getResponseData();
                sagaName = apiResponse.getSagaName();
                url = apiResponse.getCompensationUrl();
                JSONObject json = new JSONObject(compensationData);
                LOG.log(Level.INFO, " [ {0} ] INVOKING COMPENSATION-ENDPOINT -> http://ROLLBACK_SERVICE_HOST_URL:PORT/{2} -> ENTITY -> {1}  ", new Object[]{sagaName, json.get("entity_id"), getMicroServiceName(apiResponse.getCompensationUrl())});

                rollbackTarget = rollbackClient.target(apiResponse.getCompensationUrl());
                Response rollbackResponse = rollbackTarget.request().put(Entity.entity(json.get("entity_id"), MediaType.APPLICATION_JSON_TYPE));
                status = rollbackResponse.getStatus();
                LOG.log(Level.INFO, " [ {0} ] ROLLBACK API RESPONSE ->  [ {1} ] ", new Object[]{sagaName, status == 200 ? "SUCCESS" : "FAILED"});
                if (status == 200) {
                    worflowService.updateStatus(apiResponse.getId(), "COMPLETED");
                }
            }


        } catch (Exception e) {
            LOG.log(Level.INFO, " [ {0} ] SERVICE DOWN TRANSACTION FAILED ON API...  -> {TX_SERVICE_HOST_URL}/{1} - RETRY PROCESS WILL BE EXECUTED...", new Object[]{sagaName, getMicroServiceName(url)});
            status = 500;
        }

        if (status == 200)
            return Response.ok().entity(ParticipantStatus.Compensated.name()).build();
        else
            return Response.status(500).build();


    }


    private static Pattern pattern = Pattern.compile(".*/([^/#|?]*)(#.*|$)");

    public static String getMicroServiceName(String url) {
        Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }


}


