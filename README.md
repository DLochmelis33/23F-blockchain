# 23F-blockchain

This project is built using Gradle.

## Schnorr signature

The program has a simple CLI. Unfortunately, it looks kind of ugly when using Gradle :(

To run the program, use:
```bash
gradlew run --args="--help" 
```

Sample sequence of commands for signing and checking this README file:
```bash
rm public.key private.key signature
gradlew run --args="genkeys"
gradlew run --args="sign -i README.md -k private.key"
gradlew run --args="verify -i README.md -k public.key -s signature"
```

If nothing goes wrong, the last command should print "OK". 
ha
