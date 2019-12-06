package nl.wvdzwan.lapp;

import org.junit.jupiter.api.Test;

import nl.wvdzwan.lapp.core.ResolvedMethod;
import nl.wvdzwan.lapp.call.Call;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EdgeTest {

    @Test
    void callEdgeEquals() {

        ResolvedMethod m1 = ResolvedMethod.findOrCreate("namespace", "symbol", "group:artifact:version");
        ResolvedMethod m2 = ResolvedMethod.findOrCreate("namespace2", "symbol2", "group2:artifact2:v2");

        Call call = new Call(m1, m2, Call.CallType.INTERFACE, 5, 54);
        Call call2 = new Call(m1, m2, Call.CallType.INTERFACE, 5, 54);


        assertEquals(call, call2);
    }
}
