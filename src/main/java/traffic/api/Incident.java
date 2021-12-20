package traffic.api;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Incident {
    @JsonProperty
    Geometry geometry;
    @JsonProperty
    String type;
    @JsonProperty
    Properties properties;


    public class Geometry {
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

    public class Properties {
        @JsonProperty
        Integer iconCategory;

        @Override
        public String toString() {
            return "Properties{" +
                    "iconCategory=" + iconCategory +
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
