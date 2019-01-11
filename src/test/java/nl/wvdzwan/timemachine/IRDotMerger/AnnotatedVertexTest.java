package nl.wvdzwan.timemachine.IRDotMerger;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.io.Attribute;
import org.jgrapht.io.AttributeType;
import org.jgrapht.io.DefaultAttribute;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.wvdzwan.timemachine.callgraph.ArtifactRecord;

class AnnotatedVertexTest {

    ArtifactRecord record = new ArtifactRecord("com.company", "app", "3.2");

    @Test
    void mergeAttributes() {

        final Attribute nullAttribute = new DefaultAttribute<>(null, AttributeType.STRING);


        AnnotatedVertex vertex = AnnotatedVertex.findOrCreate(record, "class", "fn()");
        Map<String, Attribute> attributeMap = vertex.getAttributes();
        attributeMap.put("key1", DefaultAttribute.createAttribute("testValue1"));
        attributeMap.put("key2", DefaultAttribute.createAttribute("testValue2"));

        Map<String, Attribute> attributeMapOther = new HashMap<>();
        attributeMapOther.put("key2", DefaultAttribute.createAttribute("testValueOther"));
        attributeMapOther.put("key3", DefaultAttribute.createAttribute("testValue3"));


        vertex.mergeAttributes(attributeMapOther);

        Assertions.assertEquals(3, vertex.getAttributes().size());
        Assertions.assertEquals(
                "testValue1",
                vertex.getAttributes().getOrDefault("key1", nullAttribute).getValue()
        );

        Assertions.assertEquals(
                "testValueOther",
                vertex.getAttributes().getOrDefault("key2", nullAttribute).getValue()
        );

        Assertions.assertEquals(
                "testValue3",
                vertex.getAttributes().getOrDefault("key3", nullAttribute).getValue()
        );

    }

    @Test
    void mergeShortAttributes() {

        final Attribute nullAttribute = new DefaultAttribute<>(null, AttributeType.STRING);

        AnnotatedVertex vertex = AnnotatedVertex.findOrCreate(record, "klass", "function()");
        Map<String, Attribute> attributeMap = vertex.getAttributes();
        attributeMap.put("key1", DefaultAttribute.createAttribute("testValue1"));
        attributeMap.put("key2", DefaultAttribute.createAttribute("testValue2"));

        Map<String, Attribute> attributeMapOther = new HashMap<>();
        attributeMapOther.put("key2", DefaultAttribute.createAttribute("short"));
        attributeMapOther.put("key3", DefaultAttribute.createAttribute("testValue3"));


        vertex.mergeAttributes(attributeMapOther);

        Assertions.assertEquals(3, vertex.getAttributes().size());
        Assertions.assertEquals(
                "testValue1",
                vertex.getAttributes().getOrDefault("key1", nullAttribute).getValue()
        );

        Assertions.assertEquals(
                "testValue2",
                vertex.getAttributes().getOrDefault("key2", nullAttribute).getValue()
        );

        Assertions.assertEquals(
                "testValue3",
                vertex.getAttributes().getOrDefault("key3", nullAttribute).getValue()
        );

    }


    @Test
    void testEquals() {
        AnnotatedVertex vertex = AnnotatedVertex.findOrCreate(record, "is.this.the", "same()");
        Map<String, Attribute> attributeMap = new HashMap<>();
        attributeMap.put("key1", DefaultAttribute.createAttribute("testValue1"));
        attributeMap.put("key2", DefaultAttribute.createAttribute("testValue2"));


        AnnotatedVertex vertex2 = AnnotatedVertex.findOrCreate(record, "is.this.the", "same()");

        Assertions.assertEquals(vertex, vertex);
        Assertions.assertEquals(vertex, vertex2);
    }


    @Test
    void constructorNullNameThrowsNPE() {

        Assertions.assertThrows(NullPointerException.class, () -> {
            AnnotatedVertex.findOrCreate(null, "namespace", "symbol");
        });
    }
}