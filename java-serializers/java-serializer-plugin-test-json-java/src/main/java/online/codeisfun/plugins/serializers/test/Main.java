package online.codeisfun.plugins.serializers.test;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        var data = Data1.builder()
                .a(123)
                .b("Hello")
                .c(Map.of("a", "a", "b", "b"))
                .d(Map.of("test", OtherClass1.builder().name("test").build()))
                .build();
        var serializer = new Data1CIFSerializer();

        var bytes = serializer.serialize(data);
        System.out.println(bytes.length);
        var deserialized = serializer.deserialize(bytes);
        System.out.println(deserialized);

        var data2 = Test1.builder().a(111).build();
        var serializer2 = new Test1CIFSerializer();
        var bytes2 = serializer2.serialize(data2);
        System.out.println(bytes2.length);
        var deserialized2 = serializer2.deserialize(bytes2);
        System.out.println(deserialized2);


    }
}
