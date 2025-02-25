package online.codeisfun.plugins.serializers;

import java.util.ServiceLoader;

public class SerializerLoader {

    public static void loadImplementations() {
        ServiceLoader<CIFJavaSerializerInterface> serviceLoader = ServiceLoader.load(CIFJavaSerializerInterface.class);
        
        for (CIFJavaSerializerInterface serializer : serviceLoader) {
            System.out.println("Found serializer implementation: " + serializer.getClass().getName());
        }
    }
}