# Releasing Smart Domain

Smart Domain publishes to Maven Central through the Sonatype Central Publisher Portal compatibility
endpoint.

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
./gradlew --no-parallel -PsmartDomainVersion=0.1.0 publishReleaseToCentral
```

After upload, notify the Portal that the repository should be processed:

```bash
AUTH=$(printf '%s:%s' "${CENTRAL_TOKEN_USERNAME}" "${CENTRAL_TOKEN_PASSWORD}" | base64 | tr -d '\n')
curl --fail \
  --request POST \
  --header "Authorization: Bearer ${AUTH}" \
  "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/io.github.jayclock?publishing_type=automatic"
```

## GitHub Release Flow

The standalone repository workflow publishes when a tag like `v0.1.0` is pushed or when the
release workflow is run manually.

It performs:

1. `./gradlew build`
2. `./gradlew --no-parallel publishReleaseToCentral`
3. `POST /manual/upload/defaultRepository/io.github.jayclock?publishing_type=automatic`

## Notes

- Maven Central requires sources jars, javadoc jars, signatures, and complete POM metadata.
- Maven Central does not accept `-SNAPSHOT` versions as releases.
- The Central staging compatibility API is sensitive to stale repositories and concurrent uploads.
  Drop failed repositories before retrying and use `--no-parallel` for release publication.
