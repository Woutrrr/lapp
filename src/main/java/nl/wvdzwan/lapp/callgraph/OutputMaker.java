package nl.wvdzwan.lapp.callgraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;

import nl.wvdzwan.lapp.LappPackageTransformer;
import nl.wvdzwan.lapp.call.Edge;
import nl.wvdzwan.lapp.callgraph.outputs.GraphVizOutput;
import nl.wvdzwan.lapp.callgraph.outputs.HumanReadableDotGraph;
import nl.wvdzwan.lapp.callgraph.outputs.JsonOutput;
import nl.wvdzwan.lapp.callgraph.outputs.ProtobufOutput;
import nl.wvdzwan.lapp.callgraph.outputs.UnifiedCallGraphExport;
import nl.wvdzwan.lapp.callgraph.outputs.resolved_methods.ResolvedMethodOutput;
import nl.wvdzwan.lapp.core.LappPackage;
import nl.wvdzwan.lapp.core.Method;

public class OutputMaker {
    private static Logger logger = LogManager.getLogger();

    public static void make(LappPackage lappPackage, File outputDirectory) {
        graphOutputs(lappPackage, outputDirectory);

        protoOutputs(lappPackage, outputDirectory);
    }

    private static void protoOutputs(LappPackage lappPackage, File outputDirectory) {
        try {
            ProtobufOutput protobufOutput = new ProtobufOutput(new FileOutputStream(new File(outputDirectory, "app.buf")));
            protobufOutput.export(lappPackage);

            JsonOutput jsonOutput = new JsonOutput(new FileOutputStream(new File(outputDirectory, "app.json")));
            jsonOutput.export(lappPackage);

        } catch (FileNotFoundException e) {
            logger.error("File not found: {}", e.getMessage());
        }
    }

    public static void graphOutputs(LappPackage lappPackage, File outputDirectory)  {
        Graph<Method, Edge> graph = LappPackageTransformer.toGraph(lappPackage);

        try {
            GraphVizOutput dotOutput = new UnifiedCallGraphExport(graph);
            FileWriter writer = new FileWriter(new File(outputDirectory, "app.dot"));
            dotOutput.export(writer);

            GraphVizOutput humanOutput = new HumanReadableDotGraph(graph);
            FileWriter writerHuman = new FileWriter(new File(outputDirectory, "app_human.dot"));
            humanOutput.export(writerHuman);

            ResolvedMethodOutput resolvedMethodOutput = new ResolvedMethodOutput(new FileWriter(new File(outputDirectory, "resolved_methods.txt")));
            resolvedMethodOutput.export(graph);

        } catch (IOException e) {
            logger.error("IOException: {}", e.getMessage());
        }
    }
}
