import java.util.Random

interface Signer<PubK, PrivK, Sig> {
    fun generateKeys(bitLength: Int, rnd: Random): Pair<PubK, PrivK>
    fun sign(data: ByteArray, privateKey: PrivK, rnd: Random): Sig
    fun verify(data: ByteArray, publicKey: PubK, signature: Sig): Boolean
}
