# k8s-kafka-stream-hackafun
sliding window example
data creator app deployed to k8s published payment records to 'payments' topic.
streams-payment-app consumes the messages and reports the total spend for an account within a sliding window.  The output is visible in the logs

## Setup

### Start Minikube
```
minikube start
```

### Start Dashboard
In a different terminal start the dashboard
```
minikube dashboard
```

Configure to use minikube internal docker as docker host:
```
minikube docker-env
eval $(minikube -p minikube docker-env)
```

Build the apps
```
mvn clean install
```

Then rebuild the docker image which so that it’s installed in the minikube registry, instead of the local one.
The script `dockerBuild.sh` will build all the apps for you.  If you wish to build the apps individually, just relevant copy the command from within this script
```
sh dockerBuild.sh
```


### Apply the k8s configs

The following scripts create the environment for us and deploy the stream apps.
```
kubectl apply -f  k8s/00-namespace.yaml 
kubectl apply -f  k8s/01-zookeeper.yaml 
kubectl apply -f  k8s/02-kafka.yaml 
kubectl apply -f  k8s/04-link-data-generator-app.yaml
kubectl apply -f  k8s/05-tumbling-window-app.yaml 
kubectl apply -f  k8s/06-hopping-window-app.yaml 
kubectl apply -f  k8s/07-sliding-window-app.yaml 
```

The deployment will be in the kafka-streams-windowing namespace.   Once the services have sorted themselves out and established connections, you should be able to see them all running via the minikube dashboard



## Observable Outputs....

What can we see?
data-creator logs contain records being emitted
each window-app has logs containing: 
- a peek of the ingested record
- a peek of the final event (window aggregation)



## Consuming the outputs
Log onto the kafka pod and we can interact with kafka and consume topics.  

#### create a bash session on the container
The following command allows you to jump onto the container with a bash session.   You need to find the pod name to do this. 
```kubectl -n kafka-streams-windowing exec -it <kafka-service pod name> -- /bin/bash```

This command does the same as above, but discovers the kafka service pod name for you
```
kubectl -n kafka-streams-windowing exec -it $(kubectl get pods -n kafka-streams-windowing -o custom-columns=":metadata.name" | grep kafka-service) -- /bin/bash
```
note the nested kubectl command get the name of the kafka container to exec

#### List the topics available
```
/bin/kafka-topics --bootstrap-server localhost:9092 --list
```
list the topics then you will see the persistence recorded in the changelog topic `payment-streams-app-ACCOUNT_AGGREGATED_TOTAL-changelog`

####  Consume the 
consume the outputs via the topic
`kafka-console-consumer --bootstrap-server localhost:9092 --topic <topic name>`

e.g.
```
kafka-console-consumer --bootstrap-server localhost:9092 --topic link.tumbling
```
