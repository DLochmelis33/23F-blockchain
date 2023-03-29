interface Signer<PubK, PrivK, Sig> {
    fun generateKeys(): Pair<PubK, PrivK>
    fun sign(data: ByteArray, privateKey: PrivK): Sig
    fun verify(data: ByteArray, publicKey: PubK, signature: Sig): Boolean
}
