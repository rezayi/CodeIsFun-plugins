package online.codeisfun.plugins.serializers.test;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import online.codeisfun.plugins.serializers.CIFSerializable;

import java.util.Map;
@Data
@Builder
@AllArgsConstructor
@CIFSerializable
public class Data1 {
    private Integer a;
    private String b;
    private Map<String, String> c;
    private Map<String, OtherClass1> d;

    public Data1() {
    }
}
