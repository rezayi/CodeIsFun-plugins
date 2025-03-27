package online.codeisfun.plugins.serializers.test

import online.codeisfun.plugins.serializers.CIFSerializable

@CIFSerializable
data class Data1(
    val a: Int? = null,
    val b: String? = null,
    val c: Map<String, String>? = null,
    val d: Map<String, OtherClass1>? = null,
)
