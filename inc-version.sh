#!/bin/bash


# To increment the version, use the Makefile
# make publish
# which will call out to this script


# Use maven-help-plugin to get the current project.version
CURRENT_VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\['`

# Increment the minor version
NEW_VERSION="${CURRENT_VERSION%.*}.$((${CURRENT_VERSION##*.}+1))"

# Use maven-help-plugin to update the project.version
mvn versions:set -DnewVersion=$NEW_VERSION -DgenerateBackupPoms=false

# Commit the changes
git add common/pom.xml
git add grpc/pom.xml
git add okhttp/pom.xml
git add example/pom.xml
git add pom.xml
