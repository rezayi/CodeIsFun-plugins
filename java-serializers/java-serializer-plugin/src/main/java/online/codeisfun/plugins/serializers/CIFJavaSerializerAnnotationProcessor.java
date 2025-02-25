package online.codeisfun.plugins.serializers;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes("online.codeisfun.plugins.serializers.CIFSerializable")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class CIFJavaSerializerAnnotationProcessor extends AbstractProcessor {


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        SerializerLoader.loadImplementations();
//        var serializerClass = findSerializerInterface(roundEnv);
        for (TypeElement annotation : annotations) {
            Set<? extends TypeElement> annotatedElements = (Set<TypeElement>) roundEnv.getElementsAnnotatedWith(annotation);
            Map<String, CIFClass> cifClassMap = new HashMap<>();
            annotatedElements.forEach(annotatedElement -> {
                processClass(annotatedElement, cifClassMap);
            });
//            cifClassMap.forEach((className, cifClass) -> ProcessCifClass(cifClass, serializerClass));
        }
        return true;
    }

    private void ProcessCifClass(CIFClass cifClass, Class<CIFJavaSerializerInterface> serializerClass) {
        try {
            String packageName = cifClass.getPackageName();
            String originalClassName = cifClass.getClassName();
            String generatedClassName = originalClassName + "CIFSerializer";
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(generatedClassName);

            CIFJavaSerializerInterface serializer = serializerClass.getDeclaredConstructor().newInstance();
            serializer.initialize(classBuilder, cifClass, originalClassName);

            // Write the class to a Java file
            JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
            javaFile.writeTo(processingEnv.getFiler());
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
            CIFClass cifClass = CIFClass.fromClass(processingEnv, typeElement, protoClassMap);
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "Proto class for class=[" + cifClass.getClassName() + "] has been generated"
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
