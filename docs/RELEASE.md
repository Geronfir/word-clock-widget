# Release build (signed APK)

The release APK is built and signed entirely in **GitHub Actions** — no local
Android SDK is needed. See `.github/workflows/release.yml`.

## One-time setup (already done for this repo)

A signing keystore is required. It is **never committed**; it lives as GitHub
Actions secrets:

| Secret | What it is |
| --- | --- |
| `SIGNING_KEYSTORE_BASE64` | the `.jks` keystore, base64-encoded (`base64 -w0 file.jks`) |
| `SIGNING_STORE_PASSWORD` | keystore password |
| `SIGNING_KEY_ALIAS` | key alias (this project uses `wordclock`) |
| `SIGNING_KEY_PASSWORD` | key password |

Generate a keystore and upload it with:

```bash
keytool -genkeypair -v \
  -keystore word-clock-release.jks \
  -alias wordclock -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass "$PW" -keypass "$PW" \
  -dname "CN=Word Clock, O=Geronfir, C=ID"

base64 -w0 word-clock-release.jks | gh secret set SIGNING_KEYSTORE_BASE64
gh secret set SIGNING_STORE_PASSWORD --body "$PW"
gh secret set SIGNING_KEY_ALIAS --body wordclock
gh secret set SIGNING_KEY_PASSWORD --body "$PW"
```

> Keep a backup of the keystore and passwords somewhere safe. Losing them means
> you can no longer ship an update that installs over an existing release.

## Cutting a release

Push a version tag, or run the workflow manually:

```bash
# tag-driven (recommended)
git tag v0.1.0 && git push origin v0.1.0

# or from the Actions tab: Release -> Run workflow -> enter a tag
gh workflow run release.yml -f tag=v0.1.0
```

The workflow runs the unit tests, builds and signs the APK, verifies it (launcher
activity + Compose compiler, then `apksigner verify`), and attaches
`word-clock-<tag>.apk` to a GitHub Release.

## How signing is wired

`app/build.gradle.kts` reads four environment variables
(`SIGNING_KEYSTORE_PATH`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`,
`SIGNING_KEY_PASSWORD`). When they are absent — every debug build and every
pull-request CI run — the release variant is simply left unsigned, so nothing
breaks. The workflow decodes the base64 secret to a temp file and exports
`SIGNING_KEYSTORE_PATH` before `./gradlew assembleRelease`.
