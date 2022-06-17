package lab.quarkus.microservices;

import it.sella.microservices.delegate.SagaActivity;
import it.sella.microservices.lra.CompensatingTransaction;
import it.sella.microservices.lra.ForwardingTransaction;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class SagaBuilder {

    private String workflowSteps;

    Map<String, ForwardingTransaction> sagaSteps = new LinkedHashMap<String, ForwardingTransaction>();

    public SagaBuilder(String workflowSteps) {
        this.workflowSteps = workflowSteps;
    }

    public static SagaBuilder createSaga(String workflowSteps) {
        SagaBuilder builder = new SagaBuilder(workflowSteps);
        return builder.start();
    }

    public SagaBuilder start() {
        JSONObject json = new JSONObject(workflowSteps);
        JSONArray jsonarray = json.getJSONArray("saga");
        ForwardingTransaction activityClass = null;
        CompensatingTransaction compensatingTransaction = null;
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject objects = jsonarray.getJSONObject(i);
            Iterator key = objects.keys();
            while (key.hasNext()) {
                String activityKey = key.next().toString();
                try {
                    if (activityKey.contains("ActivityClass") ) {
                        activityClass = (ForwardingTransaction) Class.forName(objects.getString(activityKey)).newInstance();
                        System.out.println(" SagaBuilder Adding ActivityClass  -> "+activityClass);
                        sagaSteps.put(activityKey, activityClass);
                    }
//                    }else{
//                        compensatingTransaction = (CompensatingTransaction) Class.forName(objects.getString(activityKey)).newInstance();
//                        sagaSteps.put(activityKey, compensatingTransaction);
//                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return this;
    }
}
