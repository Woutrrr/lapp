package nl.wvdzwan.timemachine.libio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class Project {

    protected String name;
    protected String platform;
    protected String description;

    protected ArrayList<VersionDate> versions;

    public String getName() {
        return name;
    }

    public String getPlatform() {
        return platform;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<VersionDate> getVersions() {
        return versions;
    }


    public static Project fromJson(String json) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(
                        LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json_snippet, type, jsonDeserializationContext) ->
                                ZonedDateTime.parse(json_snippet.getAsJsonPrimitive().getAsString()).toLocalDateTime())
                .create();

        return gson.fromJson(json, Project.class);
    }


}
