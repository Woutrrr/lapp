package nl.wvdzwan.lapp.callgraph.wala;

import java.util.Iterator;
import java.util.Set;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeBT.IInvokeInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;

import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.core.Method;

public class CallGraphInserter {


    private final CallGraph cg;
    private final IClassHierarchy cha;
    private final LappPackageBuilder lappPackageBuilder;
    private final IClassLoader classLoader;

    public CallGraphInserter(CallGraph cg, IClassHierarchy cha, LappPackageBuilder lappPackageBuilder, IClassLoader classLoader) {
        this.cg = cg;
        this.cha = cha;
        this.lappPackageBuilder = lappPackageBuilder;
        this.classLoader = classLoader;
    }


    public void insertCallGraph() {
        for (CGNode node : this.cg) {
            MethodReference nodeReference = node.getMethod().getReference();

            if (classLoaderFilter(node) || nodeReference.getDeclaringClass().getName().toString().equals("Lcom/ibm/wala/FakeRootClass")) {
                // Ignore everything not in the provided classloader
                continue;
            }
            Method methodNode = lappPackageBuilder.addMethod(nodeReference, LappPackageBuilder.MethodType.IMPLEMENTATION);

            for (Iterator<CallSiteReference> callSites = node.iterateCallSites(); callSites.hasNext(); ) {

                CallSiteReference callSite = callSites.next();

                int programCounter = callSite.getProgramCounter();
                int lineNumber = node.getMethod().getLineNumber(programCounter);

                if (lappPackageBuilder.getPackageCount() > 1) {
                    // More than 1 package, thus should be able to use call site context
                    Set<CGNode> possibleTargets = cg.getPossibleTargets(node, callSite);
                    for (CGNode possibleTarget : possibleTargets) {
                        MethodReference targetWithCorrectClassLoader = correctClassLoader(possibleTarget.getMethod().getReference());

                        Method targetMethodNode = lappPackageBuilder.addMethod(targetWithCorrectClassLoader);
                        lappPackageBuilder.addCall(methodNode, targetMethodNode, getInvocationLabel(callSite), lineNumber, programCounter);
                    }
                } else {

                /* If the target is unknown, is gets the Application loader by default. We would like this to be the
                   Extension loader, that way it is easy to filter them out later.
                   */
                    MethodReference targetWithCorrectClassLoader = correctClassLoader(callSite.getDeclaredTarget());

                    Method targetMethodNode = lappPackageBuilder.addMethod(targetWithCorrectClassLoader);
                    lappPackageBuilder.addCall(methodNode, targetMethodNode, getInvocationLabel(callSite), lineNumber, programCounter);
                }
            }

        }
    }

    private boolean classLoaderFilter(CGNode node)  {
        return !node.getMethod()
                .getDeclaringClass()
                .getClassLoader()
                .getReference()
                .equals(this.classLoader.getReference()) ;
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
