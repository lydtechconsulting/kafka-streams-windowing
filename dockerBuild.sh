#!/bin/sh

pushd link-data-generator
docker build -t link-data-generator-app:latest .
popd

pushd tumbling-window-app
docker build -t tumbling-window-app:latest .
popd

pushd hopping-window-app
docker build -t hopping-window-app:latest .
popd

pushd sliding-window-app
docker build -t sliding-window-app:latest .
popd

