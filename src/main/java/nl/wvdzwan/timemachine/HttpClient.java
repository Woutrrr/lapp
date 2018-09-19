package nl.wvdzwan.timemachine;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class HttpClient {


    public String get(String url) throws IOException {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        //add request header

        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    /**
     * Download file to temporary location
     *
     * @param url
     * @return Location of downloaded file
     * @throws IOException
     */
    public File getFile(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        //add request header

        int responseCode = con.getResponseCode();

        File temp = File.createTempFile("MavenHistory", ".tmp");

        try (InputStream in = con.getInputStream()) {
            Files.copy(con.getInputStream(), temp.toPath());
        }


        return temp;
    }

}
