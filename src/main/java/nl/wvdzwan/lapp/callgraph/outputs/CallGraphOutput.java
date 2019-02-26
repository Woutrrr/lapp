package nl.wvdzwan.lapp.callgraph.outputs;

import nl.wvdzwan.lapp.callgraph.IRGraph;

public interface CallGraphOutput {

    boolean export(IRGraph graph);
}
