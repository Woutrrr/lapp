package nl.wvdzwan.timemachine.callgraph.outputs;

import com.ibm.wala.types.MethodReference;

public interface IGraphNode {

    MethodReference getMethodReference();

    void addAnnotation(NodeAnnotation annotation);
    boolean hasAnnotation(NodeAnnotation annotation);

    String prefix(String label);
}
