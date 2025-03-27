package online.codeisfun.plugins.serializers.test

import online.codeisfun.plugins.serializers.CIFSerializable

@CIFSerializable
data class Test1(
    val a: Int = 0
)
