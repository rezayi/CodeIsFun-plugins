package online.codeisfun.plugins.serializers;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes("online.codeisfun.plugins.serializers.CIFSerializable")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class CIFJavaSerializerAnnotationProcessor extends AbstractProcessor {

    private Class<? extends CIFJavaSerializerInterface> serializerClass = CIFJavaSerializerKryoImplementation.class;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWithAny(annotation);
            Map<String, CIFClass> cifClassMap = new HashMap<>();
            annotatedElements.forEach(annotatedElement -> {
                if (annotatedElement instanceof TypeElement) {
                    processClass((TypeElement) annotatedElement, cifClassMap);
                    try {
                        generateEnhancedClass((TypeElement) annotatedElement);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            cifClassMap.forEach((className, CIFClass) -> {
                ProcessCifClass(CIFClass);
            });
        }
        return true;
    }

    private void generateEnhancedClass(TypeElement classElement) throws IOException {
        String packageName = processingEnv.getElementUtils()
                .getPackageOf(classElement).getQualifiedName().toString();
        String className = classElement.getSimpleName().toString() + "CIFSerializer";

        try {
            JavaFileObject builderFile = processingEnv.getFiler()
                    .createSourceFile(packageName + "." + className);

            try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                if (!packageName.isEmpty()) {
                    out.println("package " + packageName + ";");
                    out.println();
                }
                out.println("public class " + className + "{");
                out.println("    // Original class content preserved");
                out.println("}");
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Failed to process " + className + ": " + e.getMessage()
            );
        }
    }

    private void ProcessCifClass(CIFClass cifClass) {
        try {
            CIFJavaSerializerInterface serializer = serializerClass.getDeclaredConstructor().newInstance();
            serializer.initialize(cifClass);
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Processing proto class failed: " + cifClass.getClassName()
            );
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void processClass(TypeElement typeElement, Map<String, CIFClass> protoClassMap) {
        try {
            CIFClass CIFClass = online.codeisfun.plugins.serializers.CIFClass.fromClass(processingEnv, typeElement, protoClassMap);
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "proto class for class=[" + CIFClass.getClassName() + "] has been generated"
            );
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Generating proto class failed: " + typeElement.getSimpleName()
            );
            throw new RuntimeException(e);
        }
    }
}
