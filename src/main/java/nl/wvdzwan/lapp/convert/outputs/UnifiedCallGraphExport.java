package nl.wvdzwan.lapp.convert.outputs;

import java.util.Map;

import org.jgrapht.io.Attribute;

import nl.wvdzwan.lapp.core.Method;
import nl.wvdzwan.lapp.core.ResolvedMethod;
import nl.wvdzwan.lapp.core.UnresolvedMethod;

public class UnifiedCallGraphExport extends GraphVizOutput {

    public final static String SEPARATOR = ":";
    @Override
    public String vertexIdProvider(Method method) {
        String id;
        if (method instanceof ResolvedMethod) {
            id = resolvedMethodToID((ResolvedMethod) method);
        } else {
            id = unresolvedMethodToID((UnresolvedMethod) method);
        }

        return "\"" + id + "\"";
    }

    public String vertexLabelProvider(Method method) {

        String id;
        if (method instanceof ResolvedMethod) {
            id = resolvedMethodToLabel((ResolvedMethod) method);
        } else {
            id = unresolvedMethodToLabel((UnresolvedMethod) method);
        }

        return id;
    }

    public Map<String, Attribute> vertexAttributeProvider(Method vertex) {
        return mapAsAttributeMap(vertex.metadata);
    }

    private String resolvedMethodToLabel(ResolvedMethod method) {

        return  method.artifact
                + SEPARATOR + method.namespace
                + SEPARATOR + "\n" + method.symbol;
    }

    private String resolvedMethodToID(ResolvedMethod method) {

        return  method.artifact
                + SEPARATOR + method.namespace
                + SEPARATOR + method.symbol;
    }

    private String unresolvedMethodToLabel(UnresolvedMethod method) {
        return "__"
                + SEPARATOR + method.namespace
                + SEPARATOR + "\n" + method.symbol;

    }

    private String unresolvedMethodToID(UnresolvedMethod method) {
        return "__"
                + SEPARATOR + method.namespace
                + SEPARATOR + method.symbol;

    }
}
