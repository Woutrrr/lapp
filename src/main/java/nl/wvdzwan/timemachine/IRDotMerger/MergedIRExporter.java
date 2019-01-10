package nl.wvdzwan.timemachine.IRDotMerger;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.io.Attribute;
import org.jgrapht.io.DefaultAttribute;

import nl.wvdzwan.timemachine.callgraph.outputs.GraphEdge;

public class MergedIRExporter {


        protected static String vertexIdProvider(AnnotatedVertex vertex) {
            return "\"" + vertex.getName() + "\""; //.replaceAll("[\\.\\(\\)<>/;]", "_");
        }

        protected static String vertexLabelProvider(AnnotatedVertex vertex) {
            String label = vertex.getName();

            Map<String, Attribute> attributes = vertex.getAttributes();
            if (attributes != null && attributes.containsKey("type")) {
                label = "" + attributes.get("type").getValue() + " - " + label;
            }
            return label;
        }

        protected static Map<String, Attribute> vertexAttributeProvider(AnnotatedVertex vertex) {
            return vertex.getAttributes();
        }

        public static String edgeLabelProvider(GraphEdge edge) {
            return edge.getLabel();
        }

        public static Map<String, Attribute> edgeAttributeProvider(GraphEdge edge) {
            Map<String, Attribute> attributes = new HashMap<>();

            if (edge instanceof GraphEdge.InterfaceDispatchEdge) {
                Attribute attribute = DefaultAttribute.createAttribute("bold");
                attributes.put("style", attribute);
            } else if (edge instanceof GraphEdge.VirtualDispatchEdge) {
                Attribute attribute = DefaultAttribute.createAttribute("bold");
                attributes.put("style", attribute);
            } else if (edge instanceof GraphEdge.ImplementsEdge) {
                Attribute attribute = DefaultAttribute.createAttribute("dashed");
                attributes.put("style", attribute);
            } else if (edge instanceof GraphEdge.OverridesEdge) {
                Attribute attribute = DefaultAttribute.createAttribute("dotted");
                attributes.put("style", attribute);
            } else {
                return null;
            }

            return attributes;
        }

    }