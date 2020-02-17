
package nl.wvdzwan.lapp.callgraph;

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
import com.ibm.wala.util.warnings.Warning;
import com.ibm.wala.util.warnings.Warnings;
import nl.wvdzwan.lapp.callgraph.wala.WalaAnalysisResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class WalaAnalysis {
    private static Logger logger = LogManager.getLogger();
    private final ClassLoaderReference classLoaderReference;

    private ArrayList<String> jars;
    private String exclusionFile;


    public WalaAnalysis(ArrayList<String> jars, String exclusionFile, boolean analyseStdLib) {
        this.jars = jars;
        this.exclusionFile = exclusionFile;

        if (analyseStdLib) {
            this.classLoaderReference = ClassLoaderReference.Primordial;
        } else {
            this.classLoaderReference = ClassLoaderReference.Application;
        }
    }

    public WalaAnalysisResult run() throws IOException, ClassHierarchyException {
        try {
            File exclusionsFile = null;
            if (this.exclusionFile != null) {
                exclusionsFile = new File(this.exclusionFile);
            }
            AnalysisScope scope = AnalysisScopeReader.makePrimordialScope(exclusionsFile);

            for (String jar : jars) {
                AnalysisScopeReader.addClassPathToScope(jar, scope, scope.getLoader(AnalysisScope.APPLICATION));
            }
            logger.debug("Building class hierarchy...");
            ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope);
            logger.info("Class hierarchy built, {} classes", cha::getNumberOfClasses);

            for (Warning warning : filterExclusionsWarnings(scope.getExclusions())) {
//                if (warning.getMsg().startsWith("class com.ibm.wala.classLoader.BytecodeClass$ClassNotFoundWarning")) {
//                    // Missing classes is expected since the classpath only has main jar + primordial
//                    continue;
//                }
                logger.warn(warning);
            }
            Warnings.clear();

            // Prepare call graph generation
            ArrayList<Entrypoint> entryPoints = getEntrypoints(cha);
            if (entryPoints.isEmpty()) {
                logger.warn("No entry points found! So no reason to try and generate a call graph!");
                return new WalaAnalysisResult(null, cha);
            }
            logger.info("Found {} entry points", entryPoints.size());

            AnalysisOptions options = new AnalysisOptions(scope, entryPoints);
            AnalysisCache cache = new AnalysisCacheImpl();

            logger.debug("Preform RTA analysis...");
            long startTime = System.nanoTime();
            CallGraphBuilder builder = Util.makeRTABuilder(options, cache, cha, scope);
            CallGraph cg = builder.makeCallGraph(options, null);
            long endTime = System.nanoTime();

            logger.info("RTA analysis done!");
            logger.info(() -> CallGraphStats.getStats(cg));

            for (Iterator<Warning> it = Warnings.iterator(); it.hasNext(); ) {
                Warning warning = it.next();
                logger.warn(warning);
            }

            logger.info("Took {}", () -> (endTime - startTime) / 1000000);

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

                if (exclusions == null || exclusions.contains(klass)) {
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
                if (klass.getName().toString().equals("Ljava/lang/invoke/LambdaMetafactory")) {
                    continue;
                }
                for (IMethod method : klass.getDeclaredMethods()) {
                    if (acceptMethodAsEntryPoint(method)) {
                        entryPoints.add(new DefaultEntrypoint(method, cha));
                    }
                }
            }
        }

        return entryPoints;
    }


    private boolean acceptClassForEntryPoints(IClass klass) {
        return klass.getClassLoader().getReference().equals(this.classLoaderReference)
                && !klass.isInterface()
                && !klass.isPrivate();
    }

    public static boolean acceptMethodAsEntryPoint(IMethod method) {
        return !method.isPrivate()
                && !method.isAbstract();
    }
}

