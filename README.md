# 23F-blockchain

This project written in Kotlin and built using Gradle.

## Schnorr signature

I have written RSA enough times, so I thought it was time for something new. This implementation uses [Schnorr groups](https://en.wikipedia.org/wiki/Schnorr_group) instead of elliptic curves, because ChatGPT told me they are simpler. (Note: I wrote the code myself though 👀)

The program has a simple CLI. Unfortunately, it looks kind of ugly when using Gradle :(

To run the program, use:
```bash
./gradlew run --args="--help"
```

Sample sequence of commands for signing and checking this README file:
```bash
rm public.key private.key signature # clean files generated after running this sequence
./gradlew run --args="genkeys"
./gradlew run --args="sign -i README.md -k private.key"
./gradlew run --args="verify -i README.md -k public.key -s signature"
```

Short explanation:
* The `genkeys` command will generate two files: one with a public key and one with the corresponding private key.
* The `sign` command will generate another file, which contains the signature of a given file.
* If nothing goes wrong, the last `verify` command should print "OK".
* Each command has a help message that can be called as `...args="genkeys --help"`, which will show all available options.

Currently, keys are being serialized as json files. This is far from optimal (especially serializing byte arrays as json arrays of plaintext numbers), but efficient serialization is outside the scope of this task.
