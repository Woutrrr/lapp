package nl.wvdzwan.lapp.call;

import java.util.Objects;

import nl.wvdzwan.lapp.Method.Method;
import nl.wvdzwan.lapp.Method.ResolvedMethod;

public class Call extends Edge {

    public enum CallType {
        INTERFACE("invoke_interface"),
        VIRTUAL("invoke_virtual"),
        SPECIAL("invoke_special"),
        STATIC("invoke_static"),
        UNKNOWN("unknown");

        public final String label;

        CallType(String label) {
            this.label = label;
        }

    }
    public final CallType callType;

    public Call(Method source, Method callee, CallType callType) {
        super(source, callee);
        this.callType = callType;
    }

    @Override
    public String getLabel() {
        return callType.label;
    }

    public boolean isResolved() {
        return source instanceof ResolvedMethod && target instanceof ResolvedMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Call call = (Call) o;
        return callType == call.callType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), callType);
    }
}
