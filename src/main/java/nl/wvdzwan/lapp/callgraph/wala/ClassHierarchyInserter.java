package nl.wvdzwan.lapp.callgraph.wala;

import java.util.Iterator;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.ClassReader;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.wvdzwan.lapp.core.ClassRecord;

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

        for (IMethod method : klass.getDeclaredMethods()){
            classRecord.addMethod(method.getSelector().toString());
        }
        if (klass.getName().toString().equals("Ljava/lang/Object")) { return; }

        if (klass instanceof ShrikeClass) {
            ClassReader cr = ((ShrikeClass)klass).getReader();
            try {

                classRecord.setSuperClass(cr.getSuperName().replace('/', '.'));
                for (String directInterface : cr.getInterfaceNames()) {

                    classRecord.addInterface(directInterface.replace('/', '.'));
                }


            } catch (InvalidClassFileException e) {
                logger.error("Invalid class file!", () -> klass.getName().toString());
                e.printStackTrace();
            }
        } else {
            logger.error("Unfamiliar class type found", () -> klass.getClass().toString());
        }


    }
}