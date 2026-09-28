#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
: "${ANDROID_JAR:?Set ANDROID_JAR to Android API 35 android.jar}"
: "${R8_JAR:?Set R8_JAR to the official R8 jar}"
: "${AAPT:?Set AAPT to a compatible aapt executable}"
: "${ZIPALIGN:?Set ZIPALIGN to a compatible zipalign executable}"
: "${SHIZUKU_DEPS_DIR:?Set SHIZUKU_DEPS_DIR to the SDK jar directory}"
OUT="$ROOT/out"
mkdir -p "$OUT"
rm -rf "$OUT/classes" "$OUT/dex"
mkdir -p "$OUT/classes" "$OUT/dex"
for mod in api provider aidl shared; do
    test -f "$SHIZUKU_DEPS_DIR/$mod.jar" || { echo "Missing $mod.jar" >&2; exit 1; }
done
javac -source 8 -target 8 -Xlint:-options \
    -cp "$ANDROID_JAR:$SHIZUKU_DEPS_DIR/*" \
    -d "$OUT/classes" $(find "$ROOT/app/src/main/java" -name '*.java')
jar cf "$OUT/classes.jar" -C "$OUT/classes" .
java -cp "$R8_JAR" com.android.tools.r8.D8 --lib "$ANDROID_JAR" --min-api 33 \
    --output "$OUT/dex" "$OUT/classes.jar" \
    "$SHIZUKU_DEPS_DIR/api.jar" "$SHIZUKU_DEPS_DIR/provider.jar" \
    "$SHIZUKU_DEPS_DIR/aidl.jar" "$SHIZUKU_DEPS_DIR/shared.jar"
sed 's/<manifest /<manifest package="com.openminis.mirrorwhitelist" /' \
    "$ROOT/app/src/main/AndroidManifest.xml" > "$OUT/AndroidManifest.xml"
"$AAPT" package -f -M "$OUT/AndroidManifest.xml" \
    -S "$ROOT/app/src/main/res" -I "$ANDROID_JAR" \
    --rename-manifest-package com.openminis.mirrorwhitelist \
    --min-sdk-version 33 --target-sdk-version 35 \
    --version-code 5 --version-name 1.3.1 -F "$OUT/resources.apk"
(cd "$OUT/dex" && zip -q "$OUT/resources.apk" classes.dex)
"$ZIPALIGN" -f 4 "$OUT/resources.apk" "$OUT/MirrorWhitelist-unsigned.apk"
echo 'Built out/MirrorWhitelist-unsigned.apk (unsigned)'
