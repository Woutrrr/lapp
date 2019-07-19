package nl.wvdzwan.lapp.callgraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.types.TypeReference;

public class WrapperGenerator {

    private int counter = 0;
    private Map<TypeReference, String> objectRegister = new HashMap<>();

    private StringBuilder lines = new StringBuilder();


    public void generateWrapper(List<Entrypoint> entryPoints) {
        int counter = 0;
        for (Entrypoint ep : entryPoints) {
            IMethod method = ep.getMethod();

            if (ep.getMethod().isInit()) {

                String objectName = "object" + counter;
                objectRegister.put(ep.getMethod().getDeclaringClass().getReference(), objectName);


                String type = ep.getMethod().getReference().getDeclaringClass().getName().getClassName().toString();

                lines.append(type + " " + objectName + " = new " + type + "(" + printParameters(ep) + ");");
                counter++;
            } else {
                String objectName = objectRegister.get(method.getDeclaringClass().getReference());
                lines.append(objectName + "." + ep.getMethod().getName() + "(" + printParameters(ep) + ");");
            }
            lines.append("\n");
        }
    }

    public String getSource() {


        return "package nl.wvdzwan.tudelft.lapp.wrapper.synthetic; \n" +
                "\n" +
                printImports() +
                "\n" +
                "public class WrapperClass {\n" +
                "\n" +
                "public void wrapper() {" +
                lines.toString() +
                "}\n" +
                "}";

    }

    private String printImports() {
        StringBuilder lines = new StringBuilder();
        for(TypeReference tr : objectRegister.keySet()) {
            lines.append("import " + tr.getName().toString().substring(1).replace('/', '.') + ";\n");
        }

        return lines.toString();
    }

    private String printParameters(Entrypoint ep) {
        StringBuilder result = new StringBuilder();
        for (int i = 1; i < ep.getNumberOfParameters(); i++) {
            TypeReference tref = ep.getParameterTypes(i)[0];

            if (tref.isReferenceType()) {
                result.append("null");
            } else if (tref.isArrayType()) {
                result.append("new ")
                        .append(tref.getName());
            } else if (tref.isPrimitiveType()) {
                switch (tref.toString()) {
                    case "S":
                        result.append("\"\"");
                        break;
                    case "B":
                        result.append(false);
                        break;
                    case "I":
                        result.append("0");
                        break;
                    default:
                        result.append("UNKNOWN " + tref.toString());
                }
            } else {
                result.append("UNKNOWN: " + tref.toString());
            }

            if (i + 1 < ep.getNumberOfParameters()) {
                result.append(", ");
            }
        }
        return result.toString();
    }

}
