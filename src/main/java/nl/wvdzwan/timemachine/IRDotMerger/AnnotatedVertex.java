package nl.wvdzwan.timemachine.IRDotMerger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jgrapht.io.Attribute;

public class AnnotatedVertex {
    private String name;
    private Map<String, Attribute> attributesMap;

    public AnnotatedVertex(String name, Map<String, Attribute> attributes) {
        Objects.requireNonNull(name);

        this.name = name;
        this.attributesMap = new HashMap<>();

        if (attributes != null) {
            this.attributesMap.putAll(attributes);
        }
    }

    public void mergeAttributes(Map<String, Attribute> attributes) {
        for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
            attributesMap.merge(
                    entry.getKey(),
                    entry.getValue(),
                    (a, a2) -> a.getValue().length() >= a2.getValue().length() ? a : a2);
        }
    }

    public String getName() {
        return name;
    }

    public Map<String, Attribute> getAttributes() { return attributesMap; }

    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof AnnotatedVertex) && name.equals(((AnnotatedVertex) obj).name);
    }
}
