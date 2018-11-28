package nl.wvdzwan.timemachine.libio;

public class ApiConnectionParameters {

    private String apiKey;
    private String baseUrl;

    public ApiConnectionParameters(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
