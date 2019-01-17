package nl.wvdzwan.lapp.resolver.util;

import java.util.List;

import nl.wvdzwan.lapp.resolver.ResolveBaseException;

public class VersionNotFoundException extends ResolveBaseException {

    public VersionNotFoundException(String message) {
        super(message);
    }

    public VersionNotFoundException(String artifact, String wantedVersion, List<String> foundVersions) {
        super(String.format("Version %s for %s not found! Found versions: %s", wantedVersion, artifact, foundVersions));
    }
}
