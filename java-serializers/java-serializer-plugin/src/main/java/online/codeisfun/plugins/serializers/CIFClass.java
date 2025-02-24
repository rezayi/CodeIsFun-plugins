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

    public String toProto() {
        StringBuilder builder = new StringBuilder();

        builder
                .append("syntax = \"proto3\";\n\n")
                .append("package ").append(packageName).append(";\n")
                .append("option java_multiple_files = true;\n");

        imports.forEach(CIFClass ->
                builder.append("import \"").append(CIFClass.fileName).append("\";\n")
        );
        builder.append("\n");
        builder.append("message ").append(className+"Proto").append(" {\n");
        for (int i = 0; i < fields.size(); i++) {
            var field = fields.get(i);
            builder.append("\t").append(field.getType()).append(" ").append(field.getName()).append(" = ").append(i + 1).append(";\n");
        }
        builder.append("}");
        return builder.toString();
    }


    public static CIFClass fromClass(ProcessingEnvironment processingEnv, TypeElement typeElement, Map<String, CIFClass> protoClassMap) {
        CIFClass CIFClass = new CIFClass();
        CIFClass.className = typeElement.getSimpleName().toString();
        String packageName = typeElement.getQualifiedName().toString();
        packageName = packageName.substring(0, packageName.lastIndexOf("."));
        CIFClass.packageName = packageName;
        CIFClass.fileName = CIFClass.getClassName() + "_proto.proto";
        typeElement.getEnclosedElements()
                .stream()
                .filter(element -> element.getKind() == ElementKind.FIELD)
                .forEach(field -> CIFClass.addField(processingEnv, (VariableElement) field, protoClassMap, CIFClass.imports));
        protoClassMap.put(typeElement.getQualifiedName().toString(), CIFClass);
        return CIFClass;
    }
}
