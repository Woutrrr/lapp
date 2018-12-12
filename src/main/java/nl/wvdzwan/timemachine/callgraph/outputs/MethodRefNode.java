package nl.wvdzwan.timemachine.callgraph.outputs;

import com.ibm.wala.types.MethodReference;

public class MethodRefNode implements IGraphNode {


    private final MethodReference method;

    MethodRefNode(MethodReference method) {
        this.method = method;
    }

    @Override
    public MethodReference getMethodReference() {
        return method;
    }

    @Override
    public String prefix(String label) {
        return label;
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }

    @Override
    public boolean equals(Object o) {

        return o instanceof MethodRefNode && method.equals(((MethodRefNode) o).method);
    }

    @Override
    public String toString() {
        return prefix(method.toString());
    }


}
