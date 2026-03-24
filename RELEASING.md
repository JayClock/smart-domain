# Releasing Smart Domain

Smart Domain publishes to Maven Central through the Sonatype Central Publisher Portal API by
uploading a deployment bundle.

## One-Time Setup

1. Create the standalone repository: `https://github.com/JayClock/smart-domain`
2. Verify the namespace `io.github.jayclock` in the Sonatype Central Publisher Portal.
3. Generate a Central Portal user token.
4. Generate an armored GPG private key for artifact signing.
5. Add these GitHub repository secrets:
   - `CENTRAL_TOKEN_USERNAME`
   - `CENTRAL_TOKEN_PASSWORD`
   - `GPG_PRIVATE_KEY`
   - `GPG_PASSPHRASE`

## Local Dry Run

```bash
cd smart-domain
./gradlew build
./gradlew publishToMavenLocal
./gradlew -p samples/consumer test
./gradlew -p samples/api-consumer test
```

## Local Release Upload

Use a non-snapshot version and export the publishing credentials:

```bash
export CENTRAL_TOKEN_USERNAME=...
export CENTRAL_TOKEN_PASSWORD=...
export GPG_PRIVATE_KEY="$(cat private.asc)"
export GPG_PASSPHRASE=...

cd smart-domain
./gradlew --no-parallel -PsmartDomainVersion=0.1.0 publishReleaseBundle
```

## GitHub Release Flow

The standalone repository workflow publishes when a tag like `v0.1.0` is pushed or when the
release workflow is run manually.

It performs:

1. `./gradlew build`
2. `./gradlew --no-parallel publishReleaseBundle`
3. Generates checksums and zips the published repository layout
4. Uploads the bundle to `https://central.sonatype.com/api/v1/publisher/upload`
5. Polls deployment status until `PUBLISHED`

## Notes

- Maven Central requires sources jars, javadoc jars, signatures, and complete POM metadata.
- Maven Central does not accept `-SNAPSHOT` versions as releases.
- The uploaded bundle uses Maven repository layout and contains signatures plus checksum files.
- Gradle module metadata files are excluded from the bundle because the Portal upload flow only
  needs the canonical Maven release artifacts.
