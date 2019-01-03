package nl.wvdzwan.timemachine.resolver;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.version.Version;

import nl.wvdzwan.timemachine.resolver.util.Booter;
import nl.wvdzwan.timemachine.resolver.util.VersionNotFoundException;


public class ResolveDependencies {

    private static Logger logger = LogManager.getLogger();

    private RepositorySystem system;
    private DefaultRepositorySystemSession session;
    private ArtifactVersionResolver versionFinder;

    public ResolveDependencies(RepositorySystem system, ArtifactVersionResolver versionFinder) {

        this.system = system;
        this.versionFinder = versionFinder;
        this.session = Booter.newRepositorySystemSession(system);
    }

    public DependencyResult resolveFromDate(String packageIdentfier, LocalDateTime dateLimit)
            throws VersionRangeResolutionException, DependencyResolutionException {

        //  Find version for date
        Version latestVersion = versionFinder.latestBeforeDate(packageIdentfier, dateLimit);

        // Resolve for version & date
        return resolve(packageIdentfier, latestVersion.toString(), dateLimit);

    }

    public DependencyResult resolveFromVersion(String packageIdentifier, String version)
            throws DependencyResolutionException, VersionNotFoundException {

        // Add 1 day to compare to before or on this day with less than
        LocalDateTime dateLimit = versionFinder.dateOfVersion(packageIdentifier, version).plusDays(1).truncatedTo(ChronoUnit.DAYS);

        // Resolve for version & date
        return resolve(packageIdentifier, version, dateLimit);
    }


    public DependencyResult resolve(String packageIdentifier, String version, LocalDateTime datetimeLimit)
            throws DependencyResolutionException {
        logger.debug(
                "Resolve dependency tree for {} {} travelling back to {}",
                packageIdentifier, version, datetimeLimit
        );

        session.setConfigProperty(CustomVersionRangeResolver.CONFIG_LIMIT_DATE, datetimeLimit);

        Artifact artifact = new DefaultArtifact(packageIdentifier + ":" + version);
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, ""));
        collectRequest.setRepositories(Booter.newRepositories());

//        DependencyFilter dependencyFilter = new AndDependencyFilter(
//                DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE),
//                new OptionalDependencyFilter()
//        );

        DependencyFilter dependencyFilter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);

        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, dependencyFilter);

        DependencyResult dependencyResult = system.resolveDependencies(session, dependencyRequest);

        return dependencyResult;

    }

}
