#!/bin/bash

source ./docker-version.sh

echo Building arcadium version=${ARCADIUM_VERSION}

docker build -t vaderspb/arcadium-builder --no-cache -f Dockerfile.builder .
docker build -t vaderspb/arcadium-api-server:${ARCADIUM_VERSION} --no-cache -f Dockerfile.apiserver .
docker build -t vaderspb/arcadium-session-server:${ARCADIUM_VERSION} --no-cache -f Dockerfile.sessionserver .
docker build -t vaderspb/arcadium-transport:${ARCADIUM_VERSION} --no-cache -f Dockerfile.transport .
docker build -t vaderspb/arcadium-worker:${ARCADIUM_VERSION} --no-cache -f Dockerfile.worker .
docker build -t vaderspb/arcadium-storage:${ARCADIUM_VERSION} --no-cache -f Dockerfile.storage .
