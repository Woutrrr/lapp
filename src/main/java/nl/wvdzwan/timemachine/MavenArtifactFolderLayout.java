package nl.wvdzwan.timemachine;

public class MavenArtifactFolderLayout implements ArtifactFolderLayout {
    private String root;

    public MavenArtifactFolderLayout(String root) {
        this.root = root;
    }

    /**
     * Build ArtifactRecord from repository layout path
     *
     * @param path Path following repository layout format
     * @return parsed ArtifactRecord
     */
    @Override
    public ArtifactRecord artifactRecordFromPath(String path) {
        if (root != null && path.startsWith(root)) {
            path = path.substring(root.length());
        }

        int fileNameIndex = path.lastIndexOf("/");
        int versionIndex = path.lastIndexOf("/", fileNameIndex-1);
        int artifactIndex = path.lastIndexOf("/", versionIndex-1);

        String groupID = path.substring(0, artifactIndex).replace('/', '.');
        String artifactID = path.substring(artifactIndex+1, versionIndex);
        String version = path.substring(versionIndex+1, fileNameIndex);

        return new ArtifactRecord(groupID, artifactID, version);
    }
}
