package nl.wvdzwan.timemachine.resolver;

import nl.wvdzwan.timemachine.libio.LibrariesIOInterface;
import nl.wvdzwan.timemachine.libio.Project;
import nl.wvdzwan.timemachine.libio.VersionDate;
import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.collection.VersionFilter;
import org.eclipse.aether.version.Version;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class DateVersionFilter implements VersionFilter {

    protected LibrariesIOInterface api;
    protected LocalDateTime date;

    public DateVersionFilter(LibrariesIOInterface api, LocalDate date) {
        this.api = api;
        this.date = date.atTime(23, 59, 59);
    }

    public void filterVersions(VersionFilterContext context) throws RepositoryException {
        Artifact artifact = context.getDependency().getArtifact();

        Project project = api.getProjectInfo(artifact.getGroupId() + ":" + artifact.getArtifactId());

        List<String> dateFilteredVersions = project.getVersions().stream()
                .filter(v -> v.getPublished_at().isBefore(date)) // Filter on timestamp
                .map(VersionDate::getNumber)
                .collect(Collectors.toList());

        Iterator<Version> it = context.iterator();
        for (boolean hasNext = it.hasNext(); hasNext; ) {
            Version testVersion = it.next();

            if (!dateFilteredVersions.contains(testVersion.toString())) {
                it.remove();
            }
        }

    }

    @Override
    public VersionFilter deriveChildFilter(DependencyCollectionContext context) {
        return this;
    }
}
