package nl.wvdzwan.lapp.resolver;

import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;

import java.util.List;

public class OptionalDependencyFilter implements DependencyFilter {

    @Override
    public boolean accept(DependencyNode node, List<DependencyNode> parents) {

        Dependency dependency = node.getDependency();

        if (dependency == null) {
            return false;
        }

        return !dependency.isOptional();
    }
}
