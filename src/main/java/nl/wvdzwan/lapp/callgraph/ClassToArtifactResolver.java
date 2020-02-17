package nl.wvdzwan.lapp.callgraph;

import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import nl.wvdzwan.lapp.callgraph.FolderLayout.ArtifactFolderLayout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.jar.JarFile;

public class ClassToArtifactResolver implements ClassArtifactResolver {
    protected static final Logger logger = LogManager.getLogger();

    private final ArtifactFolderLayout transformer;
    private final IClassLoader classLoader;
    private IClassHierarchy cha;

    private HashMap<IClass, String> classArtifactRecordCache = new HashMap<>();

    public ClassToArtifactResolver(IClassHierarchy cha, ArtifactFolderLayout transformer, IClassLoader classLoader) {
        this.cha = cha;
        this.transformer = transformer;
        this.classLoader = classLoader;
    }

    @Override
    public String artifactFromMethodReference(MethodReference n) {
        IClass klass = cha.lookupClass(n.getDeclaringClass());

        if (klass == null) {
            // Try harder
            TypeReference t = TypeReference.findOrCreate(this.classLoader.getReference(), n.getDeclaringClass().getName());
            klass = cha.lookupClass(t);
        }

        if (klass == null) {
            logger.warn("Couldn't find class for {}", () -> n);
            return "unknown";
        }

        return artifactFromClass(klass);
    }

    @Override
    public String artifactFromClass(IClass klass) {
        Objects.requireNonNull(klass);

        if (classArtifactRecordCache.containsKey(klass)) {
            return classArtifactRecordCache.get(klass);
        }

        JarFile jarFile = classToJarFile(klass);

        if (jarFile == null) {
            return "#unknown#";
        }

        String artifact = transformer.artifactFromJarFile(jarFile);

        // Store in cache
        classArtifactRecordCache.put(klass, artifact);

        return artifact;
    }

    private JarFile classToJarFile(IClass klass) {
        Objects.requireNonNull(klass);

        if (klass instanceof ArrayClass)  {
            ArrayClass arrayClass = (ArrayClass) klass;
            IClass innerClass = arrayClass.getElementClass();

            if (innerClass == null) {
                // getElementClass returns null for primitive types
                if (klass.getReference().getArrayElementType().isPrimitiveType()) {
                    try {
                        return new JarFile("rt.jar");
                    } catch (IOException e) {
                        return null;
                    }
                }

                return null;
            }
        }

        try {
            ShrikeClass shrikeKlass = (ShrikeClass) klass;
            JarFileEntry moduleEntry = (JarFileEntry) shrikeKlass.getModuleEntry();

            JarFile jf = moduleEntry.getJarFile();

            return jf;
        } catch (ClassCastException e){
            return null;
        }
    }

}
