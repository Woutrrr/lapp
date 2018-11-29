package nl.wvdzwan.timemachine;

import org.junit.jupiter.api.Test;

import nl.wvdzwan.timemachine.callgraph.ArtifactRecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArtifactRecordTest {


    @Test
    void nullGroupIdConstructor() {

        assertThrows(NullPointerException.class, () -> {
            new ArtifactRecord(null, "myapp", "1.2.3");
        });
    }

    @Test
    void nullArtifactIdConstructor() {

        assertThrows(NullPointerException.class, () -> {
            new ArtifactRecord("com.example.company", null, "1.2.3");
        });
    }

    @Test
    void identifierShouldNotBeNull() {

        assertThrows(NullPointerException.class, () -> {
            new ArtifactRecord(null);
        });
    }

    @Test
    void invalidIdentifiersThrowException() {
        String[] invalidIdentifiers = {
                "",
                "a:",
                "a::2"
        };

        for (String identifier : invalidIdentifiers) {
            assertThrows(IllegalArgumentException.class, () -> {
                new ArtifactRecord(identifier);
            }, identifier + " as identifier should raise exception");
        }
    }


    @Test
    void getGroupId() {
        ArtifactRecord record = new ArtifactRecord("com.example.company", "myapp", "1.0.0");

        assertEquals("com.example.company", record.getGroupId());
    }

    @Test
    void getArtifactId() {
        ArtifactRecord record = new ArtifactRecord("com.example.company", "myapp", "1.0.0");

        assertEquals("myapp", record.getArtifactId());
    }

    @Test
    void getVersion() {
        ArtifactRecord record = new ArtifactRecord("com.example.company", "myapp", "1.0.0");

        assertEquals("1.0.0", record.getVersion());
    }

    @Test
    void getJarName() {
        ArtifactRecord record = new ArtifactRecord("com.example.company", "myapp", "1.2.3");

        assertEquals("myapp-1.2.3.jar", record.getJarName());
    }

    @Test
    void getUnversionedIdentifier() {
        ArtifactRecord record = new ArtifactRecord("com.example.company", "myapp", "1.2.3");

        assertEquals("com.example.company:myapp", record.getUnversionedIdentifier());
    }

    @Test
    void getIdentifier() {
        ArtifactRecord record = new ArtifactRecord("com.example.company", "myapp", "1.2.3");

        assertEquals("com.example.company:myapp:1.2.3", record.getIdentifier());
    }
}