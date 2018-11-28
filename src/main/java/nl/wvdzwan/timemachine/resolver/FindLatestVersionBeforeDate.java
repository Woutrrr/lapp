package nl.wvdzwan.timemachine.resolver;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;

import nl.wvdzwan.timemachine.resolver.util.Booter;


/**
 * Find latest version of an artifact before a specified date.
 */
public class FindLatestVersionBeforeDate {


    private static final Logger logger = LogManager.getLogger(FindLatestVersionBeforeDate.class);

    private final RepositorySystem system;

    public FindLatestVersionBeforeDate(RepositorySystem system) {
        this.system = system;
    }

    /**
     * Return latest version of an artifact before date.
     *
     * <p>All found versions after `date` are ignored</p>
     *
     * @param packageIdentifier The Artifact identifier, formatted as groupID:artifactID
     * @param dateLimit         The date used to filter newer versions
     * @return found version
     * @throws VersionRangeResolutionException when
     */
    public Version latestBeforeDate(String packageIdentifier, LocalDateTime dateLimit)
            throws VersionRangeResolutionException {
        logger.debug("Find latest version of {} before {}...", packageIdentifier, dateLimit);

        DefaultRepositorySystemSession session = Booter.newRepositorySystemSession(system);
        session.setConfigProperty(CustomVersionRangeResolver.CONFIG_LIMIT_DATE, dateLimit);


        DefaultArtifact artifact = new DefaultArtifact(packageIdentifier + ":[0,)");

        VersionRangeRequest rangeRequest = new VersionRangeRequest();
        rangeRequest.setArtifact(artifact);
        rangeRequest.setRepositories(Booter.newRepositories(system, session));


        VersionRangeResult rangeResult = system.resolveVersionRange(session, rangeRequest);

        if (rangeResult.getVersions().size() == 0) {
            logger.error("No suitable artifact found for {} before {}", packageIdentifier, dateLimit);
            throw new VersionRangeResolutionException(
                    rangeResult,
                    String.format("No suitable artifact found for %s before %s", packageIdentifier, dateLimit)
            );
        }

        Version version = rangeResult.getHighestVersion();
        logger.info("Found version {} for {} before {}", version.toString(), packageIdentifier, dateLimit);

        return version;
    }

}
