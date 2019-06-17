package nl.wvdzwan.lapp.call;

import nl.wvdzwan.lapp.core.ClassRecord;

public class ChaEdge {

    public enum ChaEdgeType {

        EXTENDS("extends"),
        IMPLEMENTS("implements"),
        UNKNOWN("unknown");

        public final String label;

        ChaEdgeType(String label) {
            this.label = label;
        }
    }

    public final ChaEdgeType type;
    public final ClassRecord src;
    public final ClassRecord target;

    public ChaEdge(ChaEdgeType type, ClassRecord src, ClassRecord target) {
        this.type = type;
        this.src = src;
        this.target = target;
    }

    public String getLabel() {
        return type.label;
    }
}