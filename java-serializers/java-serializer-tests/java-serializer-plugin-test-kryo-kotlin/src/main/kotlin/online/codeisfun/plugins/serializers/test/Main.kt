package online.codeisfun.plugins.serializers.test

import java.util.Map

fun main(args: Array<String>) {
    val data = Data1(
        a = 123,
        b = "Hello",
        c = Map.of("a", "a", "b", "b"),
        d = Map.of("test", OtherClass1(name = "test"))
    )
        val bytes = data.serialize()
        println(bytes.size)
        val deserialized = Data1CIFSerializer().deserialize(bytes)
        println(deserialized)
}
