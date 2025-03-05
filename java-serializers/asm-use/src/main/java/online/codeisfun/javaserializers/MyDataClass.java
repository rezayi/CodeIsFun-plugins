package online.codeisfun.javaserializers;


/**
 * A simple data class annotated with @AddExtraMethod.
 * When loaded with the Java agent, an extra method (extraMethod) will be injected.
 */
@AddExtraMethod
public class MyDataClass {
    private String name;
    private int age;

    public MyDataClass(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // Standard getters
    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}