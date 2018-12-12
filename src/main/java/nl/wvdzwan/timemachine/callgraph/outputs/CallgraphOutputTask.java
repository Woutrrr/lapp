package nl.wvdzwan.timemachine.callgraph.outputs;


import com.ibm.wala.ipa.cha.IClassHierarchy;

public interface CallgraphOutputTask<T> {

    boolean makeOutput(T result, IClassHierarchy extendedCha);

}
