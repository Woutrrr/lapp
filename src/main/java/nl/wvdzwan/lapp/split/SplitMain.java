package nl.wvdzwan.lapp.split;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class SplitMain {
    private String jar;

    public SplitMain(String jar) {
        this.jar = jar;
    }

    public Map<String, List<String>> split() throws IOException {

        JarFile jar = new JarFile(this.jar);

        Path jarFile = (new File(this.jar + "_split")).getAbsoluteFile().toPath();
        jarFile.toFile().mkdir();
        System.out.println(jarFile);
        List<String> entryNames = new ArrayList<>();

        for(Enumeration<JarEntry> v = jar.entries(); v.hasMoreElements();) {
            JarEntry entry = v.nextElement();
            String entryName = (new File(entry.getName())).getName();

            if (entry.isDirectory() || !entryName.endsWith(".class")) {
                continue;
            }

            entryNames.add(jarFile.resolve(entryName).toAbsolutePath().toString());
            InputStream is = jar.getInputStream(entry);

            Files.copy(is, jarFile.resolve(entryName), REPLACE_EXISTING);


        }


        Map<String, List<String>> entryGroups = entryNames.stream().collect(Collectors.groupingBy(s -> {
            if (s.contains("$")) {
                return s.substring(0, s.indexOf("$"));
            }

            return s.substring(0, s.indexOf(".", jarFile.toString().length()+1));
        }, Collectors.toList()));

        return entryGroups;
    }

    public static Map<String, List<String>> split(String jar) throws IOException {
        SplitMain splitter = new SplitMain(jar);

        return splitter.split();
    }

    public static void main(String[] args) throws IOException {
        System.out.println(split(args[0]));
    }
}
