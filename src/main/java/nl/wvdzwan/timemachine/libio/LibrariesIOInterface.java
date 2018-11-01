package nl.wvdzwan.timemachine.libio;

public interface LibrariesIOInterface {
    Project getProjectInfo(String identifier);

    void setApiKey(String apiKey);
}
