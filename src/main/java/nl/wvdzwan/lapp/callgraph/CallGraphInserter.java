package nl.wvdzwan.lapp.callgraph;

import java.util.Iterator;
import java.util.function.Predicate;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeBT.IInvokeInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;

import nl.wvdzwan.lapp.Method.Method;
import nl.wvdzwan.lapp.call.Call;

public class CallGraphInserter {


    private final CallGraph cg;
    private final IClassHierarchy cha;
    private final PackageBuilder graph;

    public CallGraphInserter(CallGraph cg, IClassHierarchy cha, PackageBuilder graph) {
        this.cg = cg;
        this.cha = cha;
        this.graph = graph;
    }


    public void insertCallGraph() {
        for (CGNode node : this.cg) {
            MethodReference nodeReference = node.getMethod().getReference();

            if (nodeFilter.test(node)) {
                continue;
            }
            Method methodNode = graph.addMethod(nodeReference, PackageBuilder.MethodType.IMPLEMENTATION);

            for (Iterator<CallSiteReference> callSites = node.iterateCallSites(); callSites.hasNext(); ) {
                CallSiteReference callSite = callSites.next();

                MethodReference targetReference = correctClassLoader(callSite.getDeclaredTarget());


                Method targetMethodNode = graph.addMethod(targetReference);
                graph.addCall(methodNode, targetMethodNode, getInvocationLabel(callSite));

            }

        }
    }

    private Predicate<CGNode> nodeFilter = node -> {
        return !node.getMethod()
                .getDeclaringClass()
                .getClassLoader()
                .getReference()
                .equals(ClassLoaderReference.Application);
    };

    private Call.CallType getInvocationLabel(CallSiteReference callsite) {

        switch ((IInvokeInstruction.Dispatch) callsite.getInvocationCode()) {
            case INTERFACE:
                return Call.CallType.INTERFACE;
            case VIRTUAL:
                return Call.CallType.VIRTUAL;
            case SPECIAL:
                return Call.CallType.SPECIAL;
            case STATIC:
                return Call.CallType.STATIC;
        }

        return Call.CallType.UNKNOWN;
    }

    private MethodReference correctClassLoader(MethodReference reference) {
        IClass klass = cha.lookupClass(reference.getDeclaringClass());

        if (klass == null) {
            return MethodReference.findOrCreate(ClassLoaderReference.Extension,
                    reference.getDeclaringClass().getName().toString(),
                    reference.getName().toString(),
                    reference.getDescriptor().toString());
        }

        return MethodReference.findOrCreate(klass.getReference(), reference.getSelector());

    }
}
