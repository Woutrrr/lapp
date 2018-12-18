package nl.wvdzwan.timemachine.callgraph.outputs;

import java.util.HashSet;
import java.util.Set;

import com.ibm.wala.types.MethodReference;

public class MethodRefNode implements IGraphNode {

    private final MethodReference method;
    private Set<NodeAnnotation> annotations = new HashSet<>();

    MethodRefNode(MethodReference method) {
        this.method = method;
    }

    MethodRefNode(MethodReference method, NodeAnnotation annotation) {
        this(method);
        addAnnotation(annotation);
    }

    public void addAnnotation(NodeAnnotation annotation) {
        annotations.add(annotation);
    }

    @Override
    public boolean hasAnnotation(NodeAnnotation annotation) {
        return annotations.contains(annotation);
    }

    @Override
    public MethodReference getMethodReference() {
        return method;
    }

    @Override
    public String prefix(String label) {
        for(NodeAnnotation annotation : annotations) {
            label = annotation.annotate(label);
        }
        return label;
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MethodRefNode && method.equals(((MethodRefNode) o).method);
    }

    @Override
    public String toString() {
        return prefix(method.toString());
    }


}
