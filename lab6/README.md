# Lab 6: Container & Kubernetes Security

## Minikube

minikube start

eval $(minikube docker-env)

minikube service list

## Deploy Root Image

kubectl apply -f ./deploy.yaml           
deployment.apps/library-server-root created

kubectl apply -f ./service.yaml 
service/library-server-root created

## Deploy Rootless Image






