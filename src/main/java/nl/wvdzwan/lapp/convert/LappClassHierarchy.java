package nl.wvdzwan.lapp.convert;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.DefaultDirectedGraph;

import nl.wvdzwan.lapp.call.ChaEdge;
import nl.wvdzwan.lapp.core.ClassRecord;
import nl.wvdzwan.lapp.core.LappPackage;

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

    public void getImplementingClasses(ClassRecord classRecord, String method, Set<ClassRecord> implementors) {

        if (classRecord.methods.contains(method)) {
            implementors.add(classRecord);
        }

        for (ChaEdge edge : this.graph.incomingEdgesOf(classRecord)) {
            getImplementingClasses(edge.src, method, implementors);
        }
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
