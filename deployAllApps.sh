#!/bin/sh

kubectl apply -f  k8s/04-link-data-generator-app.yaml
kubectl apply -f  k8s/05-tumbling-window-app.yaml
kubectl apply -f  k8s/06-hopping-window-app.yaml
kubectl apply -f  k8s/07-sliding-window-app.yaml

