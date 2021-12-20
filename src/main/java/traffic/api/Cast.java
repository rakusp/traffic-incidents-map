package traffic.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Cast {
    public static List<Incident> cast(JSONArray jsonList) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        List<Incident> incidents = new ArrayList<>();

        for (int i = 0; i < jsonList.length(); i++) {
            JSONObject jsonObject = jsonList.getJSONObject(i);
            String json = jsonObject.toString(2);
            Incident incident = mapper.readValue(json, Incident.class);
            incidents.add(incident);
        }
        return incidents;
    }
}
