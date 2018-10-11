package nl.wvdzwan.timemachine.libio;

import nl.wvdzwan.timemachine.HttpClientInterface;
import nl.wvdzwan.timemachine.libio.Project;

import java.io.IOException;

public class LibrariesIOClient implements LibrariesIOInterface {

    private String api_key;
    private HttpClientInterface client = null;

    public LibrariesIOClient(String api_key, HttpClientInterface httpClient) {
        this.api_key = api_key;
        this.client = httpClient;
    }

    @Override
    public Project getProjectInfo(String identifier) {

        String data = "";
        try {
            data = client.get(String.format("https://libraries.io/api/maven/%s?api_key=%s", identifier, api_key));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Project.fromJson(data);
    }

}
