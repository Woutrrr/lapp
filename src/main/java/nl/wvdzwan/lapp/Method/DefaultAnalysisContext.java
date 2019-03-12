package nl.wvdzwan.lapp.Method;

public class DefaultAnalysisContext implements AnalysisContext {

    private static ResolvedMethodContext resolvedMethod = new DefaultResolvedMethodContext();
    private static UnresolvedMethodContext unresolvedMethod = new DefaultUnresolvedMethodContext();

    @Override
    public ResolvedMethod makeResolved(String namespace, String symbol, String artifact) {
        return resolvedMethod.make(namespace, symbol, artifact);
    }

    @Override
    public UnresolvedMethod makeUnresolved(String namespace, String symbol) {
        return unresolvedMethod.make(namespace, symbol);
    }

}
