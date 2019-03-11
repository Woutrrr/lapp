package nl.wvdzwan.lapp.call;

import nl.wvdzwan.lapp.Method.ResolvedMethod;
import nl.wvdzwan.lapp.Method.UnresolvedMethod;

public class UnresolvedCall extends Call {


    public UnresolvedCall(ResolvedMethod source, UnresolvedMethod callee, String label) {
        super(source, callee, label);
    }
}
