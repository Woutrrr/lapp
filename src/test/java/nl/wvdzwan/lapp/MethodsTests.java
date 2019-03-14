package nl.wvdzwan.lapp;

import org.junit.jupiter.api.Test;

import nl.wvdzwan.lapp.core.ResolvedMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MethodsTests {

    @Test
    void sameParametersGiveSameObject() {

        ResolvedMethod m1 = ResolvedMethod.findOrCreate("namespace", "symbol", "group:artifact:version");
        ResolvedMethod m2 = ResolvedMethod.findOrCreate("namespace", "symbol", "group:artifact:version");


        // Should have the same object pointer
        assertTrue(m1 == m2);
        assertEquals((Object) m1, (Object) m2);
    }

    @Test
    void verifyIDFormat() {

        ResolvedMethod m1 = ResolvedMethod.findOrCreate("namespace", "symbol", "group:artifact:version");

        String expected = "group:artifact:version::namespace.symbol";
        assertEquals(expected, m1.toID());
    }
}
