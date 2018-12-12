
package nl.wvdzwan.timemachine.callgraph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.warnings.Warnings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WalaAnalysis {
    private static Logger logger = LogManager.getLogger();

    private String mainJar;
    private String classPath;

    private String exclusionFile;


    public WalaAnalysis(String mainJar, String classPath, String exclusionFile) {
        this.mainJar = mainJar;
        this.classPath = classPath;
        this.exclusionFile = exclusionFile;
    }

    public CallGraph run() throws IOException, ClassHierarchyException {
        try {

            File exclusionsFile = (new FileProvider()).getFile(exclusionFile);
            AnalysisScope scope = AnalysisScopeReader.makePrimordialScope(exclusionsFile);

            AnalysisScopeReader.addClassPathToScope(mainJar, scope, scope.getLoader(AnalysisScope.APPLICATION));
            AnalysisScopeReader.addClassPathToScope(classPath, scope, scope.getLoader(AnalysisScope.EXTENSION));


            logger.debug("Building class hierarchy...");
            ClassHierarchy cha = ClassHierarchyFactory.make(scope);
            logger.info("Class hierarchy built, {} classes", cha::getNumberOfClasses);

            String warning = Warnings.asString();
            if (warning.length() > 0) {
                logger.warn(warning);
            }


            // Prepare call graph generation
            ArrayList<Entrypoint> entryPoints = getEntrypoints(cha);

            AnalysisOptions options = new AnalysisOptions(scope, entryPoints);
            AnalysisCache cache = new AnalysisCacheImpl();

            logger.debug("Preform RTA analysis...");
            CallGraphBuilder builder = Util.makeRTABuilder(options, cache, cha, scope);
            CallGraph cg = builder.makeCallGraph(options, null);
            logger.info("RTA analysis done!");
            logger.info(() -> CallGraphStats.getStats(cg));

            return cg;

        } catch (CallGraphBuilderCancelException e) {
            logger.warn("Graph building cancelled! Continuing with partial graph");
            return e.getPartialCallGraph();
        }
    }

    public ArrayList<Entrypoint> getEntrypoints(ClassHierarchy cha) {

        ArrayList<Entrypoint> entryPoints = new ArrayList<>();

        for (IClass klass : cha) {
            if (acceptClassForEntryPoints(klass)) {

                Collection<Entrypoint> acceptedMethods = klass.getAllMethods().stream()
                        .filter(WalaAnalysis::acceptMethodAsEntryPoint)
                        .map(n -> new DefaultEntrypoint(n, cha))
                        .collect(Collectors.toList());

                entryPoints.addAll(acceptedMethods);

            }
        }

        return entryPoints;
    }


    private static boolean acceptClassForEntryPoints(IClass klass) {
        return klass.getClassLoader().getReference().equals(ClassLoaderReference.Application)
                && !klass.isInterface()
                && klass.isPublic();
    }

    public static boolean acceptMethodAsEntryPoint(IMethod method) {
        return method.getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application)
                && method.isPublic()
                && !method.isAbstract();
    }


}

