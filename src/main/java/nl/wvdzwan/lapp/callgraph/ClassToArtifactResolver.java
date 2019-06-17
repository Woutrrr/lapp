package nl.wvdzwan.lapp.callgraph;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.jar.JarFile;

import com.ibm.wala.classLoader.ArrayClass;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.JarFileEntry;
import com.ibm.wala.classLoader.ShrikeClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.wvdzwan.lapp.callgraph.FolderLayout.ArtifactFolderLayout;

public class ClassToArtifactResolver implements ClassArtifactResolver {
    protected static final Logger logger = LogManager.getLogger();

    private final ArtifactFolderLayout transformer;
    private IClassHierarchy cha;

    private HashMap<IClass, ArtifactRecord> classArtifactRecordCache = new HashMap<>();

    public ClassToArtifactResolver(IClassHierarchy cha, ArtifactFolderLayout transformer) {
        this.cha = cha;
        this.transformer = transformer;
    }

    @Override
    public ArtifactRecord artifactRecordFromMethodReference(MethodReference n) {
        IClass klass = cha.lookupClass(n.getDeclaringClass());

        if (klass == null) {
            // Try harder
            TypeReference t = TypeReference.findOrCreate(cha.getLoader(ClassLoaderReference.Application).getReference(), n.getDeclaringClass().getName());
            klass = cha.lookupClass(t);
        }

        if (klass == null) {
            logger.warn("Couldn't find class for {}", () -> n);
            return new ArtifactRecord("unknown", n.getDeclaringClass().toString(), "unknown");
        }

        return artifactRecordFromClass(klass);
    }

    @Override
    public ArtifactRecord artifactRecordFromClass(IClass klass) {
        Objects.requireNonNull(klass);

        if (classArtifactRecordCache.containsKey(klass)) {
            return classArtifactRecordCache.get(klass);
        }

        JarFile jarFile = classToJarFile(klass);

        if (jarFile == null) {
            return new ArtifactRecord("unknown", "unknown", "unknown");
        }

        ArtifactRecord artifactRecord = transformer.artifactRecordFromJarFile(jarFile);

        // Store in cache
        classArtifactRecordCache.put(klass, artifactRecord);

        return artifactRecord;
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
