---
paths:
  - "**/.claude/rules/android/r8-minified.md"
  - "**/androidApp/proguard-rules.pro"
  - "**/androidApp/build.gradle.kts"
  - "**/AndroidManifest.xml"
  - "**/*Worker.kt"
  - "**/*Dao.kt"
  - "**/*Entity.kt"
  - "**/*Database.kt"
  - "**/*Response.kt"
  - "**/Nav*.kt"
---

# R8 and the minified build

- `release` is shrunk and obfuscated: `isMinifyEnabled` and `isShrinkResources` are both on for
  it, over `proguard-android-optimize.txt` plus `androidApp/proguard-rules.pro`.
- It carries no signing config, so what actually goes on a device is `minified`: `initWith`
  release plus the debug key, and identical to it in everything R8 does. Build it with
  `./gradlew :androidApp:assembleMinified`; the two variants' `mapping.txt` files match byte for
  byte.
- `isDebuggable` must stay off on both. AGP runs R8 in debug mode for a debuggable variant, which
  silently skips obfuscation, the part of R8 most likely to break something.
- Run this pass whenever the change touches anything reached by name: reflection,
  `Class.forName`, kotlinx.serialization, Room entities and DAOs, WorkManager workers, or a class
  the manifest names. A change that touches none of those does not need it.
- Look for a library's own rules before writing any. An AAR carries `proguard.txt` or
  `consumer-rules.pro` inside it, and AGP merges those automatically. Everything actually applied,
  and where it came from, is listed in
  `androidApp/build/outputs/mapping/minified/configuration.txt`. Only add a rule to
  `androidApp/proguard-rules.pro` once that file shows nobody supplied it.
- Read the other artifacts next to it. `missing_rules.txt` appears only when something needed a
  keep rule. `seeds.txt` lists what was kept and why. `usage.txt` lists what was stripped.
  `mapping.txt` shows what was renamed; check there that the names that must survive did.
- A successful build proves nothing on its own. Install the minified APK on an emulator, clean,
  and walk the flows the change touches. Confirm they really ran, that the log holds no
  `ClassNotFoundException` or `NoSuchMethodException`, and that no screen fell back to an empty or
  error state the unminified build doesn't show.
- A local minified build matches what CI builds only with the NDK named next to `agp` in the
  version catalog, and with every file checked out with the LF endings `.gitattributes` sets.
  Without that NDK, AGP leaves the native libraries unstripped, and the build log says "Unable to
  strip". A file checked out with CRLF lands in the APK as it is.
- `androidApp/proguard-rules.pro` keeps `SourceFile` and `LineNumberTable` and renames the source
  file to a constant. An obfuscated stack trace stays decodable through `mapping.txt` with retrace
  while leaking nothing.
