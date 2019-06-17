package nl.wvdzwan.lapp.convert.outputs;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.DefaultAttribute;

import nl.wvdzwan.lapp.LappPackageTransformer;
import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.call.Edge;
import nl.wvdzwan.lapp.core.Method;
import nl.wvdzwan.lapp.protobuf.Lapp;

public abstract class GraphVizOutput implements LappPackageOutput {
    private static Logger logger = LogManager.getLogger();

    public boolean export(OutputStream outputStream, Lapp.Package lappProto) {

        Graph<Method, Call> graph = LappPackageTransformer.toGraph(lappProto);

        DOTExporter<Method, Call> exporter = new DOTExporter<>(
                this::vertexIdProvider,
                this::vertexLabelProvider,
                this::edgeLabelProvider,
                this::vertexAttributeProvider,
                this::edgeAttributeProvider);


        setGraphAttributes(exporter);

        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        exporter.exportGraph(graph, writer);

        return true;
    }

    protected void setGraphAttributes(DOTExporter<Method, Call> exported) {
        // No graph attributes by default
    }

    abstract String vertexIdProvider(Method vertex);

    abstract String vertexLabelProvider(Method vertex);

    abstract Map<String, Attribute> vertexAttributeProvider(Method vertex);

    protected String edgeLabelProvider(Edge edge) {
        return edge.getLabel();
    }

    protected Map<String, Attribute> mapAsAttributeMap(Map<String, String> metadata) {
        Map<String, Attribute> result = new HashMap<>();


        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            result.put(
                    entry.getKey(),
                    DefaultAttribute.createAttribute(entry.getValue())
            );
        }

        return result;
    }


    protected Map<String, Attribute> edgeAttributeProvider(Edge edge) {

            return callAttributeProvider((Call) edge);

    }

    private Map<String, Attribute> callAttributeProvider(Call call) {
        Map<String, Attribute> attributes = new HashMap<>();

        switch (call.callType) {

            case INTERFACE:

                attributes.put("style", DefaultAttribute.createAttribute("bold"));
                break;
            case VIRTUAL:
                attributes.put("style", DefaultAttribute.createAttribute("bold"));
                break;
            case SPECIAL:
                break;
            case STATIC:
                break;
            case RESOLVED_DISPATCH:
                attributes.put("style", DefaultAttribute.createAttribute("dashed"));
                break;
            case UNKNOWN:
                break;
        }
        return attributes;
    }

}
