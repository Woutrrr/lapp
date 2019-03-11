package nl.wvdzwan.lapp.graph;

import org.jgrapht.graph.DefaultEdge;

public class LappEdge extends DefaultEdge {
    private String label;

    /**
     * Constructs a relationship edge
     *
     * @param label the label of the new edge.
     */
    public LappEdge(String label) {
        this.label = label;
    }

    /**
     * Gets the label associated with this edge.
     *
     * @return edge label
     */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "(" + getSource() + " : " + getTarget() + " : " + label + ")";
    }


    public static class ChaEdge extends LappEdge {

        /**
         * Constructs a relationship edge
         *
         * @param label the label of the new edge.
         */
        public ChaEdge(String label) {
            super(label);
        }
    }

    public static class CgEdge extends LappEdge {

        /**
         * Constructs a relationship edge
         *
         * @param label the label of the new edge.
         */
        public CgEdge(String label) {
            super(label);
        }
    }
}
