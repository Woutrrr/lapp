package nl.wvdzwan.lapp.callgraph.wala;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.AnnotationsReader;
import com.ibm.wala.shrikeCT.ClassReader;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.annotations.Annotation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.wvdzwan.lapp.core.ClassRecord;
import nl.wvdzwan.lapp.core.ExpectedCall;
import nl.wvdzwan.lapp.core.UnresolvedMethod;
import nl.wvdzwan.lapp.core.Util;

public class ClassHierarchyInserter {
    protected static final Logger logger = LogManager.getLogger();


    private final IClassHierarchy cha;
    private final LappPackageBuilder lappBuilder;

    public ClassHierarchyInserter(IClassHierarchy cha, LappPackageBuilder builder) {
        this.cha = cha;
        this.lappBuilder = builder;
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

        ClassRecord classRecord = lappBuilder.makeClassRecord(klass);

        classRecord.isInterface = klass.isInterface();
        classRecord.isPublic = klass.isPublic();
        classRecord.isPrivate = klass.isPrivate();
        classRecord.isAbstract = klass.isAbstract();


        for (IMethod method : klass.getDeclaredMethods()) {
            classRecord.addMethod(method.getSelector().toString());

            if (method.getAnnotations().size() > 0) {
                String namespace = Util.typeReferenceToNamespace(klass.getReference());
                String symbol = method.getSelector().toString();
                UnresolvedMethod source = UnresolvedMethod.findOrCreate(namespace, symbol);

                for (Annotation annotation : method.getAnnotations()) {
                    Map<String, AnnotationsReader.ElementValue> namedArguments = annotation.getNamedArguments();

                    String targetName = namedArguments.get("name").toString();
                    String params = Optional.ofNullable(namedArguments.get("parameterTypes"))
                            .flatMap(elementValue -> {
                                if (elementValue instanceof AnnotationsReader.ArrayElementValue) {
                                    return Optional.ofNullable(Arrays.stream(((AnnotationsReader.ArrayElementValue) elementValue).vals)
                                            .map(Object::toString)
                                            .collect(Collectors.joining(",")));
                                }
                                return Optional.empty();
                            }).orElse("");
                    String returnType = Optional.ofNullable(namedArguments.get("returnType")).map(Object::toString).orElse("V");
                    String targetSymbol = targetName + "(" + params + ")" + returnType;


                    AnnotationsReader.ArrayElementValue targets = (AnnotationsReader.ArrayElementValue) namedArguments.get("resolvedTargets");

                    for (AnnotationsReader.ElementValue targetElement : targets.vals) {
                        String targetNamespace = Util.typeReferenceStringToNamespace(targetElement.toString());
                        targetNamespace = targetNamespace.substring(0, targetNamespace.length()-1);
                        UnresolvedMethod callTarget = UnresolvedMethod.findOrCreate(targetNamespace, targetSymbol);
                        classRecord.addExpectedCall(new ExpectedCall(source, callTarget));
                    }
                }
            }
        }
        if (klass.getName().toString().equals("Ljava/lang/Object")) {
            return;
        }

        if (klass instanceof ShrikeClass) {
            ClassReader cr = ((ShrikeClass) klass).getReader();
            try {

                classRecord.setSuperClass(cr.getSuperName().replace('/', '.'));
                for (String directInterface : cr.getInterfaceNames()) {

                    classRecord.addInterface(directInterface.replace('/', '.'));
                }


            } catch (InvalidClassFileException e) {
                logger.error("Invalid class file!", () -> klass.getName().toString());
                e.printStackTrace();
            }

            if (klass.getAnnotations().size() > 0) {
                for (Annotation annotation : klass.getAnnotations()) {
                    System.out.print(annotation.toString());

                }
            }

        } else {
            logger.error("Unfamiliar class type found", () -> klass.getClass().toString());
        }


    }
}