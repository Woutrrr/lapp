package nl.wvdzwan.timemachine;

import nl.wvdzwan.timemachine.libio.LibrariesIoClient;
import nl.wvdzwan.timemachine.libio.LibrariesIoInterface;
import nl.wvdzwan.timemachine.resolver.DateVersionFilter;
import nl.wvdzwan.timemachine.resolver.OptionalDependencyFilter;
import nl.wvdzwan.timemachine.resolver.util.Booter;
import nl.wvdzwan.timemachine.resolver.util.ConsoleDependencyGraphDumper;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.AndDependencyFilter;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ResolveDependencies {


    /**
     * Main.
     * @param args
     * @throws Exception
     */
    public static void main( String[] args )
            throws Exception
    {
        System.out.println( "------------------------------------------------------------" );
        System.out.println( ResolveDependencies.class.getSimpleName() );

        String apiKey = "ad19ce0d9ce33eac2bcdadb6ea73a388";
        String datetime_limit = "2018-06-12";

        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        RepositorySystem system = Booter.newRepositorySystem(locator);

        LibrariesIOInterface api = locator.getService(LibrariesIOInterface.class);
        api.setApiKey( apiKey );


        DefaultRepositorySystemSession session = Booter.newRepositorySystemSession( system );


        ((DefaultRepositorySystemSession) session).setConfigProperty("time-machine.date", datetime_limit);

        Artifact artifact = new DefaultArtifact( "org.springframework:spring-web:5.0.7.RELEASE" );
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot( new Dependency( artifact, "" ) );
        collectRequest.setRepositories( Booter.newRepositories( system, session ) );

        CollectResult collectResult = system.collectDependencies( session, collectRequest );

        collectResult.getRoot().accept( new ConsoleDependencyGraphDumper());

        DependencyFilter dependencyFilter = new AndDependencyFilter(
                DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE),
                new OptionalDependencyFilter()
        );


        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setRoot(collectResult.getRoot());
        dependencyRequest.setFilter(dependencyFilter);

        DependencyResult dependencyResult = system.resolveDependencies( session, dependencyRequest);

        List<String> jarPaths = dependencyResult.getArtifactResults().stream()
                .filter(ArtifactResult::isResolved)
                .map(ArtifactResult::getArtifact)
                .map(Artifact::getFile)
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());

        System.out.println(jarPaths);

        dependencyResult.getRoot().accept( new ConsoleDependencyGraphDumper() );
    }

}
