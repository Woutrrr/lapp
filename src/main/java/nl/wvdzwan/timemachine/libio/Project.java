package nl.wvdzwan.timemachine.libio;

import java.util.ArrayList;

public class Project {

    protected String name;
    protected String platform;
    protected String description;

    protected ArrayList<VersionDate> versions;

    public String getName() {
        return name;
    }

    public String getPlatform() {
        return platform;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<VersionDate> getVersions() {
        return versions;
    }
}
