import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.cli.required
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.util.Random

@OptIn(ExperimentalSerializationApi::class, ExperimentalCli::class)
fun main(args: Array<String>) {
    val parser = ArgParser("signer")
    val signer = SchnorrSigner

    class GenKeys : Subcommand("genkeys", "generates a new pair of keys") {
        val bitLength by option(ArgType.Int, "bitLength", "l", description = "approximate length of keys in bits")
            .default(1000)
        val pubFile by option(ArgType.String, "publicKeyFile", "pub", description = "file to output public key")
            .default("public.key")
        val privFile by option(ArgType.String, "privateKeyFile", "priv", description = "file to output private key")
            .default("private.key")
        val randomSeed by option(ArgType.Int, "randomSeed", "r", description = "seed for random generator")

        override fun execute() {
            val seed = randomSeed
            val rnd = if (seed == null) Random() else Random(seed.toLong())
            val (pubKey, privKey) = signer.generateKeys(bitLength, rnd)
            Json.encodeToStream(pubKey, File(pubFile).outputStream())
            Json.encodeToStream(privKey, File(privFile).outputStream())
        }
    }

    class Sign : Subcommand("sign", "creates a signature for an input file") {
        val inputFile by option(ArgType.String, "inputFile", "i").required()
        val privFile by option(ArgType.String, "privateKeyFile", "k").required()
        val signFile by option(ArgType.String, "signatureFile", "s")
            .default("signature")
        val randomSeed by option(ArgType.Int, "randomSeed", "r", description = "seed for random generator")

        override fun execute() {
            val seed = randomSeed
            val rnd = if (seed == null) Random() else Random(seed.toLong())
            val data = File(inputFile).readBytes()
            val privateKey = Json.decodeFromStream<SchnorrSigner.PrivateKey>(File(privFile).inputStream())
            val signature = signer.sign(data, privateKey, rnd)
            Json.encodeToStream(signature, File(signFile).outputStream())
        }
    }

    class Verify : Subcommand("verify", "verifies if a file is correctly signed") {
        val inputFile by option(ArgType.String, "inputFile", "i").required()
        val pubFile by option(ArgType.String, "publicKeyFile", "k").required()
        val signFile by option(ArgType.String, "signatureFile", "s").required()

        override fun execute() {
            val data = File(inputFile).readBytes()
            val publicKey = Json.decodeFromStream<SchnorrSigner.PublicKey>(File(pubFile).inputStream())
            val signature = Json.decodeFromStream<SchnorrSigner.Signature>(File(signFile).inputStream())
            val isCorrect = signer.verify(data, publicKey, signature)
            if (isCorrect) println("OK") else println("FAIL")
        }
    }

    parser.subcommands(GenKeys(), Sign(), Verify())
    parser.parse(args)
}
