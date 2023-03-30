import java.util.Random
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SchnorrSignerTest {

    @Test
    fun testSha256() {
        assertTrue { sha256("sheesh".encodeToByteArray()) == sha256(("she" + "esh").encodeToByteArray()) }
        assertFalse { sha256("one".encodeToByteArray()) == sha256("two".encodeToByteArray()) }
    }

    @Test
    fun testValid() {
        val signer = SchnorrSigner(256, 32)
        val data = "Hello World!".encodeToByteArray()
        val (publicKey, privateKey) = signer.generateKeys()
        val signature = signer.sign(data, privateKey)
        assertTrue { signer.verify(data, publicKey, signature) }
    }

    @Test
    fun testInvalid() {
        val signer = SchnorrSigner(256, 32)
        val data = "Goodbye World!".encodeToByteArray()
        val (_, privateKey1) = signer.generateKeys()
        val (publicKey2, _) = signer.generateKeys()
        val signature1 = signer.sign(data, privateKey1)
        assertFalse { signer.verify(data, publicKey2, signature1) }
    }

    // this test ran for ~2.5 minutes for 10 iterations
    @Test
    fun testValidStress() {
        val rnd = Random(7)
        repeat(10) {
            val signer = SchnorrSigner(rnd = rnd)
            val data = ByteArray(100).also { rnd.nextBytes(it) }
            signer.run {
                val (pub, priv) = generateKeys()
                val s = sign(data, priv)
                assertTrue { verify(data, pub, s) }
            }
            println("done #${it + 1}")
        }
    }
}
