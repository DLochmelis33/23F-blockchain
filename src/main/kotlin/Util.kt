import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigInteger
import java.security.MessageDigest

fun sha256(data: ByteArray): BigInteger = MessageDigest.getInstance("SHA-256").digest(data).let { BigInteger(it) }

object BigIntegerSerializer : KSerializer<BigInteger> {
    private val delegateSerializer = ByteArraySerializer()

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor = SerialDescriptor("BigInteger", delegateSerializer.descriptor)
    override fun serialize(encoder: Encoder, value: BigInteger) =
        encoder.encodeSerializableValue(delegateSerializer, value.toByteArray())

    override fun deserialize(decoder: Decoder): BigInteger =
        BigInteger(decoder.decodeSerializableValue(delegateSerializer))
}
