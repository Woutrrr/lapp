package nl.wvdzwan.lapp.Method;

public interface AnalysisContext {

    ResolvedMethod makeResolved(String namespace, String symbol, String artifact);

    UnresolvedMethod makeUnresolved(String namespace, String symbol);

}
