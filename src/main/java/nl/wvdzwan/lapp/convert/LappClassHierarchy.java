package nl.wvdzwan.lapp.convert;

import nl.wvdzwan.lapp.call.ChaEdge;
import nl.wvdzwan.lapp.core.ClassRecord;
import nl.wvdzwan.lapp.core.LappPackage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LappClassHierarchy {
    protected static final Logger logger = LogManager.getLogger();

    private DefaultDirectedGraph<ClassRecord, ChaEdge> graph = new DefaultDirectedGraph<>(ChaEdge.class);
    private Map<String, ClassRecord> typeRepository;
    private Map<String, ClassRecord> missingTypeRepository;

    public DefaultDirectedGraph<ClassRecord, ChaEdge> getGraph() {
        return this.graph;
    }

    public Set<ClassRecord> getImplementingClasses(String typeName, String method) {
        ClassRecord root = typeRepository.get(typeName);
        if (root == null) {
            logger.warn("Type not found in class hierarchy {}", () -> typeName);
            return Collections.emptySet();
        }
        Set<ClassRecord> implementors = new HashSet<>();

        getImplementingClasses(root, method, implementors);

        return implementors;
    }

    public boolean getImplementingClasses(ClassRecord classRecord, String method, Set<ClassRecord> implementors) {
        boolean hasDirectImplementation = false;

        if (classRecord.methods.contains(method)) {
            implementors.add(classRecord);
            hasDirectImplementation = true;
        }

        // Check classes that implement/extend this interface/class
        for (ChaEdge edge : this.graph.incomingEdgesOf(classRecord)) {
            boolean childHasDirectImplementation = getImplementingClasses(edge.src, method, implementors);

            // If the classRecord is an interface then the implementing classes' superclasses also need to be checked for the implementation if the implementing class doesn't have an implementation
            if (classRecord.isInterface && !childHasDirectImplementation) {
                getSuperClassesProvidingImplementation(edge.src, method, implementors);
            }
        }

        return hasDirectImplementation;
    }

    public void getSuperClassesProvidingImplementation(ClassRecord subClass, String method, Set<ClassRecord> implementors) {
        ClassRecord superClass = typeRepository.get(subClass.superClass);

        if (superClass == null) {
            if (!subClass.superClass.equals("")) { // Filter out superclass of Object
                logger.warn("SuperClass Type not found in class hierarchy {}", () -> subClass.superClass);
            }
            return;
        }

        if (superClass.methods.contains(method)) {
            implementors.add(superClass);
        }

        getSuperClassesProvidingImplementation(superClass, method, implementors);
    }

    public static LappClassHierarchy make(LappPackage lapp) {

        LappClassHierarchy result = new LappClassHierarchy();

        result.typeRepository = lapp.classRecords.stream()
                .collect(Collectors.toMap(LappClassHierarchy::classRecordToTypeKey, Function.identity()));
        result.missingTypeRepository = new HashMap<>();

        for (ClassRecord record : lapp.classRecords) {
            result.graph.addVertex(record);

            // Super class
            ClassRecord superRecord = result.typeRepository.get(record.superClass);
            if (superRecord == null) {
                superRecord = new ClassRecord("PHANTOM", record.superClass);
                result.missingTypeRepository.put(classRecordToTypeKey(superRecord), superRecord);
            }
            result.graph.addVertex(superRecord);
            result.graph.addEdge(record, superRecord, new ChaEdge(ChaEdge.ChaEdgeType.EXTENDS, record, superRecord));

            // Interfaces
            for (String interfaceName : record.interfaces) {
                ClassRecord interfaceRecord = result.typeRepository.get(interfaceName);
                if (interfaceRecord == null) {
                    interfaceRecord = new ClassRecord("PHANTOM", interfaceName);
                    result.missingTypeRepository.put(classRecordToTypeKey(interfaceRecord), interfaceRecord);
                }
                result.graph.addVertex(interfaceRecord);
                result.graph.addEdge(record, interfaceRecord, new ChaEdge(ChaEdge.ChaEdgeType.IMPLEMENTS, record, interfaceRecord));
            }
        }

        return result;
    }

    private static String classRecordToTypeKey(ClassRecord c) {
        return c.name;
    }
}
