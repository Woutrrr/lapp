package nl.wvdzwan.librariesio;

import java.io.IOException;

import org.eclipse.aether.spi.locator.Service;
import org.eclipse.aether.spi.locator.ServiceLocator;

public class LibrariesIoClient implements LibrariesIoInterface, Service {

    private String apiKey;
    private String baseUrl = "https://libraries.io/api/";
    private HttpClientInterface client;

    public LibrariesIoClient() {
        // Enable default constructor for use with ServiceLocator
    }

    public LibrariesIoClient(String apiKey, HttpClientInterface httpClient) {
        setApiKey(apiKey);
        setClient(httpClient);
    }

    @Override
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setClient(HttpClientInterface httpClient) {
        this.client = httpClient;
    }

    @Override
    public Project getProjectInfo(String identifier) {

        String data = "";
        try {
            data = client.get(String.format("%smaven/%s?apiKey=%s", baseUrl, identifier, apiKey));
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
