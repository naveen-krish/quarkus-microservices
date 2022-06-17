package lab.quarkus.microservices;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Test {

    public static void main(String[] args) {

     String json = "{\n" +
             "\t\"sagaName\": \"test\",\n" +
             "\t\"saga\": [{\n" +
             "\t\t\t\"activityName\": \"createCustomer\",\n" +
             "\t\t\t\"compensationActivityName\": \"rollbackCustomer\",\n" +
             "\t\t\t\"activityClass\": \"it.sella.microservices.delegate.AnagrafeActivity\",\n" +
             "\t\t\t\"compensationClass\": \"it.sella.microservices.delegate.AnagrafeCompensation\"\n" +
             "\t\t},\n" +
             "\t\t{\n" +
             "\t\t\t\"activityName\": \"createAddress\",\n" +
             "\t\t\t\"compensationActivityName\": \"rollbackAddress\",\n" +
             "\t\t\t\"activityClass\": \"it.sella.microservices.delegate.AddressActivity\",\n" +
             "\t\t\t\"compensationClass\": \"it.sella.microservices.delegate.AddressCompensation\"\n" +
             "\t\t},\n" +
             "\t\t{\n" +
             "\t\t\t\"activityName\": \"createAccount\",\n" +
             "\t\t\t\"compensationActivityName\": \"rollbackAccount\",\n" +
             "\t\t\t\"activityClass\": \"it.sella.microservices.delegate.AccountActivity\",\n" +
             "\t\t\t\"compensationClass\": \"it.sella.microservices.delegate.AccountCompensation\"\n" +
             "\t\t}\n" +
             "\t]\n" +
             "\n" +
             "}";

        JSONObject jsonObj = new JSONObject(json);
        System.out.println("Json Obj -> "+ jsonObj.get("sagaName"));

    }


}
