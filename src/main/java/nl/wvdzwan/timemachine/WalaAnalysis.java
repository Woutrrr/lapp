
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

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.CollectionFilter;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;
import com.ibm.wala.util.graph.impl.SlowSparseNumberedGraph;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.strings.Atom;
import com.ibm.wala.viz.DotUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * This simple example WALA application builds a TypeHierarchy and fires off
 * ghostview to viz a DOT representation.
 *
 * @author sfink
 */
public class WalaAnalysis {
    // This example takes one command-line argument, so args[1] should be the "-classpath" parameter
    final static int CLASSPATH_INDEX = 1;

    public final static String DOT_FILE = "temp.dt";

    private final static String PDF_FILE = "th.pdf";



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

//            Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, cha);
            ArrayList<Entrypoint> entrypoints = new ArrayList<>();

            IMethod method;
            for (IClass klass : cha) {
                // klass is not an interface and it's application class
                if ((!klass.isInterface()) && (cha.getScope().getApplicationLoader().equals(klass.getClassLoader().getReference()))) {

                    for (Iterator m_iter = klass.getDeclaredMethods().iterator(); m_iter.hasNext(); ) {
                        method = (IMethod) m_iter.next();

                        entrypoints.add(new DefaultEntrypoint(method, cha));
                    }
                }
            }

            //
            // analysis options controls aspects of call graph construction
            //
            AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
            AnalysisCache cache = new AnalysisCacheImpl();

            //
            // build the call graph
            //
//            CallGraphBuilder builder = Util.makeZeroCFABuilder(Language.JAVA, options, cache, cha, scope);

            CallGraphBuilder builder = Util.makeRTABuilder(options, cache, cha, scope);
            CallGraph cg = builder.makeCallGraph(options, null);

            Graph<IMethod> methodGraph = outputcg(cg);

            DotUtil.writeDotFile(methodGraph, null, null, "output/cg.dot");


            return null;


        } catch (WalaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (CallGraphBuilderCancelException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Graph<IMethod> outputcg(CallGraph cg) {
        Graph<IMethod> graph = SlowSparseNumberedGraph.make();
        Iterator<CGNode> cgIterator = cg.iterator();
        CGNode node;
        CGNode sub_node;
        while(cgIterator.hasNext()) {
            node = cgIterator.next();

            if (node.getMethod().getDeclaringClass().getClassLoader().getName().equals(Atom.findOrCreateUnicodeAtom("Primordial"))) {
//            if (node.getMethod().getDeclaringClass().getReference().getName().getPackage().startsWith(Atom.findOrCreateUnicodeAtom("java"))) {
                continue;
            }

            graph.addNode(node.getMethod());



            for(Iterator<CGNode> succ_nodes = cg.getSuccNodes(node); succ_nodes.hasNext();) {
                sub_node = succ_nodes.next();
                graph.addNode(sub_node.getMethod());

                if (!graph.hasEdge(node.getMethod(), sub_node.getMethod())) {
                    graph.addEdge(node.getMethod(), sub_node.getMethod());
                }
            }

        }
        return graph;
    }

    public static <T> Graph<T> pruneGraph(Graph<T> g, Predicate<T> f) {
        Collection<T> slice = GraphSlicer.slice(g, f);
        return GraphSlicer.prune(g, new CollectionFilter<>(slice));
    }

    /**
     * Restrict g to nodes from the Application loader
     */
    public static Graph<IClass> pruneForAppLoader(Graph<IClass> g) {
        Predicate<IClass> f = c -> (c.getClassLoader().getReference().equals(ClassLoaderReference.Application));
        return pruneGraph(g, f);
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

    /**
     * Return a view of an {@link IClassHierarchy} as a {@link Graph}, with edges from classes to immediate subtypes
     */
    public static Graph<IClass> typeHierarchy2Graph(IClassHierarchy cha) {
        Graph<IClass> result = SlowSparseNumberedGraph.make();
        for (IClass c : cha) {
            result.addNode(c);
        }
        for (IClass c : cha) {

            for (IClass x : cha.getImmediateSubclasses(c)) {
                result.addEdge(c, x);
            }
            if (c.isInterface()) {
                for (IClass x : cha.getImplementors(c.getReference())) {
                    result.addEdge(c, x);
                }
            }
        }
        return result;
    }


}

