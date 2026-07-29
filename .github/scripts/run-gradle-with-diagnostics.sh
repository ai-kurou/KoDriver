#!/usr/bin/env bash
set -euo pipefail

limit_seconds="$1"
shift

dump_diagnostics() {
  echo "== Process list =="
  ps -ef || true

  echo "== Java thread dumps =="
  if ! command -v jcmd >/dev/null 2>&1 && ! command -v jstack >/dev/null 2>&1; then
    echo "Neither jcmd nor jstack is available."
    return
  fi

  while IFS= read -r java_pid; do
    echo "---- Java PID ${java_pid} ----"
    if command -v jcmd >/dev/null 2>&1; then
      jcmd "${java_pid}" Thread.print || true
    else
      jstack "${java_pid}" || true
    fi
  done < <(pgrep -f 'java|GradleDaemon|GradleWorkerMain' || true)
}

echo "Running with diagnostic timeout: ${limit_seconds}s"
echo "Command: $*"

"$@" &
gradle_pid="$!"
timeout_marker="$(mktemp)"
rm -f "${timeout_marker}"
trap 'rm -f "${timeout_marker}"' EXIT

(
  sleep "${limit_seconds}"
  if kill -0 "${gradle_pid}" 2>/dev/null; then
    touch "${timeout_marker}"
    echo "::error::Gradle command exceeded diagnostic timeout (${limit_seconds}s). Dumping process state."
    dump_diagnostics

    echo "Stopping Gradle command."
    kill -TERM "${gradle_pid}" 2>/dev/null || true
    sleep 30
    kill -KILL "${gradle_pid}" 2>/dev/null || true
  fi
) &
watchdog_pid="$!"

set +e
wait "${gradle_pid}"
exit_code="$?"
kill "${watchdog_pid}" 2>/dev/null
wait "${watchdog_pid}" 2>/dev/null
set -e

if [[ -f "${timeout_marker}" ]]; then
  exit 124
fi

if (( exit_code != 0 )); then
  echo "::error::Gradle command failed with exit code ${exit_code}. Dumping process state."
  dump_diagnostics
fi

exit "${exit_code}"
