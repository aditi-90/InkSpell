#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
if command -v mvn >/dev/null 2>&1; then exec mvn clean package cargo:run; fi
mkdir -p .tools
if [ ! -f .tools/apache-maven-3.9.9/bin/mvn ]; then
 curl -fL https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.tar.gz -o .tools/maven.tar.gz
 tar -xzf .tools/maven.tar.gz -C .tools
fi
exec .tools/apache-maven-3.9.9/bin/mvn clean package cargo:run
