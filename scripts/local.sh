#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
compose=(docker compose -f docker/docker-compose.yml)

usage() {
    echo "Uso: ./scripts/local.sh {up|deps|run|test|status|logs|db|stop|down}"
    echo "up: tudo em containers; deps: somente PostgreSQL e RabbitMQ"
    echo "run: dependencias + Java no terminal; test: suite completa via Maven Wrapper"
    echo "stop: para somente a API; down: para containers e preserva os dados"
}

action="${1:-help}"
case "$action" in
    up|deps|run|test|status|logs|db|stop|down) ;;
    help|-h|--help) usage; exit 0 ;;
    *) usage; exit 1 ;;
esac

if ! command -v docker >/dev/null 2>&1 && [ -x /Applications/Docker.app/Contents/Resources/bin/docker ]; then
    export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"
fi
if ! command -v docker >/dev/null 2>&1; then
    echo "Docker nao encontrado. No macOS: brew install --cask docker-desktop" >&2
    exit 1
fi
if ! docker_os="$(docker info --format '{{.OSType}}' 2>/dev/null)"; then
    echo "Inicie o Docker Desktop e aguarde o engine ficar pronto." >&2
    exit 1
fi
if [ "$docker_os" != linux ]; then
    echo "Selecione containers Linux no Docker Desktop." >&2
    exit 1
fi
docker compose version >/dev/null

case "$action" in
    up)
        command -v curl >/dev/null 2>&1 || { echo "Instale curl para verificar a API." >&2; exit 1; }
        "${compose[@]}" up -d --build --wait
        for attempt in {1..60}; do
            if curl --fail --silent --max-time 2 http://localhost:8080/hello >/dev/null; then
                echo "API pronta: http://localhost:8080/hello"
                echo "RabbitMQ: http://localhost:15672 (app / app)"
                exit 0
            fi
            sleep 1
        done
        "${compose[@]}" logs --tail 100 endpoint
        echo "A API nao ficou pronta. Confira os logs acima." >&2
        exit 1
        ;;
    deps) "${compose[@]}" up -d --wait postgres rabbitmq ;;
    run|test)
        ./mvnw -version
        if [ "$action" = run ]; then
            if [ -n "$("${compose[@]}" ps --status running -q endpoint)" ]; then
                echo "A API ja esta em container. Execute ./scripts/local.sh stop antes de usar run." >&2
                exit 1
            fi
            "${compose[@]}" up -d --wait postgres rabbitmq
            exec ./mvnw spring-boot:run
        fi
        exec ./mvnw test
        ;;
    status) "${compose[@]}" ps ;;
    logs) "${compose[@]}" logs -f endpoint ;;
    db) "${compose[@]}" exec postgres psql -U app -d estudojava ;;
    stop) "${compose[@]}" stop endpoint ;;
    down) "${compose[@]}" down ;;
esac
