package online.codeisfun.plugins.serializers.test;



import online.codeisfun.plugins.serializers.CIFSerializable;

import java.util.Map;

@CIFSerializable
public class Data1 {
    private Integer a;
    private String b;
    private Map<String, String> c;
    private Map<String, OtherClass1> d;

    public Data1() {
    }
}
