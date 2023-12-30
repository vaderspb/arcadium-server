#!/bin/bash

source ./docker-version.sh

docker push vaderspb/arcadium-api-server:${ARCADIUM_VERSION}
docker push vaderspb/arcadium-session-server:${ARCADIUM_VERSION}
docker push vaderspb/arcadium-transport:${ARCADIUM_VERSION}
docker push vaderspb/arcadium-worker:${ARCADIUM_VERSION}
docker push vaderspb/arcadium-storage:${ARCADIUM_VERSION}
