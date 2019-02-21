package nl.wvdzwan.lapp.callgraph;

import com.ibm.wala.types.MethodReference;
import org.jgrapht.Graph;

import nl.wvdzwan.lapp.IRDotMerger.AnnotatedVertex;
import nl.wvdzwan.lapp.callgraph.outputs.GraphEdge;

public interface IRGraph {

    AnnotatedVertex addVertex(MethodReference reference);

    AnnotatedVertex addTypedVertex(MethodReference nodeReference, String vertexType);

    boolean addEdge(AnnotatedVertex nodeVertex, AnnotatedVertex targetVertex, GraphEdge edge);

    Graph<AnnotatedVertex, GraphEdge> getInnerGraph();
}
