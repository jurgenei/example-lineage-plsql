#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

cd "$PROJECT_ROOT"
./gradlew --no-daemon :knowledgeGraph:verifyKnowledgeGraphModel
echo "Verification report: $PROJECT_ROOT/4-knowledge-graph/build/graph-report/verification-report.json"
