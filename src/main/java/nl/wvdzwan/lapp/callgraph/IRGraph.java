package nl.wvdzwan.lapp.callgraph;

import java.util.List;
import java.util.Set;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.MethodReference;
import org.jgrapht.Graph;

import nl.wvdzwan.lapp.IRDotMerger.AnnotatedVertex;
import nl.wvdzwan.lapp.callgraph.outputs.GraphEdge;

public interface IRGraph {

    List<DynamicEdge> getDynamicEdges();

    Set<AnnotatedVertex> getExternalNodes();

    enum MethodType {
        INTERFACE, ABSTRACT, IMPLEMENTATION
    }

    AnnotatedVertex addVertex(MethodReference reference);

    AnnotatedVertex addTypedVertex(MethodReference nodeReference, MethodType type);
    AnnotatedVertex addTypedVertex(IMethod method, MethodType type);

    boolean addEdge(AnnotatedVertex nodeVertex, AnnotatedVertex targetVertex, GraphEdge edge);

    Graph<AnnotatedVertex, GraphEdge> getInnerGraph();
}
