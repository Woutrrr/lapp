package nl.wvdzwan.timemachine.resolver;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import nl.wvdzwan.timemachine.HttpClient;
import nl.wvdzwan.timemachine.libio.LibrariesIOClient;
import nl.wvdzwan.timemachine.libio.LibrariesIOInterface;
import nl.wvdzwan.timemachine.resolver.util.Booter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Determines the newest version of an artifact.
 */
public class FindLatestVersionBeforeDate
{

    protected static Logger logger = LogManager.getLogger();

    /**
     * Main.
     * @param args
     * @throws Exception
     */
    public static void main( String[] args )
        throws Exception
    {
        System.out.println( "------------------------------------------------------------" );
        System.out.println( FindLatestVersionBeforeDate.class.getSimpleName() );

        String apiKey = "ad19ce0d9ce33eac2bcdadb6ea73a388";
        String datetime_limit = "2008-03-14";
        String identifier = "com.thoughtworks.xstream:xstream";


        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        RepositorySystem system = Booter.newRepositorySystem(locator);

        LibrariesIOInterface api = locator.getService(LibrariesIOInterface.class);
        api.setApiKey( apiKey );


        RepositorySystemSession session = Booter.newRepositorySystemSession( system );
        ((DefaultRepositorySystemSession) session).setVersionFilter(new DateVersionFilter(api, LocalDate.parse("2010-04-04")));
        ((DefaultRepositorySystemSession) session).setConfigProperty("time-machine.date", datetime_limit);
        ((DefaultRepositorySystemSession) session).setConfigProperty("librariesio.key", apiKey);

        Artifact artifact = new DefaultArtifact( identifier + ":[0,)" );

        VersionRangeRequest rangeRequest = new VersionRangeRequest();
        rangeRequest.setArtifact( artifact );
        rangeRequest.setRepositories( Booter.newRepositories( system, session ) );

        VersionRangeResult rangeResult = system.resolveVersionRange( session, rangeRequest );

        if (rangeResult.getVersions().size() == 0) {
            logger.fatal("No suitable artifact found for {} before {}", identifier, datetime_limit);
            return;
        }

        Version version = rangeResult.getHighestVersion();
        logger.info("Found version: {} for {}", version.toString(), identifier);



    }

}
