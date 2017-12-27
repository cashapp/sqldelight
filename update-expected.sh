#!/usr/bin/env bash

set -ex

./gradlew clean :sqldelight-gradle-plugin:testClasses

set +e

# Run the tests for the Gradle plugin. This will generate all the expected outputs and might fail.
./gradlew :sqldelight-gradle-plugin:test

set -e

for fixture in `find sqldelight-gradle-plugin/src/test/fixtures -type d -maxdepth 1 -mindepth 1`; do
  expected="$fixture/expected/"
  if [ -d "$expected" ]; then
    rsync -r --delete "$fixture/build/generated/source/sqldelight/" "$expected"
  fi
done
