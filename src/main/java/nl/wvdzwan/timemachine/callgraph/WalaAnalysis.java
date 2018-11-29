
/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package nl.wvdzwan.timemachine.callgraph;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.impl.SlowSparseNumberedGraph;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.strings.Atom;
import com.ibm.wala.util.warnings.Warnings;
import com.ibm.wala.viz.DotUtil;
import com.ibm.wala.viz.NodeDecorator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ibm.wala.types.ClassLoaderReference.Java;

/**
 * This simple example WALA application builds a TypeHierarchy and fires off
 * ghostview to viz a DOT representation.
 *
 * @author sfink
 */
public class WalaAnalysis {
    // This example takes one command-line argument, so args[1] should be the "-classpath" parameter
    final static int JARPATH_INDEX = 1;
    final static int CLASSPATH_INDEX = 3;

    final static boolean outputPhantomNodes = true;

    final static boolean outputInterface = true;

    public final static ClassLoaderReference ClassLoaderMissing = new ClassLoaderReference(Atom.findOrCreateUnicodeAtom("Missing"), Java, null);


    public static void main(String[] args) throws IOException {
        run(args);
    }

    public static Process run(String[] args) throws IOException {
        try {
            validateCommandLine(args);
            String mainJar = args[JARPATH_INDEX];
            String classpath = args[CLASSPATH_INDEX];

            File exclusionsFile = (new FileProvider()).getFile("Java60RegressionExclusions.txt");
            AnalysisScope scope = AnalysisScopeReader.makePrimordialScope(exclusionsFile);

            AnalysisScopeReader.addClassPathToScope(mainJar, scope, scope.getLoader(AnalysisScope.APPLICATION));
            AnalysisScopeReader.addClassPathToScope(classpath, scope, scope.getLoader(AnalysisScope.EXTENSION));

            // invoke WALA to build a class hierarchy
            ClassHierarchy cha = ClassHierarchyFactory.make(scope);
            System.out.println(cha.getNumberOfClasses() + " classes");
            System.out.println(Warnings.asString());

            ArrayList<Entrypoint> entrypoints = getEntrypoints(cha);

            //
            // analysis options controls aspects of call graph construction
            //
            AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
            AnalysisCache cache = new AnalysisCacheImpl();

            //
            // build the call graph
            //
            CallGraphBuilder builder = Util.makeRTABuilder(options, cache, cha, scope);
            CallGraph cg = builder.makeCallGraph(options, null);

            System.out.println(CallGraphStats.getStats(cg));

            Graph<MethodReference> methodGraph = outputcg(cg);

            NodeDecorator<MethodReference> labelDecorator = new GlobalUniqueSymbolDecorator(cha, new FlatArtifactFolderLayout());

            DotUtil.writeDotFile(methodGraph, labelDecorator, "\", splines=true, overlap=false, ranksep=5, fontsize=36, root =  \"< Application, Lnl/wvdzwan/tudelft/dynamicDispatch/app/App, main([Ljava/lang/String;)V >", "output/cg.dot");


            return null;


        } catch (WalaException e) {
            e.printStackTrace();
            return null;
        } catch (CallGraphBuilderCancelException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<Entrypoint> getEntrypoints(ClassHierarchy cha) {
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
        return method.isPublic() && !method.isAbstract();
    }

    private static Graph<MethodReference> outputcg(CallGraph cg) {
        Graph<MethodReference> graph = SlowSparseNumberedGraph.make();

        Iterator<CGNode> cgIterator = cg.iterator();
        while (cgIterator.hasNext()) {
            CGNode node = cgIterator.next();
            MethodReference nodeReference = node.getMethod().getReference();

            if (node.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial)) {
                continue;
            }

            graph.addNode(nodeReference);

            for (Iterator<CallSiteReference> callsites = node.iterateCallSites(); callsites.hasNext(); ) {
                CallSiteReference callsite = callsites.next();

                MethodReference targetReference = callsite.getDeclaredTarget();


                Set<CGNode> possibleTargets = cg.getPossibleTargets(node, callsite); // More specific, takes call site into consideration
                //Set<CGNode> possibleTargets = cg.getClassHierarchy().getPossibleTargets(targetReference));

                if (possibleTargets.size() == 0) {
                    System.err.println("No targets found for " + targetReference.toString());

                    if (outputPhantomNodes) {
                        MethodReference missingMethod = MethodReference.findOrCreate(ClassLoaderMissing, targetReference.getDeclaringClass().getName().toString(), targetReference.getName().toString(), targetReference.getSelector().getDescriptor().toString());
                        graph.addNode(missingMethod);

                        if (!graph.hasEdge(nodeReference, missingMethod)) {
                            graph.addEdge(nodeReference, missingMethod);
                        }
                    }
                }

                if (outputInterface && callsite.isInterface()) {
                    MethodReference interfaceReference = cg.getClassHierarchy().resolveMethod(targetReference).getReference();
                    graph.addNode(interfaceReference);
                    if (!graph.hasEdge(nodeReference, interfaceReference)) {
                        graph.addEdge(nodeReference, interfaceReference);
                    }
                }

                for (CGNode possibleTarget : possibleTargets) {
                    MethodReference callSiteTargetReference = possibleTarget.getMethod().getReference();

                    graph.addNode(callSiteTargetReference);

                    if (!graph.hasEdge(nodeReference, callSiteTargetReference)) {
                        graph.addEdge(nodeReference, callSiteTargetReference);
                    }
                }
            }

        }
        return graph;
    }

    /**
     * Validate that the command-line arguments obey the expected usage.
     * <p>
     * Usage: args[0] : "-jar"
     *        args[1] : String, path to jar to analyze
     *        args[2] : "-classpath"
     *        args[3] : String, a ";"-delimited class path
     *
     * @throws UnsupportedOperationException if command-line is malformed.
     */
    public static void validateCommandLine(String[] args) {
        if (args.length < 4) {
            throw new UnsupportedOperationException("must have at least 4 command-line arguments");
        }
        if (!args[0].equals("-jar")) {
            throw new UnsupportedOperationException("invalid command line, args[0] should be -jar, but is " + args[0]);
        }
        if (!args[2].equals("-classpath")) {
            throw new UnsupportedOperationException("invalid command-line, args[2] should be -classpath, but is " + args[2]);
        }
    }
}

