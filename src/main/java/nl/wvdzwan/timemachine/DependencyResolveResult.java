package nl.wvdzwan.timemachine;

import java.util.LinkedHashMap;
import java.util.Map;

public class DependencyResolveResult {

    private Map<String, ArtifactRecord> foundProjects;


    public DependencyResolveResult() {
        foundProjects = new LinkedHashMap<>();
    }


    public boolean contains(String unversionIdentifier) {
        return foundProjects.containsKey(unversionIdentifier);
    }

    public void add(ArtifactRecord artifactRecord) {
        if (!foundProjects.containsKey(artifactRecord.getUnversionedIdentifier())) {
            foundProjects.put(artifactRecord.getUnversionedIdentifier(), artifactRecord);
        }
    }

    public Map<String, ArtifactRecord> getProjects() {
        return foundProjects;
    }


}
