package nl.wvdzwan.lapp.resolver.util;

import org.eclipse.aether.resolution.VersionResolutionException;
import org.eclipse.aether.resolution.VersionResult;

public class LibIOVersionResolutionException extends VersionResolutionException {
    public LibIOVersionResolutionException(VersionResult result) {
        super(result, getMessage(result));
    }

    private static String getMessage( VersionResult result )
    {
        StringBuilder buffer = new StringBuilder( 256 );
        buffer.append( "Failed to find version" );
        if ( result != null )
        {
            buffer.append( " for " ).append( result.getRequest().getArtifact() );
            if ( !result.getExceptions().isEmpty() )
            {
                buffer.append( ": " ).append( result.getExceptions().iterator().next().getMessage() );
            }
        }

        buffer.append(" in libraries.io api");
        return buffer.toString();
    }

}
