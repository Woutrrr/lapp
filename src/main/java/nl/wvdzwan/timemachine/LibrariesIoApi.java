package nl.wvdzwan.timemachine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import nl.wvdzwan.timemachine.libio.Project;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class LibrariesIoApi {

    private String api_key;
    private HttpClient client = null;

    public LibrariesIoApi(String api_key) {
        this.api_key = api_key;
    }

    public Project getProjectInfo(String identifier) {

        String data = "";
        try {
            data = getClient().get(String.format("https://libraries.io/api/maven/%s?api_key=%s", identifier, api_key));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(
                        LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json_snippet, type, jsonDeserializationContext) ->
                                ZonedDateTime.parse(json_snippet.getAsJsonPrimitive().getAsString()).toLocalDateTime())
                .create();

        return gson.fromJson(data, Project.class);
    }


    private HttpClient getClient() {
        if (this.client == null) {
            this.client = new HttpClient();
        }

        return this.client;
    }

}
