package nl.wvdzwan.lapp.call;

import nl.wvdzwan.lapp.core.Method;
import nl.wvdzwan.lapp.core.ResolvedMethod;

public class ChaEdge extends Edge {

    public enum ChaEdgeType {

        OVERRIDE("overridden by"),
        IMPLEMENTS("implemented by"),
        UNKNOWN("unknown");

        public final String label;

        ChaEdgeType(String label) {
            this.label = label;
        }
    }

    public final ChaEdgeType type;

    public ChaEdge(Method related, ResolvedMethod subject, ChaEdgeType type) {
        super(related, subject);
        this.type = type;
    }

    @Override
    public String getLabel() {
        return type.label;
    }
}