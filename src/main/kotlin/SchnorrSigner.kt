@file:UseSerializers(BigIntegerSerializer::class)

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.math.BigInteger
import java.util.*

object SchnorrSigner : Signer<SchnorrSigner.PublicKey, SchnorrSigner.PrivateKey, SchnorrSigner.Signature> {

    private val hash: (ByteArray) -> BigInteger = ::sha256

    // subgroup of Z_p of order q with generator g
    @Serializable
    data class SchnorrGroupDescription internal constructor(
        internal val p: BigInteger,
        internal val q: BigInteger,
        internal val g: BigInteger
    ) {
        fun randomElement(rnd: Random) = g.modPow(BigInteger(q.bitLength(), rnd), p)

        fun pow(base: BigInteger, exp: BigInteger) = base.modPow(exp, p)

        companion object {
            fun generate(approxBitLengthP: Int, bitLengthQ: Int, rnd: Random): SchnorrGroupDescription {
                val q = BigInteger.probablePrime(bitLengthQ, rnd)
                outer@ while (true) {
                    val k = BigInteger(approxBitLengthP * 2 - bitLengthQ, rnd)
                    val maybeP = q * k + BigInteger.ONE
                    if (maybeP.isProbablePrime(100)) {
                        val p = maybeP
                        while (true) {
                            val h = BigInteger(p.bitLength(), rnd)
                            val maybeG = h.modPow(k, p)
                            if (maybeG != BigInteger.ONE) {
                                val g = maybeG
                                return SchnorrGroupDescription(p, q, g)
                            }
                        }
                    }
                }
            }
        }
    }

    @Serializable
    data class PrivateKey(internal val x: BigInteger, internal val groupDesc: SchnorrGroupDescription)

    @Serializable
    data class PublicKey(internal val y: BigInteger, internal val groupDesc: SchnorrGroupDescription)

    @Serializable
    data class Signature(
        internal val s: BigInteger,
        internal val e: BigInteger,
        internal val groupDesc: SchnorrGroupDescription
    )

    override fun generateKeys(bitLength: Int, rnd: Random): Pair<PublicKey, PrivateKey> {
        val group = SchnorrGroupDescription.generate(bitLength, bitLength / 10, rnd)
        val x = group.randomElement(rnd)
        val y = group.pow(group.g, x)
        return PublicKey(y, group) to PrivateKey(x, group)
    }

    override fun sign(data: ByteArray, privateKey: PrivateKey, rnd: Random): Signature {
        val (x, group) = privateKey
        val (_, q, g) = group
        val k = group.randomElement(rnd)
        val r = group.pow(g, k)
        val e = hash(r.toByteArray() + data).mod(q) // DIFFERENT GROUP
        val s = (k - x * e).mod(q) // DIFFERENT GROUP
        return Signature(s, e, group)
    }

    override fun verify(data: ByteArray, publicKey: PublicKey, signature: Signature): Boolean {
        val (y, group) = publicKey
        val (p, q, g) = group
        val (s, e) = signature
        val rv = (group.pow(g, s) * group.pow(y, e)).mod(p)
        val ev = hash(rv.toByteArray() + data).mod(q) // DIFFERENT GROUP
        return ev == e
    }
}
