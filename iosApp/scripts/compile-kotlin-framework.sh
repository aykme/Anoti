#!/bin/sh
# Builds the Kotlin framework for the Xcode build that runs this script. Xcode starts it without
# the user's shell environment, so it finds a Java itself first. The iOS build needs no Android
# SDK; one in its default folder is still handed over.
set -e

if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
  exit 0
fi

# `java` on PATH is never used: on a Mac with no JDK it is a stub that asks to install one.
java_works() {
  [ -n "$1" ] && "$1/bin/java" -version > /dev/null 2>&1
}

java_home=""
for candidate in \
  "$JAVA_HOME" \
  "$(/usr/libexec/java_home 2> /dev/null || true)" \
  "/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
  "$HOME/Applications/Android Studio.app/Contents/jbr/Contents/Home"
do
  if java_works "$candidate"; then
    java_home="$candidate"
    break
  fi
done

if [ -z "$java_home" ]; then
  echo "error: Java not found. Install a JDK, or Android Studio into /Applications." >&2
  exit 1
fi
export JAVA_HOME="$java_home"
echo "compile-kotlin-framework: Java from $JAVA_HOME"

project_dir="$SRCROOT/.."
default_sdk="$HOME/Library/Android/sdk"
if ! grep -q '^sdk.dir=' "$project_dir/local.properties" 2> /dev/null \
  && [ -z "$ANDROID_HOME" ] && [ -d "$default_sdk" ]; then
  export ANDROID_HOME="$default_sdk"
fi
if [ -n "$ANDROID_HOME" ]; then
  echo "compile-kotlin-framework: Android SDK from $ANDROID_HOME"
elif grep -q '^sdk.dir=' "$project_dir/local.properties" 2> /dev/null; then
  echo "compile-kotlin-framework: Android SDK from local.properties"
else
  echo "compile-kotlin-framework: no Android SDK found"
fi

cd "$project_dir"
./gradlew :core-kmp:di-app:embedAndSignAppleFrameworkForXcode
