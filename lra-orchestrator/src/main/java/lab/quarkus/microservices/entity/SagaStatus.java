package lab.quarkus.microservices.entity;

public enum SagaStatus {

    SAGA_COMPLETED,SAGA_CANCELLED,SAGA_EXECUTING,ACTIVITY_COMPLETED;
//    COMPLETED("SAGA_COMPLETED"),
//    CANCELLED("SAGA_CANCELLED"),
//    EXECUTING("SAGA_EXECUTING");
//
//    private final String sagaStatus;
//
//    SagaStatus(String status){
//        sagaStatus = status;
//    }
}
