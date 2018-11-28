package nl.wvdzwan.librariesio;

/**
 * Interface to interact with libraries.io like api's.
 */
public interface LibrariesIoInterface {

    /**
     * Retrieve project information from libraries.io for an artifact.
     * @param identifier the artifact to load information for
     * @return parsed project information
     */
    Project getProjectInfo(String identifier);

    /**
     * Set api key to use for authentication.
     * @param apiKey the key to authenticate with
     */
    void setApiKey(String apiKey);

    /**
     * Set custom base url e.g. to a local mirror or another source that supports the libraries.io api.
     * @param baseUrl the base url where the api can be found
     */
    void setBaseUrl(String baseUrl);
}
