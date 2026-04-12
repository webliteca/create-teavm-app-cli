#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
./mvnw -B -pl {{APP_NAME}}-api test -Pjvm
