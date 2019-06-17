package nl.wvdzwan.lapp.callgraph.wala;

import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.callgraph.ArtifactRecord;
import nl.wvdzwan.lapp.callgraph.ClassToArtifactResolver;
import nl.wvdzwan.lapp.callgraph.FolderLayout.ArtifactFolderLayout;
import nl.wvdzwan.lapp.core.ClassRecord;
import nl.wvdzwan.lapp.core.LappPackage;
import nl.wvdzwan.lapp.core.Method;
import nl.wvdzwan.lapp.core.ResolvedMethod;
import nl.wvdzwan.lapp.core.UnresolvedMethod;
import nl.wvdzwan.lapp.core.Util;

public class LappPackageBuilder {

    private static final Logger logger = LogManager.getLogger();
    private final LappPackage lappPackage;
    private ArtifactFolderLayout folderLayout;

    private ClassToArtifactResolver artifactResolver;

    enum MethodType {
        INTERFACE, ABSTRACT, IMPLEMENTATION
    }

    public LappPackageBuilder(ClassToArtifactResolver artifactResolver, ArtifactFolderLayout folderLayout) {
        this.artifactResolver = artifactResolver;
        this.folderLayout = folderLayout;

        this.lappPackage = new LappPackage();
    }


    public LappPackageBuilder setPackages(List<Module> modules) {

        for (Module m : modules) {
           if (m instanceof JarFileModule) {
               JarFileModule jfm = ((JarFileModule) m);

               lappPackage.artifacts.add(folderLayout.artifactRecordFromJarFile(jfm.getJarFile()));
           } else {
               logger.warn("Unknown module to analyse found.");
           }
        }

        return this;
    }

    public LappPackageBuilder insertCha(IClassHierarchy cha) {
        ClassHierarchyInserter chaInserter = new ClassHierarchyInserter(cha, this);
        chaInserter.insertCHA();

        return this;
    }

    public LappPackageBuilder insertCallGraph(CallGraph callGraph) {
        if (callGraph == null) {
            // Package probably didn't contain entry points
            return this;
        }
        CallGraphInserter cgInserter = new CallGraphInserter(callGraph, callGraph.getClassHierarchy(), this);
        cgInserter.insertCallGraph();

        return this;
    }

    public Method addMethod(MethodReference nodeReference, MethodType type) {
        Method method = addMethod(nodeReference);
        method.metadata.put("type", type.toString());

        return method;
    }


    public Method addMethod(MethodReference reference) {

        String namespace = Util.typeReferenceToNamespace(reference.getDeclaringClass());
        String symbol = reference.getSelector().toString();


        if (inApplicationScope(reference)) {
            ArtifactRecord record = artifactResolver.artifactRecordFromMethodReference(reference);

            ResolvedMethod resolvedMethod = ResolvedMethod.findOrCreate(namespace, symbol, record.getIdentifier());

            return resolvedMethod;

        } else {
            UnresolvedMethod unresolvedMethod = UnresolvedMethod.findOrCreate(namespace, symbol);
            return unresolvedMethod;
        }
    }

    public boolean addCall(Method source, Method target, Call.CallType type) {

        return lappPackage.addCall(source, target, type);

    }

    public ClassRecord makeClassRecord(IClass klass) {
        ArtifactRecord artifactRecord = artifactResolver.artifactRecordFromClass(klass);

        ClassRecord record = new ClassRecord(artifactRecord.getIdentifier(), Util.typeReferenceToNamespace(klass.getReference()));
        lappPackage.addClassRecord(record);

        return record;
    }

    public LappPackage build() {
        return this.lappPackage;
    }

    private boolean inApplicationScope(MethodReference reference) {
        return reference.getDeclaringClass().getClassLoader().equals(ClassLoaderReference.Application);
    }


}
