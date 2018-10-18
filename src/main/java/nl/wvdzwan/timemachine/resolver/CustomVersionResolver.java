package nl.wvdzwan.timemachine.resolver;

import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.resolution.VersionRequest;
import org.eclipse.aether.resolution.VersionResolutionException;
import org.eclipse.aether.resolution.VersionResult;

public class CustomVersionResolver extends DefaultVersionResolver {

    @Override
    public VersionResult resolveVersion(RepositorySystemSession session, VersionRequest request )
            throws VersionResolutionException
    {

        return super.resolveVersion(session, request);
    }
}
