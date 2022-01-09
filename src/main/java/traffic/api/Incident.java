package traffic.api;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Incident {
    @JsonProperty
    public Geometry geometry;
    @JsonProperty
    public String type;
    @JsonProperty
    public Properties properties;


    public static class Geometry {
        @JsonProperty
        public Double[][] coordinates;
        @JsonProperty
        public String type;

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
        public String id;
        @JsonProperty
        public List<String> roadNumbers;
        @JsonProperty
        public String timeValidity;
        @JsonProperty
        public LocalDateTime startTime;
        @JsonProperty
        public String from;
        @JsonProperty
        public LocalDateTime endTime;
        @JsonProperty
        public String to;
        @JsonProperty
        public Events[] events;

        public static class Events {
            @JsonProperty
            public Integer code;
            @JsonProperty
            public String description;
            @JsonProperty
            public Integer iconCategory;

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
                    "id=" + id +
                    ", roadNumbers=" + roadNumbers +
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
