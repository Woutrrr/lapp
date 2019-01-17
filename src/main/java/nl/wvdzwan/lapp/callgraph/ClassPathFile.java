package nl.wvdzwan.lapp.callgraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ClassPathFile {

    private String mainJar;
    private ArrayList<String> dependencies = new ArrayList<>();

    public ClassPathFile (File file, Path jarsFolder) throws FileNotFoundException {
        this(new BufferedReader(new FileReader(file)), jarsFolder);
    }

    public ClassPathFile (BufferedReader reader, Path jarsFolder) {



        // Read mainJar
        try {
            String line = reader.readLine();

            if (null == line) {
                throw new IllegalArgumentException("Invalid classpath file, first line must contain jar filename");
            }

            mainJar = jarsFolder.resolve(Paths.get(line)).toString();;

        } catch (IOException e) {
            // Client responsibility
            mainJar = null;
        }

        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String combinedPath = jarsFolder.resolve(Paths.get(line)).toString();
                dependencies.add(combinedPath);
            }
        } catch (IOException e) {
            // No dependencies are also ok
        }

    }

    public String getMainJar() {
        return this.mainJar;
    }

    public ArrayList<String> getDependencies() {
        return this.dependencies;
    }
}
