package nl.wvdzwan.timemachine.IRDotMerger;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;

import org.jgrapht.Graph;
import org.jgrapht.io.ImportException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.wvdzwan.timemachine.callgraph.outputs.GraphEdge;

class IRDotMergerTest {

    @Test
    void mergeTwoGraphs() throws IOException, ImportException {

        File file1 = new File(getClass().getClassLoader().getResource("mergedreader-test-data/app.dot").getFile());
        File file2 = new File(getClass().getClassLoader().getResource("mergedreader-test-data/core.dot").getFile());

        File combinedFile = new File(getClass().getClassLoader().getResource("ir-merger-data/merged.dot").getFile());
        String expected = new String(Files.readAllBytes(combinedFile.toPath()));


        Graph<AnnotatedVertex, GraphEdge> graph = IRDotMerger.importGraphs(file1, file2);
        StringWriter output = new StringWriter();
        IRDotMerger.exportGraph(graph, output);


        Assertions.assertEquals(expected, output.toString());

    }

}