package online.codeisfun.plugins.serializers;

public interface CIFSerializerInterface <T>{

    byte[] serialize(T object);

    T deserialize(byte[] bytes);
}
