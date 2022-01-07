package traffic.api;

import java.time.LocalDateTime;
import java.util.List;

public class TestAPI {
    public static void main(String[] args) {

//        List<Incident> incidents = GetData.get(21.0, 52.0, 21.8, 52.8);
        List<Incident> incidents = GetData.get(20, 51.5, 21.5, 53);
        System.out.println(Filter.byIncidentDescription("Roadworks", incidents));
        System.out.println(Filter.endsBefore(LocalDateTime.of(2021, 12, 31, 0, 0), incidents));
        System.out.println(Filter.stillTakingPlace(incidents));
        System.out.println(Filter.incidentsOnRoad("631", incidents));
        System.out.println(Filter.incidentsAfterGivenTime(1, 1, 1, 1, 1, incidents));
    }
}

