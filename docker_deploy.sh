#!/bin/zsh

sbt clean
sbt assembly
sbt docker:publishLocal
docker image tag iotkafkawindvane:latest intel-server-03:5000/iotkafkawindvane
docker image push intel-server-03:5000/iotkafkawindvane
# Server side:
# kubectl apply -f /home/appuser/deployments/iotKafkaWindVane.yaml
# If needed:
# kubectl delete deployment iot-kafka-windvane
# For troubleshooting
# kubectl exec --stdin --tty iot-kafka-windvane -- /bin/bash