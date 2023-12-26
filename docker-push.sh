#!/bin/bash

ARCADIUM_VERSION=0.0.1v5

docker push vaderspb/arcadium-api-server:$ARCADIUM_VERSION
docker push vaderspb/arcadium-session-server:$ARCADIUM_VERSION
docker push vaderspb/arcadium-transport:$ARCADIUM_VERSION
docker push vaderspb/arcadium-worker:$ARCADIUM_VERSION
