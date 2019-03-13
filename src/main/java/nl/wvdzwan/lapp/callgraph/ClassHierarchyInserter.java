package nl.wvdzwan.lapp.callgraph;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;

import nl.wvdzwan.lapp.Method.Method;
import nl.wvdzwan.lapp.Method.ResolvedMethod;
import nl.wvdzwan.lapp.call.ChaEdge;
import nl.wvdzwan.lapp.callgraph.PackageBuilder.MethodType;

public class ClassHierarchyInserter {


    private final IClassHierarchy cha;
    private final PackageBuilder graph;

    public ClassHierarchyInserter(IClassHierarchy cha, PackageBuilder graph) {
        this.cha = cha;
        this.graph = graph;
    }

    public void insertCHA() {
        IClassLoader classLoader = cha.getLoader(ClassLoaderReference.Application);

        // Iterate all classes in Application scope
        for (Iterator<IClass> it = classLoader.iterateAllClasses(); it.hasNext(); ) {
            IClass klass = it.next();
            processClass(klass);
        }
    }

    private void processClass(IClass klass) {


        Map<Selector, List<IMethod>> interfaceMethods = klass.getDirectInterfaces()
                .stream()
                .flatMap(o -> o.getDeclaredMethods().stream())
                .collect(
                        Collectors.groupingBy(IMethod::getSelector)
                );

        for (IMethod declaredMethod : klass.getDeclaredMethods()) {

            List<IMethod> methodInterfaces = interfaceMethods.get(declaredMethod.getSelector());

            processMethod(klass, declaredMethod, methodInterfaces);
        }
    }

    private void processMethod(IClass klass, IMethod declaredMethod, List<IMethod> methodInterfaces) {
        if (declaredMethod.isPrivate()) {
            // Private methods cannot be overridden, so no need for them.
            return;
        }
        IClass superKlass = klass.getSuperclass();

        Method declaredMethodNode = graph.addMethod(declaredMethod.getReference(), getMethodType(klass, declaredMethod));

        if (!(declaredMethodNode instanceof ResolvedMethod)) {
            return;
        }
        ResolvedMethod resolvedMethod = (ResolvedMethod) declaredMethodNode;


        IMethod superMethod = superKlass.getMethod(declaredMethod.getSelector());
        if (superMethod != null) {
            Method superMethodNode = graph.addMethod(superMethod.getReference());

            graph.addChaEdge(superMethodNode, resolvedMethod, ChaEdge.ChaEdgeType.OVERRIDE);
        }


        if (methodInterfaces != null) {
            for (IMethod interfaceMethod : methodInterfaces) {
                Method interfaceMethodNode = graph.addMethod(interfaceMethod.getReference(), MethodType.INTERFACE);

                graph.addChaEdge(interfaceMethodNode, resolvedMethod, ChaEdge.ChaEdgeType.IMPLEMENTS);
            }
        }


        // An abstract class doesn't have to define abstract method for interface methods
        // So if this method doesn't have a super method or an interface method look for them in the interfaces of the abstract superclass
        if (superKlass.isAbstract() && superMethod == null && methodInterfaces == null) {

            Map<Selector, IMethod> abstractSuperClassInterfaceMethods = superKlass.getDirectInterfaces()
                    .stream()
                    .flatMap(o -> o.getDeclaredMethods().stream())
                    .collect(Collectors.toMap(IMethod::getSelector, Function.identity()));

            IMethod abstractSuperClassInterfaceMethod = abstractSuperClassInterfaceMethods.get(declaredMethod.getSelector());
            if (abstractSuperClassInterfaceMethod != null) {
                Method abstractSuperClassInterfaceMethodNode = graph.addMethod(abstractSuperClassInterfaceMethod.getReference(), MethodType.INTERFACE);
                graph.addChaEdge(abstractSuperClassInterfaceMethodNode, resolvedMethod, ChaEdge.ChaEdgeType.IMPLEMENTS);
            }
        }
    }

    private MethodType getMethodType(IClass klass, IMethod declaredMethod) {
        if (declaredMethod.isAbstract()) {

            if (klass.isInterface()) {
                return MethodType.INTERFACE;
            } else {
                return MethodType.ABSTRACT;
            }

        } else {
            return MethodType.IMPLEMENTATION;
        }
    }
}