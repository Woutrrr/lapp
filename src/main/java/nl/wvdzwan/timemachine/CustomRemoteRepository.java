package nl.wvdzwan.timemachine;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.model.resolution.UnresolvableModelException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class CustomRemoteRepository implements MavenRepository {

    static final Logger logger = LogManager.getLogger(CustomRemoteRepository.class);

    protected String url;
    protected ArtifactRepositoryLayout layout;

    public CustomRemoteRepository(String url, ArtifactRepositoryLayout layout) {
        this.url = url;
        this.layout = layout;
    }

    @Override
    public String getMetaData(String groupId, String artifactId) {
        return null;
    }

    @Override
    public List<String> getVersions(String groupId, String artifactId) {
        return null;
    }

    @Override
    public File getPom(Artifact artifact) throws UnresolvableModelException {


        File pomFile = null;
        try {
            pomFile = File.createTempFile("mavenScraper", "pomFile");
        } catch (IOException e) {
            e.printStackTrace();
        }

        URI remote = URI.create(url + layout.pathOf(artifact) + ".pom");

        try{
            downloadPom(pomFile, remote);
        } catch (HttpResponseException e) {
            logger.warn("Could not download pom for {}:{}:{}, got status code {}",
                    artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), e.getStatusCode());

            throw new UnresolvableModelException("Could not find pom in repository, got http status: " + e.getMessage(), artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
        }

        return pomFile;
    }

    protected File downloadPom(File localPath, URI remotePath) throws HttpResponseException {
        logger.debug("Downloading {}", remotePath);

        // TODO : extract downloading coupling
        CloseableHttpClient client = createHttpClient();
        HttpGet httpGet = new HttpGet(remotePath);
        try {

            CloseableHttpResponse response = client.execute(httpGet);
            FileOutputStream fo = null;
            try {
                logger.debug("Status: {}", response.getStatusLine());

                HttpEntity entity = response.getEntity();

                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                  throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().toString());
                }

                fo = new FileOutputStream(localPath);
                entity.writeTo(fo);

                EntityUtils.consume(entity);

            } finally {
                if (fo != null) {
                    fo.close();
                }
                response.close();
            }

        } catch (HttpResponseException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return localPath;
    }

    private CloseableHttpClient createHttpClient() {
        return HttpClients.custom()
                .useSystemProperties()
//                .setConnectionManager(cm)
                // TODO : setup connection manager?
                .build();
    }

    @Override
    public File getJar(ArtifactRecord artifact) throws JarNotFoundException {
        return getJar(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
    }

    @Override
    public File getJar(String groupId, String artifactId, String version) throws JarNotFoundException {

        logger.info("Downloading jar of {} {} {}", groupId, artifactId, version);

        // https://repo1.maven.org/maven2/org/jdtaus/banking/jdtaus-banking-messages/1.14/jdtaus-banking-messages-1.14.jar
        String url = String.format("http://repo1.maven.org/maven2/%1$s/%2$s/%3$s/%2$s-%3$s.jar",
                groupId.replace('.', '/'),
                artifactId,
                version);
        logger.debug("Accessing {}", url);

        File temp = null;
        try {
            temp = File.createTempFile("MavenHistory", ".tmp");


            // TODO : extract downloading coupling
            CloseableHttpClient client = createHttpClient();
            HttpGet httpGet = new HttpGet(url);


            try (CloseableHttpResponse response = client.execute(httpGet)) {

                logger.debug("Status: {}", response.getStatusLine());

                HttpEntity entity = response.getEntity();
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new JarNotFoundException("Jar not found, got http status: " + response.getStatusLine().toString(), groupId, artifactId, version);
                }

                if (entity != null) {
                    try (FileOutputStream outstream = new FileOutputStream(temp)) {
                        entity.writeTo(outstream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return temp;

    }
}
