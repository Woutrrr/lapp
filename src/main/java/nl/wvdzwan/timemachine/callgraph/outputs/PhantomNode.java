package nl.wvdzwan.timemachine.callgraph.outputs;

import com.ibm.wala.types.MethodReference;

public class PhantomNode extends MethodRefNode {

    PhantomNode(MethodReference method) {
        super(method);
    }

    @Override
    public String prefix(String label) {
        return "PHANTOM_" + label;
    }
}
