package nl.wvdzwan.lapp;

import org.jgrapht.graph.DefaultDirectedGraph;

import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.call.ChaEdge;
import nl.wvdzwan.lapp.call.Edge;
import nl.wvdzwan.lapp.core.LappPackage;
import nl.wvdzwan.lapp.core.Method;
import nl.wvdzwan.lapp.protobuf.Lapp;
import nl.wvdzwan.lapp.protobuf.LappPackageReader;

public class LappPackageTransformer {

    public static DefaultDirectedGraph<Method, Edge> toGraph(LappPackage lappPackage) {

        DefaultDirectedGraph<Method, Edge> graph = new DefaultDirectedGraph<>(Edge.class);

        for (Method m : lappPackage.methods) {
            graph.addVertex(m);
        }

        for (Call c : lappPackage.resolvedCalls) {
            graph.addVertex(c.source);
            graph.addVertex(c.target);

            graph.addEdge(c.source, c.target, c);
        }

        for (Call c : lappPackage.unresolvedCalls) {
            graph.addVertex(c.source);
            graph.addVertex(c.target);

            graph.addEdge(c.source, c.target, c);
        }

        for (ChaEdge c : lappPackage.cha) {
            graph.addVertex(c.source);
            graph.addVertex(c.target);

            graph.addEdge(c.source, c.target, c);
        }

        return graph;

    }

    public static DefaultDirectedGraph<Method, Edge> toGraph(Lapp.Package lappProto) {
        LappPackage lappPackage = LappPackageReader.from(lappProto);

        return toGraph(lappPackage);
    }
}
