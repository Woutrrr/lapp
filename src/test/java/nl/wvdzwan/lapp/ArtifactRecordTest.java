package nl.wvdzwan.lapp;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.junit.jupiter.api.Test;

import nl.wvdzwan.lapp.callgraph.ArtifactRecord;

import static org.junit.jupiter.api.Assertions.*;

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
    void nullIdentifierIsInvalid() {
        assertFalse(ArtifactRecord.isValidIdentifier(null));
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

        assertEquals("com.example.company", record.groupId);
    }

    @Test
    void getArtifactId() {
        ArtifactRecord record = new ArtifactRecord("com.example.company", "myapp", "1.0.0");

        assertEquals("myapp", record.artifactId);
    }

    @Test
    void getVersion() {
        ArtifactRecord record = new ArtifactRecord("com.example.company", "myapp", "1.0.0");

        assertEquals("1.0.0", record.getVersion());
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

    @Test
    void artifactToIdentifier() {
        Artifact artifact = new DefaultArtifact("com.example.company", "my-app", "jar", "3.2.1");

        assertEquals("com.example.company:my-app:3.2.1", ArtifactRecord.getIdentifier(artifact));
    }

    @Test
    void artifactNotEqualsNull() {
        ArtifactRecord artifact = new ArtifactRecord("com.example.company", "myapp", "1.2.3");

        assertFalse(artifact.equals(null));
    }

    @Test
    void artifactEqualsSelf() {
        ArtifactRecord artifact = new ArtifactRecord("com.example.company", "myapp", "1.2.3");

        assertEquals(artifact, artifact);
    }

}