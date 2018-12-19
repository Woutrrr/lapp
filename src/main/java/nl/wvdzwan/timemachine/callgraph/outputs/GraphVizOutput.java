package nl.wvdzwan.timemachine.callgraph.outputs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.function.Predicate;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.MethodReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphExporter;

public class GraphVizOutput implements CallgraphOutputTask<CallGraph> {
    private static Logger logger = LogManager.getLogger();

    private File output;
    private String repositoryPathPrefix;
    private GraphVizOutputTransformer transformer;

    public GraphVizOutput(
            File output,
            Predicate<CGNode> nodeFilter,
            String repositoryPathPrefix) {

        this.output = output;
        this.repositoryPathPrefix = repositoryPathPrefix;

        transformer = new GraphVizOutputTransformer(nodeFilter);
    }

    @Override
    public boolean makeOutput(CallGraph cg, IClassHierarchy extendedCha) {

        Graph<MethodReference, GraphEdge> methodGraph = transformer.transform(cg, extendedCha);

        GraphExporter<MethodReference, GraphEdge> exporter = new CustomDotExporter(transformer.getVertexAttributeMapMap());

        try {
            Writer writer = new FileWriter(output.getAbsolutePath());
            exporter.exportGraph(methodGraph, writer);
        } catch (IOException | ExportException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
