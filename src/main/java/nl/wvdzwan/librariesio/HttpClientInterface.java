package nl.wvdzwan.librariesio;

import java.io.File;
import java.io.IOException;

public interface HttpClientInterface {
    String get(String url) throws IOException;

    File getFile(String url) throws IOException;
}
