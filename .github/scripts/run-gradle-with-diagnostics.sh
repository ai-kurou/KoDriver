#!/usr/bin/env bash
set -euo pipefail

limit_seconds="$1"
shift

echo "Running with diagnostic timeout: ${limit_seconds}s"
echo "Command: $*"

"$@" &
gradle_pid="$!"
deadline=$((SECONDS + limit_seconds))

while kill -0 "${gradle_pid}" 2>/dev/null; do
  if (( SECONDS >= deadline )); then
    echo "::error::Gradle command exceeded diagnostic timeout (${limit_seconds}s). Dumping process state."
    echo "== Process list =="
    ps -ef

    echo "== Java thread dumps =="
    while IFS= read -r java_pid; do
      echo "---- Java PID ${java_pid} ----"
      if command -v jcmd >/dev/null 2>&1; then
        jcmd "${java_pid}" Thread.print || true
      elif command -v jstack >/dev/null 2>&1; then
        jstack "${java_pid}" || true
      else
        echo "Neither jcmd nor jstack is available."
      fi
    done < <(pgrep -f 'java|GradleDaemon|GradleWorkerMain' || true)

    echo "Stopping Gradle command."
    kill -TERM "${gradle_pid}" 2>/dev/null || true
    sleep 30
    kill -KILL "${gradle_pid}" 2>/dev/null || true
    wait "${gradle_pid}" 2>/dev/null || true
    exit 124
  fi
  sleep 30
done

wait "${gradle_pid}"
