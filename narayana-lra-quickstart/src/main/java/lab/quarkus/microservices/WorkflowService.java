package lab.quarkus.microservices;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.transaction.Transactional;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@ApplicationScoped
public class WorkflowService {

    private final WorkflowRepository workflowRepository;

    @Inject
    public WorkflowService(WorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }


    public Workflow getWorkflowByName(String name)  {
        System.out.println(" Finding workflow for Saga -> "+name);
       return workflowRepository.findByName(name);

    }
@Transactional
    public void  createWorkflow(Workflow workflow)  {
         workflowRepository.persist(workflow);

    }

    @Transactional
    public Response invokeAnagrafe(String payload)throws Exception{

        try {
            System.out.println(" Invoking Anagrafe ");
            ClientBuilder.newClient()
                    .target("http://localhost:8081/anagrafe")
                    .path("/createCustomer")
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .rx()
                    .post(Entity.entity(payload, MediaType.APPLICATION_JSON))
                    .whenComplete((res, t) -> {
                        if (res != null) {
                            System.out.println(res.getStatus() + " " + res.getStatusInfo().getReasonPhrase());
                            res.close();
                        }
                    });

            Response response =  ClientBuilder.newClient()
                    .target("http://localhost:8083/address")
                    .path("/createAddress")
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(payload, MediaType.APPLICATION_JSON));

            System.out.println(" Adress response -> "+response.getStatus());
            return Response.ok().status(200).build();


        }catch (Exception e) {
            System.out.println(" Workflow Invocation Error -> "+e.getMessage());
            return error(500,e.getMessage());
        }
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
