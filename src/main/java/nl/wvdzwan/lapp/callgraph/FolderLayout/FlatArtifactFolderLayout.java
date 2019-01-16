package nl.wvdzwan.lapp.callgraph.FolderLayout;

import java.util.jar.JarFile;
import java.util.regex.Pattern;

import nl.wvdzwan.lapp.callgraph.ArtifactRecord;

public class FlatArtifactFolderLayout implements ArtifactFolderLayout {

    private final Pattern pattern = Pattern.compile("\\d");

    /**
     * Build ArtifactRecord from path
     *
     * @param jarFile Jar file
     * @return parsed ArtifactRecord
     */
    @Override
    public ArtifactRecord artifactRecordFromJarFile(JarFile jarFile) {

        String path = jarFile.getName();

        int fileNameIndex = path.lastIndexOf("/");
        String fileName = path.substring(fileNameIndex + 1, path.length()-4);
        int versionIndex = findFirstDigit(fileName);

        if (versionIndex > -1) {
            String groupID = "";
            String artifactID = fileName.substring(0, versionIndex-1);
            String version = fileName.substring(versionIndex);

            return new ArtifactRecord(groupID, artifactID, version);
        }
        else {
            return new ArtifactRecord("", fileName, "UNKNOWN");
        }
    }

    private int findFirstDigit(String s) {

        for(int i = 0, n = s.length() ; i < n ; i++) {
            if (Character.isDigit(s.charAt(i))) {
                return i;
            }
        }

        return -1;
    }
}
