package online.codeisfun.plugins.serializers.test;

import lombok.Builder;
import lombok.Data;
import online.codeisfun.plugins.serializers.CIFSerializable;

@Data
@Builder
@CIFSerializable
public class OtherClass1 {
    private String name;
}
