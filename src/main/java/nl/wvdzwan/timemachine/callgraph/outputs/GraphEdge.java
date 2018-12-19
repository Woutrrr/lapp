package nl.wvdzwan.timemachine.callgraph.outputs;

import org.jgrapht.graph.DefaultEdge;

public abstract class GraphEdge extends DefaultEdge {

    protected abstract String getLabel();


    public static abstract class DispatchEdge extends GraphEdge {
    }

    public static class InterfaceDispatchEdge extends DispatchEdge {

        @Override
        protected String getLabel() {
            return "invoke_interface";
        }
    }

    public static class VirtualDispatchEdge extends DispatchEdge {

        @Override
        protected String getLabel() {
            return "invoke_virtual";
        }
    }

    public static class SpecialDispatchEdge extends DispatchEdge {

        @Override
        protected String getLabel() {
            return "invoke_special";
        }
    }

    public static class StaticDispatchEdge extends DispatchEdge {

        @Override
        protected String getLabel() {
            return "invoke_static";
        }
    }



    public static abstract class ClassHierarchyEdge extends GraphEdge {

    }

    public static class OverridesEdge extends ClassHierarchyEdge {

        @Override
        protected String getLabel() {
            return "overridden by";
        }
    }

    public static class ImplementsEdge extends ClassHierarchyEdge {

        @Override
        protected String getLabel() {
            return "implemented by";
        }
    }




}
