---
paths:
  - "**/gradle/libs.versions.toml"
  - "**/iosApp/Configuration/**"
---

# iOS versions

- Every iOS version lives in `gradle/libs.versions.toml`, under the iOS headers.
- `iosApp/Configuration/Version.xcconfig` is generated from the catalog by
  `./gradlew generateIosVersionXcconfig`. Every build of `androidApp` runs that task too.
- Never edit the file by hand. Commit a version change together with the regenerated file.
- A change to a `swiftLint*` entry of the catalog makes a SwiftLint run due; see
  [swiftlint.md](swiftlint.md).
