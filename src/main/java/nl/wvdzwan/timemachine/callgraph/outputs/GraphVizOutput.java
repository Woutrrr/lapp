package nl.wvdzwan.timemachine.callgraph.outputs;

import java.io.File;
import java.util.function.Predicate;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.viz.DotUtil;
import com.ibm.wala.viz.NodeDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.wvdzwan.timemachine.callgraph.GlobalUniqueSymbolDecorator;
import nl.wvdzwan.timemachine.callgraph.MavenFolderLayout;

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

        IClassHierarchy cha = (extendedCha != null) ? extendedCha : cg.getClassHierarchy();

        Graph<IGraphNode> methodGraph = transformer.transform(cg);

        NodeDecorator<IGraphNode> labelDecorator = new GlobalUniqueSymbolDecorator(
                cha,
                new MavenFolderLayout(repositoryPathPrefix)
        );

        try {
            DotUtil.writeDotFile(methodGraph, labelDecorator, "\", splines=true, overlap=false, ranksep=5, fontsize=36, root =  \"", output.getAbsolutePath());
        } catch (WalaException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
