package lab.quarkus.microservices;

import it.sella.microservices.lra.ForwardingTransaction;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;

import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

@Path("/transaction")
public class LRAResource {

    SagaWorkFlowSteps[] sagaWorkFlowSteps = null;

    @Inject
    WorkflowService worflowService;

    @POST
    @Path("/register/saga/workflow")
    //@LRA // Step 2b: The method should run within an LRA
    @Produces(MediaType.TEXT_PLAIN)
    /* Step 2c the context is useful for associating compensation logic */
    public String register(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId, String workflowSteps) {

        JSONObject json = new JSONObject(workflowSteps);
        Workflow workflow = new Workflow();
        workflow.setWorkflowSteps(json.toString());
        workflow.setWorkflowName(String.valueOf(json.get("sagaName")));
        worflowService.createWorkflow(workflow);
        return String.valueOf(worflowService.getWorkflowByName(String.valueOf(json.get("sagaName"))).getId());
    }

    @POST
    @Path("/initiate")
    @LRA(value = LRA.Type.REQUIRED,
            cancelOn = {
                    Response.Status.INTERNAL_SERVER_ERROR // cancel on a 500 code
            })// Step 2b: The method should run within an LRA
    @Produces(MediaType.TEXT_PLAIN)
    /* Step 2c the context is useful for associating compensation logic */
    public String startTx(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId, Map payLoadMap) throws Exception{

        String workflowName = String.valueOf(payLoadMap.get("Saga_Name"));

        System.out.println(" Received workflowName -> " + workflowName);

        String workflow = worflowService.getWorkflowByName(workflowName).getWorkflowSteps();
        System.out.println("  Workflow Steps from DB  -> "+workflow);
        Map<String, ForwardingTransaction> sagaSteps = SagaBuilder.createSaga(workflow).getSagaSteps();

        worflowService.invokeAnagrafe(String.valueOf(payLoadMap.get("Tx_payload")));
//        for (String activityKey : sagaSteps.keySet()) {
//            System.out.println(activityKey + " = " + sagaSteps.get(activityKey));
//
//            if (activityKey.contains("Activity")) {
//                try {
//                    System.out.println(" Invoking Tx -> " + activityKey);
//                    ForwardingTransaction forwardingTransaction = sagaSteps.get(activityKey);
//                    forwardingTransaction.invokeTx(String.valueOf(payLoadMap.get("Tx_payload")));
//                } catch (Exception e) {
//                  System.out.println(e.getMessage());
//                }
//            }
//        }

//        sagaWorkFlowSteps = new SagaWorkFlowSteps[0];
//        try {
//            sagaWorkFlowSteps = mapper.readValue(jsonarray.toString(), SagaWorkFlowSteps[].class);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//
//        for (SagaWorkFlowSteps sagaWorkFlowStep : sagaWorkFlowSteps) {
//            //  sagaWorkFlowStep.setSaga(sagaName);
//            System.out.println(" Saga Registration -> " + sagaWorkFlowStep.toString());
//        }
        System.out.printf("LRA completed with context %s%n", lraId);
        return String.valueOf(Response.ok(lraId.toASCIIString()).build());
    }

    // Step 2d: There must be a method to compensate for the action if it's cancelled
    @PUT
    @Path("compensate")
    @Compensate
    public Response compensateWork(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        System.out.printf("compensating %s%n", lraId);
        return Response.ok(lraId.toASCIIString()).build();
    }

    // Step 2e: An optional callback notifying that the LRA is closing
    @PUT
    @Path("complete")
    @Complete
    public Response completeWork(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
//        for (SagaWorkFlowSteps sagaWorkFlowStep : sagaWorkFlowSteps) {
//            System.out.println(" Compensating API ->" + sagaWorkFlowStep.getCompensationActivityName());
//        }
        System.out.printf("completing %s%n", lraId);
        return Response.ok(lraId.toASCIIString()).build();
    }

    @GET
    @Path("/start")
    @LRA(end = false) // Step 3a: The method should run within an LRA
    @Produces(MediaType.TEXT_PLAIN)
    public String start(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        System.out.printf("hello with context %s%n", lraId);
        return lraId.toASCIIString();
    }
}