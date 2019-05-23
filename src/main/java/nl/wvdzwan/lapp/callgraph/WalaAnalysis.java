
package nl.wvdzwan.lapp.callgraph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
import com.ibm.wala.util.config.SetOfClasses;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.warnings.Warning;
import com.ibm.wala.util.warnings.Warnings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.wvdzwan.lapp.callgraph.wala.WalaAnalysisResult;

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

    public WalaAnalysisResult run() throws IOException, ClassHierarchyException {
        try {

            File exclusionsFile = (new FileProvider()).getFile(exclusionFile);
            AnalysisScope scope = AnalysisScopeReader.makePrimordialScope(exclusionsFile);
            AnalysisScopeReader.addClassPathToScope(mainJar, scope, scope.getLoader(AnalysisScope.APPLICATION));

            if (!classPath.equals("")) {
                AnalysisScopeReader.addClassPathToScope(classPath, scope, scope.getLoader(AnalysisScope.EXTENSION));
            }
            logger.debug("Building class hierarchy...");
            // TODO : This really should use makeWithPhantom however that function is not yet stable and will cause NPE's later in the analysis
            ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope);
            logger.info("Class hierarchy built, {} classes", cha::getNumberOfClasses);

            for (Warning warning : filterExclusionsWarnings(scope.getExclusions())) {
                if (warning.getMsg().startsWith("class com.ibm.wala.classLoader.BytecodeClass$ClassNotFoundWarning")) {
                    // Missing classes is expected since the classpath only has main jar + primordial
                    continue;
                }
                logger.warn(warning);
            }
            Warnings.clear();

            // Prepare call graph generation
            ArrayList<Entrypoint> entryPoints = getEntrypoints(cha);

            AnalysisOptions options = new AnalysisOptions(scope, entryPoints);
            AnalysisCache cache = new AnalysisCacheImpl();

            logger.debug("Preform RTA analysis...");
            long startTime = System.nanoTime();
            CallGraphBuilder builder = Util.makeRTABuilder(options, cache, cha, scope);
            CallGraph cg = builder.makeCallGraph(options, null);
            long endTime = System.nanoTime();

            logger.info("RTA analysis done!");
            logger.info(() -> CallGraphStats.getStats(cg));


            logger.info("Took {}", () -> (endTime - startTime)/1000000);

            return new WalaAnalysisResult(cg, cg.getClassHierarchy());

        } catch (CallGraphBuilderCancelException e) {
            logger.warn("Graph building cancelled! Continuing with partial graph");
            return new WalaAnalysisResult(e.getPartialCallGraph(), e.getPartialCallGraph().getClassHierarchy());
        }
    }

    private ArrayList<Warning> filterExclusionsWarnings(SetOfClasses exclusions) {
        ArrayList<Warning> result = new ArrayList<>();

        for (Iterator<Warning> it = Warnings.iterator(); it.hasNext(); ) {
            Warning warning = it.next();

            String msg = warning.getMsg();

            if (msg.startsWith("class com.ibm.wala.ipa.cha.ClassHierarchy$ClassExclusion")) {
                int index = msg.lastIndexOf("Superclass name");

                String superClass = msg.substring(index + 17);

                if (exclusions.contains(superClass)) {
                    continue;
                }

            }

            if (msg.startsWith("class com.ibm.wala.classLoader.BytecodeClass$ClassNotFoundWarning")) {
                int index = msg.lastIndexOf("ClassNotFoundWarning");
                String klass = msg.substring(index + 24);

                if (exclusions.contains(klass)) {
                    continue;
                }
            }

            result.add(warning);
        }

        return result;
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

