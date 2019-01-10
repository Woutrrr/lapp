package nl.wvdzwan.timemachine.resolver.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.impl.VersionResolver;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.wvdzwan.librariesio.HttpClient;
import nl.wvdzwan.librariesio.HttpClientInterface;
import nl.wvdzwan.librariesio.LibrariesIoInterface;
import nl.wvdzwan.librariesio.RateLimitedClient;
import nl.wvdzwan.timemachine.resolver.CustomVersionRangeResolver;
import nl.wvdzwan.timemachine.resolver.CustomVersionResolver;

/**
 * A helper to boot the service locator, the repository system and a repository system session.
 */
public class Booter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Booter.class);

    public static final String LOCAL_REPO = "local-repo";

    public static DefaultServiceLocator newServiceLocator() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();

        locator.setService(HttpClientInterface.class, HttpClient.class);
        locator.setService(LibrariesIoInterface.class, RateLimitedClient.class);

        return locator;
    }

    public static RepositorySystem newRepositorySystem(DefaultServiceLocator locator) {

        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        locator.setService(VersionResolver.class, CustomVersionResolver.class);
        locator.setService(VersionRangeResolver.class, CustomVersionRangeResolver.class);

        locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
            @Override
            public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
                LOGGER.error("Service creation failed for {} implementation {}: {}",
                        type, impl, exception.getMessage(), exception);
            }
        });

        return locator.getService(RepositorySystem.class);
    }

    public static DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository(LOCAL_REPO);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        session.setTransferListener(new LoggerTransferListener());
        session.setRepositoryListener(new LoggerRepositoryListener());


        // uncomment to generate dirty trees
        // session.setDependencyGraphTransformer( null );

        return session;
    }

    public static List<RemoteRepository> newRepositories() {
        return new ArrayList<>(Collections.singletonList(newCentralRepository()));
    }

    private static RemoteRepository newCentralRepository() {
        return new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build();
    }

}
