package nl.wvdzwan.lapp.filter;

import org.junit.jupiter.api.Test;

import nl.wvdzwan.lapp.Method.Method;
import nl.wvdzwan.lapp.Method.ResolvedMethod;
import nl.wvdzwan.lapp.Method.UnresolvedMethod;

class GraphFilterTest {


    @Test
    void filterSingleLibrary() {

        ResolvedMethod r = new ResolvedMethod("n", "s", "w:z:1.2");
        ResolvedMethod r2 = new ResolvedMethod("r", "t", "w:z:1.2");

        UnresolvedMethod u = new UnresolvedMethod("u", "q");

        method(r, u, r2);
    }

    void method (Method m, Method n, Method r2) {



    }

}