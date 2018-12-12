package nl.wvdzwan.timemachine.callgraph.outputs;

import com.ibm.wala.types.MethodReference;

public class InvokeInterfaceNode extends MethodRefNode {

    InvokeInterfaceNode(MethodReference method) {
        super(method);
    }

    @Override
    public String prefix(String label) {
        return "INVOKE_INTERFACE_" + label;
    }
}
