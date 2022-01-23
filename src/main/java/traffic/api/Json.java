package traffic.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Json {

    private static final ObjectMapper objectMapper = getObjectMapper();

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    private static JsonNode parse(String src) throws JsonProcessingException {
        return objectMapper.readTree(src);
    }

    private static <A> A fromJson(JsonNode node, Class<A> clazz) throws JsonProcessingException {
        return objectMapper.treeToValue(node, clazz);
    }

    private static String jsonToString(JSONObject jsonObject) {
        return jsonObject.toString(2);
    }

    public static List<Incident> cast(JSONArray jsonList) throws JsonProcessingException {

        List<Incident> incidents = new ArrayList<>();

        for (int i = 0; i < jsonList.length(); i++) {
            JSONObject jsonObject = jsonList.getJSONObject(i);
            JsonNode node = parse(jsonToString(jsonObject));
            Incident incident = fromJson(node, Incident.class);
            incidents.add(incident);
        }
        return incidents;
    }
}
