package nl.wvdzwan.lapp.convert.outputs;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.io.DOTExporter;

import nl.wvdzwan.lapp.LappPackageTransformer;
import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.convert.LappClassHierarchy;
import nl.wvdzwan.lapp.core.ClassRecord;
import nl.wvdzwan.lapp.core.LappPackage;
import nl.wvdzwan.lapp.core.Method;
import nl.wvdzwan.lapp.core.ResolvedMethod;
import nl.wvdzwan.lapp.protobuf.Lapp;
import nl.wvdzwan.lapp.protobuf.LappPackageReader;

import static nl.wvdzwan.lapp.call.Call.CallType.RESOLVED_DISPATCH;

public class ResolvedDispatchCallGraphExport extends HumanReadableDotGraph {

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
                    if (c.source.toID().equals(dynamic_target.toID())) {
                        continue;
                    }
                    graph.addVertex(dynamic_target);
                    graph.addEdge(c.source, dynamic_target, new Call(c.target, dynamic_target, RESOLVED_DISPATCH, c.lineNumber, c.programCounter));
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
}
