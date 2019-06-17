package nl.wvdzwan.lapp.convert.outputs;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.DOTExporter;

import nl.wvdzwan.lapp.LappPackageTransformer;
import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.convert.LappClassHierarchy;
import nl.wvdzwan.lapp.core.ClassRecord;
import nl.wvdzwan.lapp.core.LappPackage;
import nl.wvdzwan.lapp.core.Method;
import nl.wvdzwan.lapp.core.ResolvedMethod;
import nl.wvdzwan.lapp.core.UnresolvedMethod;
import nl.wvdzwan.lapp.protobuf.Lapp;
import nl.wvdzwan.lapp.protobuf.LappPackageReader;

import static nl.wvdzwan.lapp.call.Call.CallType.RESOLVED_DISPATCH;

public class ResolvedDispatchCallGraphExport extends GraphVizOutput {

    @Override
    public boolean export(OutputStream outputStream, Lapp.Package lappProto) {
        LappPackage lappPackage = LappPackageReader.from(lappProto);
        Graph<Method, Call> graph = LappPackageTransformer.toGraph(lappPackage);

        LappClassHierarchy cha = LappClassHierarchy.make(lappPackage);

        Set<Call> calls = graph.edgeSet();
        ArrayList<Call> copy = new ArrayList<>(calls);
        for (Call c : copy) {
            if (c.callType == Call.CallType.INTERFACE || c.callType == Call.CallType.VIRTUAL) {
                Set<ClassRecord> implementors = cha.getImplementingClasses(c.target.namespace, c.target.symbol);

                for (ClassRecord cr : implementors) {
                    ResolvedMethod dynamic_target = ResolvedMethod.findOrCreate(cr.name, c.target.symbol, cr.artifact);
                    graph.addVertex(dynamic_target);
                    graph.addEdge(c.target, dynamic_target, new Call(c.target, dynamic_target, RESOLVED_DISPATCH));
                }
            }
        }



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


    @Override
    public String vertexIdProvider(Method method) {
        String id;
        if (method instanceof ResolvedMethod) {
            id = resolvedMethodToLabel((ResolvedMethod) method);
        } else {
            id = unresolvedMethodToLabel((UnresolvedMethod) method);
        }

        return "\"" + id + "\"";
    }

    public String vertexLabelProvider(Method method) {

        String label = vertexIdProvider(method);

        if (method.metadata.containsKey("type")) {
            label = "" + method.metadata.get("type") + " - " + label;
        }
        return label;
    }

    public Map<String, Attribute> vertexAttributeProvider(Method vertex) {
        return mapAsAttributeMap(vertex.metadata);
    }

    private String resolvedMethodToLabel(ResolvedMethod method) {
        String separator = "::";
        return "mvn"
                + separator + method.artifact
                + separator + method.namespace
                + separator + method.symbol;
    }

    private String unresolvedMethodToLabel(UnresolvedMethod method) {
        return "mvn::__"
                + "::" + method.namespace
                + "::" + method.symbol;

    }
}
