package nl.wvdzwan.lapp.callgraph;

import java.util.HashMap;
import java.util.Objects;
import java.util.jar.JarFile;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.JarFileEntry;
import com.ibm.wala.classLoader.ShrikeClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;

import nl.wvdzwan.lapp.callgraph.FolderLayout.ArtifactFolderLayout;

public class ClassToArtifactResolver {

    private final ArtifactFolderLayout transformer;
    private IClassHierarchy cha;

    private HashMap<IClass, ArtifactRecord> classArtifactRecordCache = new HashMap<>();

    public ClassToArtifactResolver(IClassHierarchy cha, ArtifactFolderLayout transformer) {
        this.cha = cha;
        this.transformer = transformer;
    }

    public ArtifactRecord artifactRecordFromMethodReference(MethodReference n) {
        IClass klass = cha.lookupClass(n.getDeclaringClass());

        if (klass == null) {
            // Try harder
            TypeReference t = TypeReference.findOrCreate(cha.getLoader(ClassLoaderReference.Application).getReference(), n.getDeclaringClass().getName());
            klass = cha.lookupClass(t);
        }

        return artifactRecordFromClass(klass);
    }

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
