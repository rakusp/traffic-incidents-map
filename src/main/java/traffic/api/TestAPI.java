package traffic.api;

import java.util.List;

public class TestAPI {
    public static void main(String[] args) {

        List<Incident> incidents = GetData.get(21.0, 52.0, 21.8, 52.8);
        System.out.println(incidents);
    }
}

