package nl.wvdzwan.lapp.convert.outputs;

import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.io.DOTExporter;

import nl.wvdzwan.lapp.call.ChaEdge;
import nl.wvdzwan.lapp.convert.LappClassHierarchy;
import nl.wvdzwan.lapp.core.ClassRecord;
import nl.wvdzwan.lapp.core.LappPackage;
import nl.wvdzwan.lapp.protobuf.Lapp;
import nl.wvdzwan.lapp.protobuf.LappPackageReader;

public class ClassHierarchyDotGraph implements LappPackageOutput {
    @Override
    public boolean export(OutputStream outputStream, Lapp.Package lappPackage) {

        LappPackage lapp = LappPackageReader.from(lappPackage);

        LappClassHierarchy cha = LappClassHierarchy.make(lapp);

        DefaultDirectedGraph<ClassRecord, ChaEdge> graph = cha.getGraph();

        DOTExporter<ClassRecord, ChaEdge> exporter = new DOTExporter<>(
                // vertexIDProvider
                classRecord -> {
                    return "\"" + classRecord.artifact + ":" + classRecord.name +"\"";
                },
                // vertexLabelProvider
                classRecord -> classRecord.artifact + ":" + classRecord.name ,
                //this::edgeLabelProvider
                chaEdge -> chaEdge.getLabel(),
                null,
                null);


        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        exporter.exportGraph(graph, writer);

        return true;
    }

}
