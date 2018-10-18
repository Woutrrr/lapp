package nl.wvdzwan.timemachine;

import nl.wvdzwan.timemachine.libio.LibrariesIOClient;
import nl.wvdzwan.timemachine.libio.Project;
import nl.wvdzwan.timemachine.libio.VersionDate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.eclipse.aether.collection.UnsolvableVersionConflictException;
import org.eclipse.aether.graph.DependencyNode;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class DependencyTreeResolver {
    protected static Logger logger = LogManager.getLogger();

    private final LibrariesIOClient api;
    private final ModelFactory modelFactory;

    public DependencyTreeResolver(LibrariesIOClient api, ModelFactory modelFactory) {
        this.api = api;
        this.modelFactory = modelFactory;
    }

    public DependencyResolveResult resolve(ArtifactRecord rootArtifact, LocalDateTime datetime_limit) throws Exception, VersionRangeNotFulFilledException, InvalidVersionSpecificationException {
        Project project = api.getProjectInfo(rootArtifact.getUnversionedIdentifier());

        Optional<VersionDate> optional_version = project.getVersions().stream()
                .filter(v -> v.getPublished_at().isBefore(datetime_limit)) // Filter on timestamp
                .max((vd1, vd2) -> vd1.getPublished_at().compareTo(vd2.getPublished_at()));

        if (!optional_version.isPresent()) {
            throw new Exception("No suitable artifact found!");
        }

        VersionDate versionDate = optional_version.get();
        rootArtifact.setVersion(versionDate.getNumber());
        logger.info("Found version: {} for {} with timestamp {}", versionDate.getNumber(), rootArtifact.getUnversionedIdentifier(), versionDate.getPublished_at().toString());


        DependencyResolveResult result = new DependencyResolveResult();
        result.add(rootArtifact);

        // TODO include first resolve in loop



        Model model = modelFactory.getModel(rootArtifact.getGroupId(), rootArtifact.getArtifactId(), rootArtifact.getVersion());

        List<Dependency> deps = model.getDependencies();

        Deque<Dependency> toResolve = new ArrayDeque<>();
        toResolve.addAll(deps);

        while (!toResolve.isEmpty()) {
            Dependency dep = toResolve.removeFirst();
            String dependencyIdentifier = dep.getGroupId() + ":" + dep.getArtifactId();

            if (result.contains(dependencyIdentifier)) {
                logger.info("{} already resolved before. Skipping...", dependencyIdentifier);
                continue;
            }

            // Resolve dependency
            String version = dep.getVersion();

            if (isVersionRange(version)) {
                VersionDate dependencyVersionDate = resolveVersionRange(dependencyIdentifier, version, datetime_limit, api);

                logger.info("Resolved  {} {} to {}", dependencyIdentifier, version, dependencyVersionDate.getNumber());

                version = dependencyVersionDate.getNumber();
            }

            logger.info("Add {}:{} to project list", dependencyIdentifier, version);
            result.add(new ArtifactRecord(dep.getGroupId(), dep.getArtifactId(), version));

            Model dependencyProject;
            try {
                dependencyProject = modelFactory.getModel(dep.getGroupId(), dep.getArtifactId(), version);
            } catch (UnresolvableModelException e) {
                logger.warn("Could not build model for {}:{}, skipping...", dependencyIdentifier, version);
                continue;
            }
            List<Dependency> subDependencies = dependencyProject.getDependencies();

            List<String> acceptedScopes = Arrays.asList("compile", "runtime");
            List<String> commonScopes = Arrays.asList("compile", "test");

            // Just to inform/warn of uncommon scopes
            subDependencies.stream()
                    .filter(dependency -> !commonScopes.contains(dependency.getScope()))
                    .forEach(dependency -> logger.warn("Uncommon scope " + dependency.getScope() + " for " + dependency.getGroupId() + ":" + dependency.getArtifactId() +":" + dependency.getVersion()));

            List<Dependency> filteredDependencies = subDependencies.stream()
                    .filter(dependency -> acceptedScopes.contains(dependency.getScope()))
                    .collect(Collectors.toList());

            if (filteredDependencies.size() > 0) {
                logger.debug("Filtered dependencies of {}:{} : {}", dependencyIdentifier, dep.getVersion(), filteredDependencies);
            }
            toResolve.addAll(filteredDependencies);

        }

        return result;
    }

    /**
     * Download jar files for a list of projects
     *
     * @param projects Collection with projects to download jars for
     * @param destinationDir destination to save jar files to
     * @return List with paths of downloaded files
     * @throws IOException
     */
    public ArrayList<Path> downloadJars(Collection<ArtifactRecord> projects, File destinationDir) throws IOException {
        ArtifactRepositoryLayout layout = new DefaultRepositoryLayout();
        CustomRemoteRepository remoteRepository = new CustomRemoteRepository("http://repo1.maven.org/maven2/", layout);

        ArrayList<Path> downloadedJars = new ArrayList<>();
        destinationDir.mkdirs();

        for (ArtifactRecord project : projects) {

            Path destination = (new File(destinationDir, project.getJarName())).toPath();

            // Download jar to temporary location
            File downloadedJar = remoteRepository.getJar(project);

            Files.deleteIfExists(destination);

            Files.move(downloadedJar.toPath(), destination);

            downloadedJars.add(destination);
        }

        return downloadedJars;
    }

    private class VersionRangeNotFulFilledException extends Exception {
        public VersionRangeNotFulFilledException(String message) {
            super(message);
        }
    }

    public boolean isVersionRange(String versionDefinition) {
        final char[] rangeIndicators = {'[', ']', '(', ')', ','};

        for (char character : rangeIndicators) {
            if (versionDefinition.indexOf(character) > -1) {
                return true;
            }
        }

        return false;
    }

    public VersionDate resolveVersionRange(String projectIdentifier, String versionRangeDef, LocalDateTime timestamp, LibrariesIOClient api)
            throws
            InvalidVersionSpecificationException,
            VersionRangeNotFulFilledException {

        Project project = api.getProjectInfo(projectIdentifier);

        VersionRange versionRange = VersionRange.createFromVersionSpec(versionRangeDef);

        Optional<VersionDate> maybeVersion = project.getVersions().stream()
                .filter(v -> v.getPublished_at().isBefore(timestamp)) // Filter on timestamp
                .filter(v -> versionRange.containsVersion(new DefaultArtifactVersion(v.getNumber()))) // Filter on version range definition
                .max(Comparator.comparing(VersionDate::getPublished_at)); // Get last available version

        if (!maybeVersion.isPresent()) {
            throw new VersionRangeNotFulFilledException("No suitable artifact found!");
        }
        return maybeVersion.get();

    }
}
