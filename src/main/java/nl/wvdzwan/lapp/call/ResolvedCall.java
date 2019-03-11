package nl.wvdzwan.lapp.call;

import nl.wvdzwan.lapp.Method.ResolvedMethod;

public class ResolvedCall extends Call {


    public ResolvedCall(ResolvedMethod source, ResolvedMethod callee, String label) {
        super(source, callee, label);
    }


}
