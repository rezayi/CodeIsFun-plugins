package online.codeisfun.plugins.serializers;

import javassist.*;

import java.io.File;
import java.io.IOException;

public class ClassModifier {
    public static void addSerializationMethod(String className1, String outputDir) throws NotFoundException, CannotCompileException, IOException {
        try {
            String className = "online.codeisfun.plugins.serializers.test.OtherClass2";

            System.out.println("🔍 Looking for class: " + className);

            ClassPool classPool = new ClassPool(true); // Create a new ClassPool instead of using getDefault()

            // Add target/classes explicitly
//            classPool.appendClassPath(new LoaderClassPath(ClassModifier.class.getClassLoader())); // JVM ClassLoader
            classPool.insertClassPath("../java-serializer-plugin-test-kryo/target/classes"); // Ensure compiled files are included

            // Debug: Print all available classes
            classPool.getImportedPackages().forEachRemaining(System.out::println);

            // Try loading the class
            CtClass ctClass = classPool.get(className);
            System.out.println("✅ Successfully loaded class: " + className);


            // Check if method already exists (to prevent duplication)
            try {
                CtMethod existingMethod = ctClass.getDeclaredMethod("serializeToJson");
                if (existingMethod != null) {
                    System.out.println("Method already exists in " + className);
                    return; // Skip modification
                }
            } catch (NotFoundException ignored) {
                // If not found, we continue adding
            }

            // Define new method (serialization logic)
            String methodBody =
                    "public String serializeToJson() {" +
                            "    try {" +
                            "        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();" +
                            "        return mapper.writeValueAsString(this);" +
                            "    } catch (Exception e) {" +
                            "        throw new RuntimeException(\"Serialization failed\", e);" +
                            "    }" +
                            "}";

            // Create method in the class
            CtMethod newMethod = CtMethod.make(methodBody, ctClass);
            ctClass.addMethod(newMethod);

            // Write back the modified class file
            ctClass.writeFile(outputDir);
            System.out.println("Method added successfully to " + className);
        } catch (NotFoundException e) {
            System.err.println("❌ Class not found: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
