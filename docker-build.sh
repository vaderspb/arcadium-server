#!/bin/bash

ARCADIUM_VERSION=0.0.1v5

docker build -t vaderspb/arcadium-builder --no-cache -f Dockerfile.builder .
docker build -t vaderspb/arcadium-api-server:$ARCADIUM_VERSION --no-cache -f Dockerfile.apiserver .
docker build -t vaderspb/arcadium-session-server:$ARCADIUM_VERSION --no-cache -f Dockerfile.sessionserver .
docker build -t vaderspb/arcadium-transport:$ARCADIUM_VERSION --no-cache -f Dockerfile.transport .
docker build -t vaderspb/arcadium-worker:$ARCADIUM_VERSION --no-cache -f Dockerfile.worker .
