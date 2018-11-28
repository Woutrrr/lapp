package nl.wvdzwan.librariesio;

import java.io.IOException;

public interface HttpClientInterface {
    String get(String url) throws IOException;
}
