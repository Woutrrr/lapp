package nl.wvdzwan.lapp.call;

import nl.wvdzwan.lapp.Method.Method;

public class UnresolvedReversedCall extends Call {
    protected UnresolvedReversedCall(Method source, Method callee, String label) {
        super(source, callee, label);
    }
}
