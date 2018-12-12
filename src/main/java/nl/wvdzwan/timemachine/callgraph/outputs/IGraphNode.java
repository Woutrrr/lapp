package nl.wvdzwan.timemachine.callgraph.outputs;

import com.ibm.wala.types.MethodReference;

public interface IGraphNode {

    MethodReference getMethodReference();

    String prefix(String label);
}
