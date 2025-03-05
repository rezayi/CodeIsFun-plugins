package online.codeisfun.javaserializers;

import java.lang.reflect.Method;

/**
 * Main application to demonstrate that the extra method was injected.
 */
public class Main {
    public static void main(String[] args) {
        try {
            // Create an instance of the annotated class
            MyDataClass instance = new MyDataClass("Alice", 30);
            // Use reflection to find and invoke the injected method "extraMethod"
            Method injectedMethod = instance.getClass().getMethod("extraMethod");
            System.out.println("Invoking injected method:");
            injectedMethod.invoke(instance);
        } catch (NoSuchMethodException e) {
            System.err.println("Injected method not found! Did you run the app with the Java agent?");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}