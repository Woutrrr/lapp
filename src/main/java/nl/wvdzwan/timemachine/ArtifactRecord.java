package nl.wvdzwan.timemachine;

public class ArtifactRecord {

    private String groupId;
    private String artifactId;
    private String version = "";

    public ArtifactRecord(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public ArtifactRecord(String identifier) {
        String[] parts = identifier.split(":");


        this.groupId = parts[0];
        this.artifactId = parts[1];

        if (parts.length == 3) {
            this.version = parts[2];
        }
    }

    public void setVersion(String version) { this.version = version; }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getJarName() {
        return artifactId + "-" + version + ".jar";
    }

    public String getUnversionedIdentifier() {
        return String.format("%s:%s", groupId, artifactId);
    }

    public String getIdentifier() {
        return String.format("%s:%s:%s", groupId, artifactId, version);
    }
}