package online.codeisfun.plugins.serializers;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CIFClass {
    private String className;
    private String packageName;
    private String fileName;
    private final List<CIFField> fields = new ArrayList<>();
    private final List<CIFClass> imports = new ArrayList<>();

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public CharSequence getFileName() {
        return fileName;
    }

    public List<CIFField> getFields() {
        return fields;
    }

    private void addField(ProcessingEnvironment processingEnv, VariableElement field, Map<String, CIFClass> protoClassMap, List<CIFClass> imports) {
        this.fields.add(CIFField.fromField(processingEnv, field, protoClassMap, imports));
    }

    public static CIFClass fromClass(ProcessingEnvironment processingEnv, TypeElement typeElement, Map<String, CIFClass> protoClassMap) {
        CIFClass cifClass = new CIFClass();
        cifClass.className = typeElement.getSimpleName().toString();
        String packageName = typeElement.getQualifiedName().toString();
        packageName = packageName.substring(0, packageName.lastIndexOf("."));
        cifClass.packageName = packageName;
        cifClass.fileName = cifClass.getClassName() + "_proto.proto";
        System.out.println(cifClass.className);
        typeElement.getEnclosedElements()
                .stream()
                .filter(element -> element.getKind() == ElementKind.FIELD)
                .forEach(field -> cifClass.addField(processingEnv, (VariableElement) field, protoClassMap, cifClass.imports));
        protoClassMap.put(typeElement.getQualifiedName().toString(), cifClass);
        return cifClass;
    }
}
