package nl.wvdzwan.timemachine;

import org.apache.maven.model.Dependency;

import java.util.Objects;

public class ArtifactRecord {

    private String groupId;
    private String artifactId;
    private String version;

    public ArtifactRecord(String groupId, String artifactId, String version) {

        this.groupId = Objects.requireNonNull(groupId, "groupId must not be null");
        this.artifactId = Objects.requireNonNull(artifactId, "artifactId must not be null");
        this.version = version;
    }

    public ArtifactRecord(String identifier) {
        Objects.requireNonNull(identifier);

        if (!isValidIdentifier(identifier)) {
            throw new IllegalArgumentException("Malformed identifier string");
        }

        String[] parts = identifier.split(":");

        this.groupId = parts[0];
        this.artifactId = parts[1];

        if (parts.length == 3) {
            this.version = parts[2];
        }
    }

    public void setVersion(String version) {
        this.version = version;
    }

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

    public static boolean isValidIdentifier(String identifier) {
        if (identifier == null) {
            return false;
        }

        String[] parts = identifier.split(":", 3);

        if (parts.length < 2) {
            return false;
        }

        if (parts[0].length() == 0
                || parts[1].length() == 0
                || (parts.length == 3 && parts[2].length() == 0)) {
            return false;
        }

        return true;
    }
}