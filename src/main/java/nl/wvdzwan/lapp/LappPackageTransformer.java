package nl.wvdzwan.lapp;

import org.jgrapht.graph.DefaultDirectedGraph;

import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.core.ClassRecord;
import nl.wvdzwan.lapp.core.LappPackage;
import nl.wvdzwan.lapp.core.Method;
import nl.wvdzwan.lapp.core.ResolvedMethod;
import nl.wvdzwan.lapp.protobuf.Lapp;
import nl.wvdzwan.lapp.protobuf.LappPackageReader;

public class LappPackageTransformer {

    public static DefaultDirectedGraph<Method, Call> toGraph(LappPackage lappPackage) {

        DefaultDirectedGraph<Method, Call> graph = new DefaultDirectedGraph<>(Call.class);

        for(ClassRecord cr : lappPackage.classRecords) {
            for(String method : cr.methods) {
                ResolvedMethod resolvedMethod = ResolvedMethod.findOrCreate(cr.name, method, cr.artifact);
                graph.addVertex(resolvedMethod);
            }
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

//        for (ChaEdge c : lappPackage.cha) {
//            graph.addVertex(c.source);
//            graph.addVertex(c.target);
//
//            graph.addEdge(c.source, c.target, c);
//        }
        // TODO insert class hierarchy
//        for (Lapp.ClassRecord c : lappPackage.classRecords) {
//            graph.addVertex()
//        }

        return graph;

    }

    public static DefaultDirectedGraph<Method, Call> toGraph(Lapp.Package lappProto) {
        LappPackage lappPackage = LappPackageReader.from(lappProto);

        return toGraph(lappPackage);
    }
}
