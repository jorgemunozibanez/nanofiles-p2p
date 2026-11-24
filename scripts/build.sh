#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source_root="$repo_root/src"
output_root="$repo_root/build/classes"

mkdir -p "$output_root"
find "$source_root" -name '*.java' -print0 \
  | xargs -0 javac --release 17 -encoding UTF-8 -d "$output_root"

printf 'Compiled Java sources into %s\n' "$output_root"
