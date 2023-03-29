import java.math.BigInteger
import java.util.*

class SchnorrSigner(
    approxBitLengthP: Int = 2000,
    bitLengthQ: Int = 200,
    private val rnd: Random,
    private val hash: (ByteArray) -> BigInteger
) : Signer<SchnorrSigner.PublicKey, SchnorrSigner.PrivateKey, SchnorrSigner.Signature> {

    data class PrivateKey(internal val x: BigInteger)
    data class PublicKey(internal val y: BigInteger)
    data class Signature(internal val s: BigInteger, internal val e: BigInteger)

    // subgroup of Z_p of order q with generator g
    private val p: BigInteger
    private val q: BigInteger
    private val g: BigInteger

    init {
        q = BigInteger.probablePrime(bitLengthQ, rnd)
        outer@ while (true) {
            val k = BigInteger(approxBitLengthP * 2 - bitLengthQ, rnd)
            val maybeP = q * k + BigInteger.ONE
            if (maybeP.isProbablePrime(100)) {
                p = maybeP
                while (true) {
                    val h = BigInteger(p.bitLength(), rnd)
                    val maybeG = h.modPow(k, p)
                    if (maybeG != BigInteger.ONE) {
                        g = maybeG
                        break@outer
                    }
                }
            }
        }
    }

    private fun randomGroupElement() = g.modPow(BigInteger(q.bitLength(), rnd), p)

    private fun BigInteger.groupPow(t: BigInteger) = this.modPow(t, p)

    override fun generateKeys(): Pair<PublicKey, PrivateKey> {
        val x = randomGroupElement()
        val y = g.groupPow(x)
        return PublicKey(y) to PrivateKey(x)
    }

    override fun sign(data: ByteArray, privateKey: PrivateKey): Signature {
        val (x) = privateKey
        val k = randomGroupElement()
        val r = g.groupPow(k)
        val e = hash(r.toByteArray() + data).mod(p) // can be 0
        val s = k - x * e // can be 0
        return Signature(s, e)
    }

    override fun verify(data: ByteArray, publicKey: PublicKey, signature: Signature): Boolean {
        val (y) = publicKey
        val (s, e) = signature
        val rv = g.groupPow(s) * y.groupPow(e)
        val ev = hash(rv.toByteArray() + data) // can be 0
        return ev == e
    }
}
