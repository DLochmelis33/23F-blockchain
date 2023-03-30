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
        val signer = SchnorrSigner
        val rnd = Random(123)
        val data = "Hello World!".encodeToByteArray()
        val (publicKey, privateKey) = signer.generateKeys(300, rnd)
        val signature = signer.sign(data, privateKey, rnd)
        assertTrue { signer.verify(data, publicKey, signature) }
    }

    @Test
    fun testInvalid() {
        val signer = SchnorrSigner
        val rnd = Random(321)
        val data = "Goodbye World!".encodeToByteArray()
        val (_, privateKey1) = signer.generateKeys(300, rnd)
        val (publicKey2, _) = signer.generateKeys(300, rnd)
        val signature1 = signer.sign(data, privateKey1, rnd)
        assertFalse { signer.verify(data, publicKey2, signature1) }
    }

    @Test
    // runs for around 3-4 minutes on my machine
    fun testValidStress() {
        val rnd = Random(7)
        repeat(100) {
            val signer = SchnorrSigner
            val data = ByteArray(100).also { rnd.nextBytes(it) }
            signer.run {
                val (pub, priv) = generateKeys(1000, rnd)
                val s = sign(data, priv, rnd)
                assertTrue { verify(data, pub, s) }
            }
            println("done #${it + 1}")
        }
    }
}
