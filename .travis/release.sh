#!/bin/bash

set -e

echo "Ensuring that pom <version> matches $TRAVIS_TAG"
./mvnw org.codehaus.mojo:versions-maven-plugin:2.5:set -DnewVersion=$TRAVIS_TAG

echo "Uploading to oss repo and GitHub"
./mvnw deploy -DskipTests=true --batch-mode --update-snapshots -Prelease
