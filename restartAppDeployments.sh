#!/bin/sh

for app in `kubectl get deployments -n kafka-streams-windowing -o custom-columns=":metadata.name" | grep app`
do  
	kubectl rollout restart deployment $app -n kafka-streams-windowing
done
