#!/bin/bash
set -e
SDK="$HOME/android-sdk"
BT="$SDK/build-tools/34.0.0"
JAR="$SDK/platforms/android-28/android.jar"
rm -rf obj bin && mkdir -p obj bin
javac -parameters --release 8 -d obj -cp "$JAR" src/com/lite/lock/*.java
"$BT/d8" --release --lib "$JAR" --output bin/ $(find obj -name "*.class")
"$BT/aapt" package -f -M AndroidManifest.xml -I "$JAR" -F obj/unaligned.apk bin/
"$BT/zipalign" -f 4 obj/unaligned.apk bin/iLock.apk
"$BT/apksigner" sign --ks ~/.android/debug.keystore --ks-pass pass:android bin/iLock.apk
echo "Build successful: bin/iLock.apk"
