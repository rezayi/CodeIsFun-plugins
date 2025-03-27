package online.codeisfun.plugins.serializers.test

import online.codeisfun.plugins.serializers.CIFSerializable

@CIFSerializable
data class OtherClass1(
    val name: String? = null
)
