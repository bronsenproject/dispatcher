# Releasing to Maven Central

This guide describes how to release the Dispatcher library to Maven Central (central.sonatype.org).

## Prerequisites

1. **Sonatype Account**: Ensure you have an account at [central.sonatype.org](https://central.sonatype.org/).
2. **GPG Key**: You must have a GPG key for signing artifacts. Follow the [official instructions](https://central.sonatype.org/publish/requirements/gpg/#signing-a-file) for GPG setup.

## 1. Maven Configuration

Your `~/.m2/settings.xml` (or the one used in CI/CD) must contain the server credentials with the ID `central` (matching the `publishingServerId` in `pom.xml`).

```xml
<settings>
    <servers>
        <server>
            <id>central</id>
            <username>YOUR_SONATYPE_TOKEN_USERNAME</username>
            <password>YOUR_SONATYPE_TOKEN_PASSWORD</password>
        </server>
    </servers>
</settings>
```

## 2. GPG Key Preparation (for CI/CD)

To use your GPG key in CI/CD (like GitHub Actions), you need to export it as a Transferable Secret Key (TSK).

1. **Find your key fingerprint**:
   ```bash
   gpg --list-secret-keys --keyid-format LONG
   ```

2. **Export your secret key to a file**:
   Replace `<FINGERPRINT>` with your key's fingerprint.
   ```bash
   gpg --export-secret-keys --armor <FINGERPRINT> > mykey.tsk.asc
   ```

3. **Verify/Import (optional)**:
   ```bash
   gpg --import mykey.tsk.asc
   ```

## 3. Deployment

### Local Deployment
If you are deploying from your local machine with a configured GPG agent:
```bash
mvn clean deploy
```

### CI/CD Deployment
In your CI/CD pipeline, provide the GPG key and passphrase as environment variables. The `pom.xml` is configured to sign artifacts during the `verify` phase.

```bash
MAVEN_GPG_KEY=$(cat mykey.tsk.asc) MAVEN_GPG_PASSPHRASE=<your-passphrase> mvn clean deploy -Dgpg.signer=bc
```

> **Note**: The `central-publishing-maven-plugin` is configured with `<autoPublish>true</autoPublish>`, so artifacts will be published automatically once they pass validation in the staging portal.
## 4. GitHub Actions Pipeline

The project includes a GitHub Actions pipeline in `.github/workflows/pipeline.yml` that automates the deployment process.

### Required Secrets

You must configure the following secrets in your GitHub repository:

- `OSSRH_USERNAME`: Your Sonatype token username.
- `OSSRH_PASSWORD`: Your Sonatype token password.
- `GPG_SECRET_KEY`: The exported GPG Transferable Secret Key (TSK) content (from step 2.2).
- `GPG_PASSPHRASE`: The passphrase for your GPG key.

### Branch Flows

- **`dev` branch**: On every push, the pipeline builds the project and performs a signed `mvn deploy` of the snapshot version to Maven Central.
- **`prod` branch**: On every push, the pipeline uses the `maven-release-plugin` (`mvn release:prepare release:perform`) to:
    1.  Automatically bump the version (removing `-SNAPSHOT`).
    2.  Tag the release in Git.
    3.  Deploy the release artifacts to Maven Central.
    4.  Bump to the next development version.

> **Note**: The `maven-release-plugin` requires `GITHUB_TOKEN` permissions to push tags and commits back to the repository. The default `GITHUB_TOKEN` provided by GitHub Actions usually has these permissions if the workflow is triggered by a push to a protected branch, but ensure "Read and write permissions" are enabled in the repository's Action settings.