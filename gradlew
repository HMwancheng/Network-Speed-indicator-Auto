#!/usr/bin/env sh

export GRADLE_HOME="$HOME/.gradle"

dirname="$(cd "$(dirname "$0")" && pwd)"

if [ -d "$dirname/gradle/wrapper" ]; then
    if [ -x "$dirname/gradle/wrapper/gradle-wrapper.jar" ]; then
        java -jar "$dirname/gradle/wrapper/gradle-wrapper.jar" "$@"
        exit $?
    fi
fi

echo "Error: gradle-wrapper.jar not found!"
exit 1
