package traffic.api;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Incident {
    @JsonProperty
    Geometry geometry;
    @JsonProperty
    String type;
    @JsonProperty
    Properties properties;


    public static class Geometry {
        @JsonProperty
        Double[][] coordinates;
        @JsonProperty
        String type;

        @Override
        public String toString() {
            return "Geometry{" +
                    "coordinates=" + Arrays.deepToString(coordinates) +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    public static class Properties {
        @JsonProperty
        List<String> roadNumbers;
        @JsonProperty
        String timeValidity;
        @JsonProperty
        LocalDateTime startTime;
        @JsonProperty
        String from;
        @JsonProperty
        LocalDateTime endTime;
        @JsonProperty
        String to;
        @JsonProperty
        Events[] events;

        public static class Events{
            @JsonProperty
            Integer code;
            @JsonProperty
            String description;
            @JsonProperty
            Integer iconCategory;

            @Override
            public String toString() {
                return "Events{" +
                        "code=" + code +
                        ", description='" + description + '\'' +
                        ", iconCategory=" + iconCategory +
                        '}';
            }
        }


        @Override
        public String toString() {
            return "Properties{" +
                    "roadNumbers=" + roadNumbers +
                    ", timeValidity='" + timeValidity + '\'' +
                    ", startTime=" + startTime +
                    ", from='" + from + '\'' +
                    ", endTime=" + endTime +
                    ", to='" + to + '\'' +
                    ", events=" + Arrays.toString(events) +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Incident{" +
                "geometry=" + geometry +
                ", type='" + type + '\'' +
                ", properties=" + properties +
                '}';
    }

}
