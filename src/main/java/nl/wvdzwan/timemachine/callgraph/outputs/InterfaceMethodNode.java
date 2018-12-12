package nl.wvdzwan.timemachine.callgraph.outputs;

import com.ibm.wala.types.MethodReference;

public class InterfaceMethodNode extends MethodRefNode {

    InterfaceMethodNode(MethodReference method) {
        super(method);
    }

    @Override
    public String prefix(String label) {
        return "IMPLEMENTS_INTERFACE_METHOD_" + label;
    }

    @Override
    public int hashCode() {
        return 7 + super.hashCode();
    }
}
