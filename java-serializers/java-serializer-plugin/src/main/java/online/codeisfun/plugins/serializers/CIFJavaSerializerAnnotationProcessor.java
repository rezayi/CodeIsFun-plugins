package online.codeisfun.plugins.serializers;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("online.codeisfun.plugins.serializers.CIFSerializable")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class CIFJavaSerializerAnnotationProcessor extends AbstractProcessor {
    private String generatedDir;
    public static Class<? extends CIFJavaSerializerGeneratorInterface> serializerClass = null;
    private Elements elementUtils;
    private Types typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        try {
            FileObject fileObject = processingEnv.getFiler().getResource(StandardLocation.SOURCE_OUTPUT, "", "dummy.txt");
            generatedDir = new File(fileObject.toUri()).getParentFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(CIFSerializable.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }

            // Get class name and output path
            String className = ((TypeElement) element).getQualifiedName().toString();
            String outputDir = "generated-sources/annotations";
//            outputDir = outputDir + "/" + className.replace(".java", "").substring(0, className.lastIndexOf('.')).replace('.', '/');
//            className = className.replace(".java", "").substring(className.lastIndexOf('.') + 1);
            try {
                System.out.println("Generating " + className + ".java in directory " + outputDir);
                ClassModifier.addSerializationMethod(className, outputDir);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        loadImplementations();
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE, "CIF serializer class: " + serializerClass.getName());
        for (TypeElement annotation : annotations) {
            Set<? extends TypeElement> annotatedElements = (Set<TypeElement>) roundEnv.getElementsAnnotatedWith(annotation);
            Map<String, CIFClass> cifClassMap = new HashMap<>();
            annotatedElements.forEach(annotatedElement -> {
                processClass(annotatedElement, cifClassMap);
            });
            List<String> generatedFiles = new ArrayList<>();
            cifClassMap.forEach((className, cifClass) -> {
                var generatedFilePath = processCifClass(cifClass, serializerClass);
                generatedFiles.add(generatedFilePath);
            });
            removeOldGeneratedClasses(generatedFiles);
        }
        return true;
    }


    /**
     * TODO: It works well on rebuild. but when annotation added to new class it builds single class. so it thinks other classes should be deleted
     */
    private void removeOldGeneratedClasses(List<String> generatedFiles) {
        generatedFiles.forEach(fileName -> processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "File:::: " + fileName));

        File generatedDir = new File(this.generatedDir);
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE, "Removing old generated classes from " + generatedDir.getAbsolutePath());
        if (!generatedDir.exists()) {
            return;
        }
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE, "generated dir exists");

        try (var files = Files.walk(Paths.get(generatedDir.toURI()))) {
            files.filter(Files::isRegularFile) // Only files, exclude directories
                    .collect(Collectors.toList())
                    .stream()
                    .filter(path -> {
                        String relativePath = path.toString().substring(path.toString().indexOf("generated-sources/annotations/") + 30);
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "relative path: " + relativePath);
                        return path.toString().endsWith(".java") && !generatedFiles.contains(relativePath);
                    })
                    .forEach(path -> {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "path: " + path);
                        File file = path.toFile();
                        String className = file.getName().replace(".java", "");
                        processingEnv.getMessager().printMessage(
                                Diagnostic.Kind.NOTE, "processing className:" + className);

                        // If the class still implements the interface, keep it
                        if (!doesClassImplementInterface(className)) {
                            processingEnv.getMessager().printMessage(
                                    Diagnostic.Kind.NOTE, "implements interface");

                            // Otherwise, delete it
                            try {
                                Files.deleteIfExists(file.toPath());
                                processingEnv.getMessager().printMessage(
                                        Diagnostic.Kind.NOTE, "Deleted old generated file: " + file.getName());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean doesClassImplementInterface(String className) {
        // Retrieve the TypeElement for the given class name
        TypeElement classElement = elementUtils.getTypeElement(className);
        if (classElement == null) {
            return false; // Class does not exist or is not found in the processing environment
        }

        // Get the interface TypeElement we are checking against
        TypeElement targetInterface = elementUtils.getTypeElement(CIFSerializerInterface.class.getName());
        if (targetInterface == null) {
            return false; // Target interface is not found
        }

        // Check if the class directly implements the target interface
        for (TypeMirror interfaceType : classElement.getInterfaces()) {
            if (typeUtils.isSameType(interfaceType, targetInterface.asType())) {
                return true; // The class implements the target interface
            }
        }

        return false; // The class does not implement the target interface
    }

    public void loadImplementations() {//TODO check only one implementation exists
        ServiceLoader<CIFJavaSerializerGeneratorInterface> serviceLoader = ServiceLoader.load(CIFJavaSerializerGeneratorInterface.class, getClass().getClassLoader());
        for (CIFJavaSerializerGeneratorInterface serializer : serviceLoader) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE, "Found serializer implementation: " + serializer.getClass().getName());
            serializerClass = serializer.getClass();
        }
    }

    private String processCifClass(CIFClass cifClass, Class<? extends CIFJavaSerializerGeneratorInterface> serializerClass) {
        try {
            String packageName = cifClass.getPackageName();
            String originalClassName = cifClass.getClassName();
            String generatedClassName = originalClassName + "CIFSerializer";
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(generatedClassName);
            CIFJavaSerializerGeneratorInterface serializer = serializerClass.getDeclaredConstructor().newInstance();
            serializer.initialize(classBuilder, cifClass, originalClassName);

            // Write the class to a Java file
            var javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
            javaFile.writeTo(processingEnv.getFiler());
            return packageName.replace(".", "/") + "/" + generatedClassName + ".java";
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
                    "Serializer class for class=[" + cifClass.getClassName() + "] has been generated"
            );
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Generating serializer class failed: " + typeElement.getSimpleName()
            );
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
