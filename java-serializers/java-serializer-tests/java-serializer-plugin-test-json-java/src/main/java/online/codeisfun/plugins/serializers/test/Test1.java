package online.codeisfun.plugins.serializers.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.codeisfun.plugins.serializers.CIFSerializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@CIFSerializable
public class Test1 {
    private int a;
}
