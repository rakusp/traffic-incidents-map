package traffic.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.*;
import java.util.concurrent.CompletableFuture;
import org.json.*;

public class GetData {
    public static void Get(double minLon, double minLat, double maxLon, double maxLat) {
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
                    URLEncoder.encode("fields={incidents{type,geometry{type,coordinates}," +
                            "properties{id,iconCategory,magnitudeOfDelay,events{description,code}," +
                            "startTime,endTime,from,to,length,delay,roadNumbers,aci{probabilityOfOccurrence," +
                            "numberOfReports,lastReportTime}}}}", "UTF-8");

            System.out.println("URL: " + uri);

            HttpRequest request = HttpRequest.newBuilder(new URI(uri)).build();
            HttpResponse<String> response = HttpClient.newBuilder()
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response code: " + response.statusCode());

            // Tworzenie JSONA i dostawanie się do obiektów
            // https://stackoverflow.com/questions/2591098/how-to-parse-json-in-java
            JSONObject obj = new JSONObject(response.body());
            System.out.println("Body: ");
            System.out.println(obj.toString(2));

            System.out.println(obj.getJSONArray("incidents"));

        } catch (URISyntaxException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
