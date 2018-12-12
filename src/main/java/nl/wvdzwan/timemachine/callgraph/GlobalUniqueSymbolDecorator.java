package nl.wvdzwan.timemachine.callgraph;

import java.util.HashMap;
import java.util.jar.JarFile;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.JarFileEntry;
import com.ibm.wala.classLoader.ShrikeClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.viz.NodeDecorator;

import nl.wvdzwan.timemachine.callgraph.outputs.IGraphNode;

public class GlobalUniqueSymbolDecorator implements NodeDecorator<IGraphNode> {

    private final ArtifactFolderLayout transformer;
    private IClassHierarchy cha;

    private HashMap<IClass, ArtifactRecord> classArtifactRecordCache = new HashMap<>();

    public GlobalUniqueSymbolDecorator(IClassHierarchy cha, ArtifactFolderLayout transformer) {
        this.cha = cha;
        this.transformer = transformer;
    }

    @Override
    public String getLabel(IGraphNode node) {

        String sep = "::";
        String ecosystem = "mvn";

        MethodReference n = node.getMethodReference();

        IClass klass = cha.lookupClass(n.getDeclaringClass());

        if (klass == null) {
            TypeReference t = TypeReference.findOrCreate(cha.getLoader(ClassLoaderReference.Application).getReference(), n.getDeclaringClass().getName());
            klass = cha.lookupClass(t);
        }
        ArtifactRecord artifactRecord = artifactRecordFromClass(klass);

        String namespace = n.getDeclaringClass().getName().toString().substring(1).replace('/', '.');
        String symbol = n.getSelector().toString();
        String libraryName = artifactRecord.getUnversionedIdentifier();
        String version = artifactRecord.getVersion();

        return node.prefix(ecosystem + sep
                + libraryName + sep
                + version + sep
                + namespace + sep
                + symbol);
    }

    private ArtifactRecord artifactRecordFromClass(IClass klass) {
        if (classArtifactRecordCache.containsKey(klass)) {
            return classArtifactRecordCache.get(klass);
        }

        JarFile jarFile = classToJarFile(klass);

        if (jarFile == null) {
            return new ArtifactRecord("unknown", "unknown", "unknown");
        }

        ArtifactRecord artifactRecord = transformer.artifactRecordFromPath(jarFile.getName());
        classArtifactRecordCache.put(klass, artifactRecord);
        return artifactRecord;
    }

    private JarFile classToJarFile(IClass klass) {
        if (klass == null) {
            return null;
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
