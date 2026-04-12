#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
./mvnw -B -pl {{PROCESSOR_MODULE_NAME}} test -Pjvm
