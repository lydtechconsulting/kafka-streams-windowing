# Kafka Streams Windowing Examples

Welcome to some simple examples of Kafka Streams Windowing capabilities.
The project has a number of purposes
- Demonstrate the basic bahaviours and differences in the Windowing options for Kafka Streams
- Explores the windowing types using tests. 
- Expand on the demonstration by creating a K8s environment in minikube that runs each of the window types

## Window type examples & test
The test in each window-app can be executed via your IDE and stepped through to provide an environment for exploring the behaviours of each window

Build and Test using mvn
```asciidoc
mvn clean install
```


# Run in K8s - Setup
It is possible to spin up each windowing app in minikube and see the windows in action.  
The setup includes: a kafka instance (and zookeeper), a data generator, an app for each window type, and instructions on how to consume the events for a complete e2e demonstration

For the K8s we will use minikube.  A download/install guide for minikube can be found here: https://minikube.sigs.k8s.io/docs/start/

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

Build the apps.  Go to your checked out project and build with mvn.
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
