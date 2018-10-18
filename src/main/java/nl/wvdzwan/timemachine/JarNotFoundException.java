package nl.wvdzwan.timemachine;

public class JarNotFoundException extends Exception {

    private final String groupId;
    private final String artifactId;
    private final String version;


    public JarNotFoundException(String message, String groupId, String artifactId, String version) {
        super(message);
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }
}
