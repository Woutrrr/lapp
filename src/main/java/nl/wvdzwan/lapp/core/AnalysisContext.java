package nl.wvdzwan.lapp.core;

import nl.wvdzwan.lapp.core.ResolvedMethod;
import nl.wvdzwan.lapp.core.UnresolvedMethod;

public interface AnalysisContext {

    ResolvedMethod makeResolved(String namespace, String symbol, String artifact);

    UnresolvedMethod makeUnresolved(String namespace, String symbol);

}
