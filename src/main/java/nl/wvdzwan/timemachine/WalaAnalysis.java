
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
package nl.wvdzwan.timemachine;

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
import com.ibm.wala.util.collections.CollectionFilter;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;
import com.ibm.wala.util.graph.impl.SlowSparseNumberedGraph;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.strings.Atom;
import com.ibm.wala.viz.DotUtil;
import com.ibm.wala.util.warnings.Warnings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This simple example WALA application builds a TypeHierarchy and fires off
 * ghostview to viz a DOT representation.
 *
 * @author sfink
 */
public class WalaAnalysis {
    // This example takes one command-line argument, so args[1] should be the "-classpath" parameter
    final static int CLASSPATH_INDEX = 1;


    public static void main(String[] args) throws IOException {
        run(args);
    }

    public static Process run(String[] args) throws IOException {
        try {
            validateCommandLine(args);
            String classpath = args[CLASSPATH_INDEX];
            AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(
                    classpath,
                    (new FileProvider()).getFile("Java60RegressionExclusions.txt"));

            // invoke WALA to build a class hierarchy
            ClassHierarchy cha = ClassHierarchyFactory.make(scope);

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

            Graph<MethodReference> methodGraph = outputcg(cg);

            DotUtil.writeDotFile(methodGraph, null, null, "output/cg.dot");


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
        CGNode node;
        CallSiteReference callsite;

        while(cgIterator.hasNext()) {
            node = cgIterator.next();

            if (node.getMethod().getDeclaringClass().getClassLoader().getName().equals(Atom.findOrCreateUnicodeAtom("Primordial"))) {
//            if (node.getMethod().getDeclaringClass().getReference().getName().getPackage().startsWith(Atom.findOrCreateUnicodeAtom("java"))) {
                continue;
            }

            graph.addNode(node.getMethod().getReference());



            for(Iterator<CallSiteReference> callsites = node.iterateCallSites(); callsites.hasNext();) {
                callsite = callsites.next();
                graph.addNode(callsite.getDeclaredTarget());

                if (!graph.hasEdge(node.getMethod().getReference(), callsite.getDeclaredTarget())) {
                    graph.addEdge(node.getMethod().getReference(), callsite.getDeclaredTarget());
                }
            }

        }
        return graph;
    }

    /**
     * Validate that the command-line arguments obey the expected usage.
     * <p>
     * Usage: args[0] : "-classpath" args[1] : String, a ";"-delimited class path
     *
     * @throws UnsupportedOperationException if command-line is malformed.
     */
    public static void validateCommandLine(String[] args) {
        if (args.length < 2) {
            throw new UnsupportedOperationException("must have at least 2 command-line arguments");
        }
        if (!args[0].equals("-classpath")) {
            throw new UnsupportedOperationException("invalid command-line, args[0] should be -classpath, but is " + args[0]);
        }
    }



}

