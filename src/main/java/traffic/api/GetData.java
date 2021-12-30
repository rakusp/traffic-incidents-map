package traffic.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.json.*;

public class GetData {
    public static List<Incident> get(double minLon, double minLat, double maxLon, double maxLat) {
        try {
            // Tymczasowo tak ale te klucze wypada jakoś chronić potem xD
            String APIkey = "aTNN9zA6QOTb4DGHuRWUpQGEk8KmE91p";
            String bbox = minLon + "," + minLat + "," + maxLon + "," + maxLat;
            System.out.println("Coordinates: " + bbox);

            // https://developer.tomtom.com/traffic-api/traffic-api-documentation-traffic-incidents/incident-details#request-data
            // Na tej stronie jest jak URL robić
            String uri = "https://api.tomtom.com/traffic/services/5/" +
                    "incidentDetails?" +
                    "key=" + APIkey + "&" +
                    "bbox=" + bbox + "&" +
                    "fields=" +
                    URLEncoder.encode("{incidents{type,geometry{type,coordinates}," +
                            "properties{id,iconCategory,magnitudeOfDelay,events{description,code,iconCategory}," +
                            "startTime,endTime,from,to,length,delay,roadNumbers,timeValidity," +
                            "aci{probabilityOfOccurrence,numberOfReports,lastReportTime}," +
                            "tmc{countryCode,tableNumber,tableVersion,direction,points{location,offset}}}}}", StandardCharsets.UTF_8) + "&" +
                    "categoryFilter=0,1,2,3,4,5,6,7,8,9,10,11,14" + "&" +
                    "timeValidityFilter=present,future";

            System.out.println("URL: " + uri);

            HttpRequest request = HttpRequest.newBuilder(new URI(uri)).build();
            HttpResponse<String> response = HttpClient.newBuilder()
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response code: " + response.statusCode());

            // Tworzenie JSONA i dostawanie się do obiektów
            // https://stackoverflow.com/questions/2591098/how-to-parse-json-in-java
            JSONObject obj = new JSONObject(response.body());
            JSONArray jsonList = obj.getJSONArray("incidents");
            return Json.cast(jsonList);


        } catch (URISyntaxException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
