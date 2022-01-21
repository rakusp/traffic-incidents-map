package traffic.api;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Filter {


    public static List<Incident> exceptIncidentWithType(Integer incidentType, List<Incident> incidents) {

        return incidents.stream()
                .filter(i -> !(i.properties.events[0].iconCategory.equals(incidentType)))
                .collect(Collectors.toList());
    }

    public static List<Incident> endsBefore(LocalDateTime deadLine, List<Incident> incidents) {

        List<Incident> searchedIncidents = new ArrayList<>();

        for (Incident i : incidents) {
            if ((i.properties.endTime != null && i.properties.endTime.isBefore(deadLine))) {
                searchedIncidents.add(i);
            }
        }
        return searchedIncidents;
    }

    public static List<Incident> stillTakingPlace(List<Incident> incidents) {

        List<Incident> searchedIncidents = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        for (Incident i : incidents) {
            LocalDateTime start = i.properties.startTime;
            LocalDateTime end = i.properties.endTime;
            if (end == null || (start != null && start.isBefore(now) && end.isAfter(now))) {
                searchedIncidents.add(i);
            }
        }
        return searchedIncidents;
    }

    public static List<Incident> incidentsOnRoad(String roadNumber, List<Incident> incidents) {

        List<Incident> searchedIncidents = new ArrayList<>();

        for (Incident i : incidents) {
            if (i.properties.roadNumbers.contains(roadNumber)) {
                searchedIncidents.add(i);
            }
        }
        return searchedIncidents;
    }

    public static List<Incident> incidentsAfterGivenTime(Integer years, Integer months, Integer days,
                                                         Integer hours, Integer minutes, List<Incident> incidents) {

        List<Incident> searchedIncidents = new ArrayList<>();
        LocalDateTime givenTime = LocalDateTime.now().plusYears(years).plusMonths(months).
                plusDays(days).plusHours(hours).plusMinutes(minutes);

        for (Incident i : incidents) {
            if (i.properties.endTime == null || i.properties.endTime.isAfter(givenTime)) {
                searchedIncidents.add(i);
            }
        }
        return searchedIncidents;
    }

}
