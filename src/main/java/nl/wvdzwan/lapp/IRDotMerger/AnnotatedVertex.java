package nl.wvdzwan.lapp.IRDotMerger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.DefaultAttribute;

import nl.wvdzwan.lapp.callgraph.ArtifactRecord;

@Deprecated
public class AnnotatedVertex {

    private final static String separator = "::";
    private final static String ecosystem = "mvn";

    final private static HashMap<String, AnnotatedVertex> dictionary = new LinkedHashMap<>();

    private ArtifactRecord artifactRecord;
    private String namespace;
    private String symbol;

    private Map<String, Attribute> attributesMap;

    private AnnotatedVertex(ArtifactRecord artifact, String namespace, String symbol) {
        this.artifactRecord = artifact;
        this.namespace = namespace;
        this.symbol = symbol;
        this.attributesMap = new HashMap<>();
    }

    public static synchronized AnnotatedVertex findOrCreate(ArtifactRecord artifact, String namespace, String symbol) {
        Objects.requireNonNull(artifact);
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(symbol);

        String key = toGlobalIdentifier(artifact, namespace, symbol);

        AnnotatedVertex val = dictionary.get(key);
        if (val != null) {
            return val;
        }

        val = new AnnotatedVertex(artifact, namespace, symbol);
        dictionary.put(key, val);
        return val;
    }


    public void mergeAttributes(Map<String, Attribute> attributes) {
        for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
            attributesMap.merge(
                    entry.getKey(),
                    entry.getValue(),
                    (a, a2) -> a.getValue().length() >= a2.getValue().length() ? a : a2);
        }
    }

    public boolean setAttribute(String key, String value) {
        return this.attributesMap.put(key, DefaultAttribute.createAttribute(value)) == null;
    }

    public ArtifactRecord getArtifactRecord() {
        return artifactRecord;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getSymbol() {
        return symbol;
    }

    public Map<String, Attribute> getAttributes() {
        return attributesMap;
    }

    public String toGlobalIdentifier() {
        return toGlobalIdentifier(artifactRecord, namespace, symbol);
    }

    private static String toGlobalIdentifier(ArtifactRecord artifactRecord, String namespace, String symbol) {
        return ecosystem
                + separator + artifactRecord.getUnversionedIdentifier()
                + separator + artifactRecord.getVersion()
                + separator + namespace
                + separator + symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AnnotatedVertex that = (AnnotatedVertex) o;

        return new EqualsBuilder()
                .append(artifactRecord, that.artifactRecord)
                .append(namespace, that.namespace)
                .append(symbol, that.symbol)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(artifactRecord)
                .append(namespace)
                .append(symbol)
                .toHashCode();
    }
}
