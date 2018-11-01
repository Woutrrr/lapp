package nl.wvdzwan.timemachine.libio;

import nl.wvdzwan.timemachine.HttpClientInterface;
import nl.wvdzwan.timemachine.libio.Project;
import org.eclipse.aether.spi.locator.Service;
import org.eclipse.aether.spi.locator.ServiceLocator;

import java.io.IOException;

public class LibrariesIOClient implements LibrariesIOInterface, Service {

    private String api_key;
    private HttpClientInterface client = null;



    public LibrariesIOClient() {
        // Enable default constructor
    }

    public LibrariesIOClient(String api_key, HttpClientInterface httpClient) {
        setApiKey(api_key);
        setClient(httpClient);
    }

    public void setApiKey(String apiKey) {
        this.api_key = apiKey;
    }

    public void setClient(HttpClientInterface httpClient) {
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


    @Override
    public void initService(ServiceLocator locator) {
        client = locator.getService(HttpClientInterface.class);
    }

}
