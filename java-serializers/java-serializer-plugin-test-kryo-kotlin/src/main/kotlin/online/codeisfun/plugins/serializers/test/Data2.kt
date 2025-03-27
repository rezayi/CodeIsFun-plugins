package online.codeisfun.plugins.serializers.test

import online.codeisfun.plugins.serializers.CIFSerializable

@CIFSerializable
data class Data2(
    val a2: Int? = null,
    val b2: String? = null,
    val c2: List<Int>? = null,
    val d2: Data1? = null,
    val e2: List<Data1>? = null,
    val f2: Map<String, Data1>? = null,
    val f3: Map<String, OtherClass2>? = null,
)
