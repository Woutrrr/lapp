package nl.wvdzwan.timemachine.IRDotMerger;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.io.Attribute;
import org.jgrapht.io.AttributeType;
import org.jgrapht.io.DefaultAttribute;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class AnnotatedVertexTest {

    @Test
    void mergeAttributes() {

        final Attribute nullAttribute = new DefaultAttribute<>(null, AttributeType.STRING);

        Map<String, Attribute> attributeMap = new HashMap<>();
        attributeMap.put("key1", DefaultAttribute.createAttribute("testValue1"));
        attributeMap.put("key2", DefaultAttribute.createAttribute("testValue2"));
        AnnotatedVertex vertex = new AnnotatedVertex("testVertex", attributeMap);

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

        Map<String, Attribute> attributeMap = new HashMap<>();
        attributeMap.put("key1", DefaultAttribute.createAttribute("testValue1"));
        attributeMap.put("key2", DefaultAttribute.createAttribute("testValue2"));
        AnnotatedVertex vertex = new AnnotatedVertex("testVertex", attributeMap);

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
    void newVertexClonesAttributes() {

        Map<String, Attribute> attributeMap = new HashMap<>();
        attributeMap.put("key1", DefaultAttribute.createAttribute("testValue1"));
        attributeMap.put("key2", DefaultAttribute.createAttribute("testValue2"));
        AnnotatedVertex vertex = new AnnotatedVertex("testVertex", attributeMap);


        Map<String, Attribute> vertexAttributes = vertex.getAttributes();


        Assertions.assertNotSame(attributeMap, vertexAttributes);
        Assertions.assertEquals(2, vertexAttributes.size());
        Assertions.assertEquals("testValue1", vertexAttributes.get("key1").getValue());
        Assertions.assertEquals("testValue2", vertexAttributes.get("key2").getValue());

    }

    @Test
    void testEquals() {
        Map<String, Attribute> attributeMap = new HashMap<>();
        attributeMap.put("key1", DefaultAttribute.createAttribute("testValue1"));
        attributeMap.put("key2", DefaultAttribute.createAttribute("testValue2"));
        AnnotatedVertex vertex = new AnnotatedVertex("testVertex", attributeMap);

        Map<String, Attribute> attributeMap2 = new HashMap<>();
        attributeMap2.put("key1", DefaultAttribute.createAttribute("testValue1"));
        attributeMap2.put("key2", DefaultAttribute.createAttribute("testValue2"));
        AnnotatedVertex vertex2 = new AnnotatedVertex("testVertex", attributeMap2);

        Assertions.assertEquals(vertex, vertex);
        Assertions.assertEquals(vertex, vertex2);
    }

    @Test
    void testEqualsOnlyNameMatters() {
        Map<String, Attribute> attributeMap = new HashMap<>();
        attributeMap.put("key1", DefaultAttribute.createAttribute("testValue1"));
        attributeMap.put("key2", DefaultAttribute.createAttribute("testValue2"));
        AnnotatedVertex vertex = new AnnotatedVertex("testVertex", attributeMap);

        Map<String, Attribute> attributeMap2 = new HashMap<>();
        attributeMap2.put("key3", DefaultAttribute.createAttribute("testValue3"));
        attributeMap2.put("key4", DefaultAttribute.createAttribute("testValue4"));
        AnnotatedVertex vertex2 = new AnnotatedVertex("testVertex", attributeMap2);

        Assertions.assertEquals(vertex, vertex2);
    }

    @Test
    void constructorNullNameThrowsNPE() {

        Assertions.assertThrows(NullPointerException.class, () -> {
            new AnnotatedVertex(null, new HashMap<>());
        });
    }

    @Test
    void constructNullAttributes() {

        Assertions.assertDoesNotThrow(() -> {
            new AnnotatedVertex("test", null);
        });
    }

    @Test
    void hashCodeOnlyFromName() {

        String name = "testName";

        Map<String, Attribute> attributeMap2 = new HashMap<>();
        attributeMap2.put("key3", DefaultAttribute.createAttribute("testValue3"));
        attributeMap2.put("key4", DefaultAttribute.createAttribute("testValue4"));
        AnnotatedVertex vertex = new AnnotatedVertex("testName", attributeMap2);


        Assertions.assertEquals(name.hashCode(), vertex.hashCode());
        Assertions.assertEquals(name.hashCode(), vertex.getName().hashCode());

    }
}