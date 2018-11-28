package nl.wvdzwan.timemachine;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.AndDependencyFilter;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.version.Version;

import nl.wvdzwan.timemachine.resolver.ArtifactVersionResolver;
import nl.wvdzwan.timemachine.resolver.CustomVersionRangeResolver;
import nl.wvdzwan.timemachine.resolver.OptionalDependencyFilter;
import nl.wvdzwan.timemachine.resolver.util.Booter;
import nl.wvdzwan.timemachine.resolver.util.ConsoleDependencyGraphDumper;


public class ResolveDependencies {

    private RepositorySystem system;
    private DefaultRepositorySystemSession session;

    public ResolveDependencies(RepositorySystem system) {

        this.system = system;
        this.session = Booter.newRepositorySystemSession(system);
    }

    public ResolveResult resolveFromDate(String packageIdentfier, LocalDateTime dateLimit) throws VersionRangeResolutionException, DependencyResolutionException, DependencyCollectionException {

        //  Find version for date
        ArtifactVersionResolver versionFinder = new ArtifactVersionResolver(system);
        Version latestVersion = versionFinder.latestBeforeDate(packageIdentfier, dateLimit);

        // Resolve for version & date
        resolve(packageIdentfier, latestVersion.toString(), dateLimit);

        return new ResolveResult();
    }

    public ResolveResult resolveFromVersion(String packageIdentifier, String version) {

        // Find date for version

        // Resolve for version & date

        return new ResolveResult();
    }


    public void resolve(String packageIdentifier, String version, LocalDateTime datetime_limit) throws DependencyResolutionException, DependencyCollectionException {
        System.out.println("------------------------------------------------------------");
        System.out.println(ResolveDependencies.class.getSimpleName());


        session.setConfigProperty(CustomVersionRangeResolver.CONFIG_LIMIT_DATE, datetime_limit);

        Artifact artifact = new DefaultArtifact(packageIdentifier + ":" + version);
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, ""));
        collectRequest.setRepositories(Booter.newRepositories(system, session));

        CollectResult collectResult = system.collectDependencies(session, collectRequest);

        collectResult.getRoot().accept(new ConsoleDependencyGraphDumper());

        DependencyFilter dependencyFilter = new AndDependencyFilter(
                DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE),
                new OptionalDependencyFilter()
        );


        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setRoot(collectResult.getRoot());
        dependencyRequest.setFilter(dependencyFilter);

        DependencyResult dependencyResult = system.resolveDependencies(session, dependencyRequest);

        List<String> jarPaths = dependencyResult.getArtifactResults().stream()
                .filter(ArtifactResult::isResolved)
                .map(ArtifactResult::getArtifact)
                .map(Artifact::getFile)
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());

        System.out.println(jarPaths);

        dependencyResult.getRoot().accept(new ConsoleDependencyGraphDumper());
    }

}
