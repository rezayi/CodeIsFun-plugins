package online.codeisfun.plugins.serializers;

import com.squareup.javapoet.TypeSpec;

public interface CIFJavaSerializerInterface {
    void initialize(TypeSpec.Builder classBuilder, CIFClass cifClass, String newClassName);
}
