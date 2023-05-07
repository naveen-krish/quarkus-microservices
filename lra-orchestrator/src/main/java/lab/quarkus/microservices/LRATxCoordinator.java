package lab.quarkus.microservices;

import lab.quarkus.microservices.entity.SagaStatus;
import lab.quarkus.microservices.entity.Status;
import lab.quarkus.microservices.entity.Workflow;
import lab.quarkus.microservices.service.TxService;
import lab.quarkus.microservices.service.WorkflowService;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

@Path("/saga")
public class LRATxCoordinator {

    private static final Logger LOG = Logger.getLogger(LRATxCoordinator.class.getName());

    @Inject
    TxService txService;
    @Inject
    WorkflowService workflowService;
    String sagaName = "MicroServiceSaga";



    @POST
   @Path("/register")
    // public String register(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraIdUrl, String workflowSteps) {
    public String register(String workflowSteps) {

        LOG.log(Level.INFO, "Saga Registered -> {0} ", new Object[]{workflowSteps});
        JSONObject json = new JSONObject(workflowSteps);
        Workflow workflow = new Workflow();
        workflow.setWorkflowSteps(json.toString());
        workflow.setWorkflowName(String.valueOf(json.get("sagaName")));
        workflowService.createWorkflow(workflow);
        workflowService.insertActivityDetails(workflowSteps,workflow.getWorkflowName());
        return String.valueOf(Response.ok(String.valueOf(json.get("sagaName"))).build());
    }


    @POST
    @LRA(value = LRA.Type.REQUIRES_NEW,

            cancelOn = {
                    Response.Status.INTERNAL_SERVER_ERROR // cancel on a 500 code
            },
            cancelOnFamily = {
                    Response.Status.Family.CLIENT_ERROR // cancel on any 4xx code
            })
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})

    @Path("/initiate")
    public Response initiateTx(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraIdUrl, Map payload) {
        LOG.log(Level.INFO, " RECEIVED DETAILS FOR SAGA -> {0} Tx-ID [ {1} ] ", new Object[]{payload, getTxId(lraIdUrl)});
        Status sagaStatus = workflowService.workflowStatus(getTxId(lraIdUrl));
        if( sagaStatus != null) {
          //  System.out.println(" InvokeTx SagaStatus -> " + sagaStatus.getSagaStatus()+ " -> "+getTxId(lraIdUrl));
        }else{
            LOG.log(Level.INFO," INITIATING TRANSACTION SAGA -> {0} TX-ID -> {1} ", new Object[]{sagaName, getTxId(lraIdUrl)});
            Status newSagaStatus = new Status();
            newSagaStatus.setLraIdUrl(getTxId(lraIdUrl));
            newSagaStatus.setSagaStatus(SagaStatus.SAGA_EXECUTING.name());
            newSagaStatus.setWorkflowName(sagaName);
            workflowService.createSagaStatus(newSagaStatus);
        }
        return txService.invokeTx(getTxId(lraIdUrl), payload);
    }

    @PUT
    @Path("/complete")
    @Complete
    public Response completeTx(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraIdUrl) {
        System.out.println();
        LOG.log(Level.INFO, " TRANSACTION COMPLETED... ->  [ {0} ]  ", getTxId(lraIdUrl));
        return Response.ok("Completed").build();
    }

    private static Pattern pattern = Pattern.compile("/lra-coordinator/([^/]+)");

    public static String getTxId(String url) {
        Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    @PUT
    @LRA(value = LRA.Type.REQUIRED)
    @Path("/compensate")
    @Compensate
    public Response compensateTx(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraIdUrl) {

    //    LOG.log(Level.INFO, " INITIATING COMPENSATING TRANSACTION... [ {0} ] ", getTxId(lraIdUrl));
        Map payLoadMap = new HashMap();
        String lra = getTxId(lraIdUrl);

        Status sagaStatus = workflowService.workflowStatus(getTxId(lraIdUrl));
        if( sagaStatus != null) {
         //   System.out.println(" Compensate SagaStatus -> " + sagaStatus.getSagaStatus()+ " -> "+getTxId(lraIdUrl));
        }else{
           // System.out.println(" CompensateTx Creating SagaStatus -> "+SagaStatus.SAGA_EXECUTING.name()+ " -> "+getTxId(lraIdUrl));
            Status newSagaStatus = new Status();
            newSagaStatus.setLraIdUrl(getTxId(lraIdUrl));
            newSagaStatus.setSagaStatus(SagaStatus.SAGA_EXECUTING.name());
            newSagaStatus.setWorkflowName(sagaName);
            workflowService.createSagaStatus(newSagaStatus);
       }
        String payload = workflowService.getWorkflowByName(sagaName).getPayload();
        payLoadMap.put("Saga_Name","AnagrafeSaga");
        payLoadMap.put("Tx_payload",payload);
        return txService.invokeTx(getTxId(lraIdUrl),payLoadMap);
      //  return txService.compensateTx(getTxId(lraIdUrl));

    }

}
