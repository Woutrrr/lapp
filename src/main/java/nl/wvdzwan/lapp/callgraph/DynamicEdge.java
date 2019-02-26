package nl.wvdzwan.lapp.callgraph;

import java.util.Objects;

import nl.wvdzwan.lapp.IRDotMerger.AnnotatedVertex;
import nl.wvdzwan.lapp.callgraph.outputs.GraphEdge;

public class DynamicEdge {

    protected final AnnotatedVertex src;
    protected final AnnotatedVertex dst;
    protected final GraphEdge edgeType;




    public DynamicEdge(AnnotatedVertex src, AnnotatedVertex dst, GraphEdge edgeType) {
        this.src = src;
        this.dst = dst;
        this.edgeType = edgeType;
    }

    public AnnotatedVertex getSrc() {
        return src;
    }

    public AnnotatedVertex getDst() {
        return dst;
    }

    public GraphEdge getEdgeType() {
        return edgeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(src, dst, edgeType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamicEdge that = (DynamicEdge) o;
        return Objects.equals(src, that.src) &&
                Objects.equals(dst, that.dst) &&
                Objects.equals(edgeType, that.edgeType);
    }
}
