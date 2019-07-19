package nl.wvdzwan.lapp.callgraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClassPathFile {
    private static Logger logger = LogManager.getLogger();

    private ArrayList<String> jars = new ArrayList<>();

    public ClassPathFile (File classpathFile) throws FileNotFoundException {

        BufferedReader reader = new BufferedReader(new FileReader(classpathFile));

        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                jars.add(line);
            }
        } catch (IOException e) {
            logger.error("Error reading classpath file");
        }

    }



    public ArrayList<String> getJars() {
        return this.jars;
    }
}
