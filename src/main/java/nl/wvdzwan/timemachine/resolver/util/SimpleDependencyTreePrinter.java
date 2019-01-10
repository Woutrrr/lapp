package nl.wvdzwan.timemachine.resolver.util;


import java.io.PrintStream;

import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;

import nl.wvdzwan.timemachine.callgraph.ArtifactRecord;

/**
 * A dependency visitor that writes the dependency tree to a file
 */
public class SimpleDependencyTreePrinter implements DependencyVisitor {

    private PrintStream out;

    private String indent = "";

    public SimpleDependencyTreePrinter(PrintStream out) {
        this.out = out;
    }

    public boolean visitEnter(DependencyNode node) {
        out.println(indent + formatNode(node));
        indent += "  ";

        return true;
    }

    private String formatNode(DependencyNode node) {
        return ArtifactRecord.getIdentifier(node.getArtifact());
    }

    public boolean visitLeave(DependencyNode node) {
        indent = indent.substring(2);

        return true;
    }


}
