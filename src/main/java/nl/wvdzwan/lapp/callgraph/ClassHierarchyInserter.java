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
import nl.wvdzwan.lapp.callgraph.IRGraphBuilder.MethodType;
import nl.wvdzwan.lapp.callgraph.outputs.GraphEdge;

public class ClassHierarchyInserter {


    private final IClassHierarchy cha;
    private final IRGraphBuilder graph;

    public ClassHierarchyInserter(IClassHierarchy cha, IRGraphBuilder graph) {
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

    public void processClass(IClass klass) {


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

    public void processMethod(IClass klass, IMethod declaredMethod, List<IMethod> methodInterfaces) {
        if (declaredMethod.isPrivate()) {
            // Private methods cannot be overridden, so no need for them.
            return;
        }
        IClass superKlass = klass.getSuperclass();

        Method declaredMethodNode = graph.addMethod(declaredMethod, getMethodType(klass, declaredMethod));


        IMethod superMethod = superKlass.getMethod(declaredMethod.getSelector());
        if (superMethod != null) {
            Method superMethodNode = graph.addMethod(superMethod.getReference());

            graph.addEdge(superMethodNode, declaredMethodNode, new GraphEdge.ClassHierarchyEdge.OverridesEdge());
        }


        if (methodInterfaces != null) {
            for (IMethod interfaceMethod : methodInterfaces) {
                Method interfaceMethodNode = graph.addMethod(interfaceMethod.getReference(), MethodType.INTERFACE);

                graph.addEdge(interfaceMethodNode, declaredMethodNode, new GraphEdge.ClassHierarchyEdge.ImplementsEdge());
            }
        }


        // An abstract class doesn't have to define abstract functions for interface methods
        // So if this method doesn't have a super method or an interface method look for them in the interfaces of the abstract superclass
        if (superKlass.isAbstract() && superMethod == null && methodInterfaces == null) {

            Map<Selector, IMethod> abstractSuperClassInterfaceMethods = superKlass.getDirectInterfaces()
                    .stream()
                    .flatMap(o -> o.getDeclaredMethods().stream())
                    .collect(Collectors.toMap(IMethod::getSelector, Function.identity()));

            IMethod abstractSuperClassInterfaceMethod = abstractSuperClassInterfaceMethods.get(declaredMethod.getSelector());
            if (abstractSuperClassInterfaceMethod != null) {
                Method abstractSuperClassInterfaceMethodNode = graph.addMethod(abstractSuperClassInterfaceMethod.getReference(), MethodType.INTERFACE);
                graph.addEdge(abstractSuperClassInterfaceMethodNode, declaredMethodNode, new GraphEdge.ClassHierarchyEdge.ImplementsEdge());
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