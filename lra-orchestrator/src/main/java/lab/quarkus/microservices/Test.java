package lab.quarkus.microservices;

import lab.quarkus.microservices.entity.ForwardActivity;
import lab.quarkus.microservices.entity.SagaStatus;
import lab.quarkus.microservices.entity.Status;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

//    .*/([^/#|?]*)(#.*|$)
    private static Pattern pattern = Pattern.compile(".*/([^/#|?]*)(#.*|$)");
            //Pattern.compile("/lra-coordinator/([^/]+)");
    public static String extractAccountIdFromURL(String url) {
        Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }
    public static void main(String[] args) {
//        String examples = "http://TX_SERVICE_HOST_URL:PORT/createAddress";
//                //"http://localhost:50000/lra-coordinator/0_ffffc0a80102_c6d4_6294c1fe_296";
//
//        for (String url : examples.split("\\R")){
//            System.out.println(getMicroServiceName(url));
//        }
//
//        System.out.println(SagaStatus.SAGA_COMPLETED.name());
        compareLists();

    }

    private static Pattern pattern1 = Pattern.compile(".*/([^/#|?]*)(#.*|$)");

    public static String getMicroServiceName(String url) {
        Matcher matcher = pattern1.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    public static void compareLists() {

        Set<String> lraIdUrl = new HashSet<>();

        ForwardActivity activtity = new ForwardActivity();
        activtity.setSagaName("anagrafe");
        activtity.setLraIdUrl("xyz");

        ForwardActivity activtity1 = new ForwardActivity();
        activtity1.setSagaName("anagrafe");
        activtity1.setLraIdUrl("xyzxx");

        ForwardActivity activtity2 = new ForwardActivity();
        activtity2.setSagaName("anagrafe");
        activtity2.setLraIdUrl("xyzzz");

        Status status = new Status();
        status.setWorkflowName("anagrafe");
        status.setLraIdUrl("xyz");
        status.setSagaStatus("Active");

        Status status1 = new Status();
        status1.setWorkflowName("anagrafe");
        status1.setLraIdUrl("xyzz");
        status1.setSagaStatus("Cancelled");


        List<ForwardActivity> forwardActivityList =  Arrays.asList(activtity,activtity1,activtity2);
        List<Status> statusList = Arrays.asList(status,status1);

        for (ForwardActivity at : forwardActivityList) {
            lraIdUrl.add(at.getLraIdUrl());
        }
//        lraIdUrl.forEach(System.out::println);
//        forwardActivityList.forEach(System.out::println);

        for (Status st : statusList) {
            if(lraIdUrl.contains(st.getLraIdUrl()))
                System.out.println(" Matching LRAID -> "+st.getLraIdUrl()+" -> "+st.getSagaStatus());

        }

//        if(statusList.size() > forwardActivityList.size()) {
//            for (Status st : statusList) {
//                for (ForwardActivity activity : forwardActivityList) {
//
//                    if (activity.getLraIdUrl().equals(st.getLraIdUrl())) {
//                        System.out.println(" Matching LRAID -> "+activity.getLraIdUrl());
//                    }
//                }
//
//            }
//        }else{
//            for(ForwardActivity activity:forwardActivityList){
//                for(Status st:statusList){
//                    if(st.getLraIdUrl().equals(activity.getLraIdUrl())){
//                        System.out.println(" Matching LRAID -> "+activity.getLraIdUrl());
//
//                    }
//                }
//            }
//        }

    }
}
