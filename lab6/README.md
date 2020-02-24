# Lab 6: Container & Kubernetes Security

## Setup

### Minikube

If you are using Minikube then you need to follow these steps 
to use local docker container images:

```shell script
minikube start
eval $(minikube docker-env)
./gradlew docker
```

To see the exposed url for a service in Minikube just use:

```shell script
minikube service list
```

### Docker for Windows/Mac

If you are using Docker for Windows or Mac then the integrated single node 
Kubernetes automatically can use local container images for deployments.

## Deploy Images to Kubernetes

If you have docker installed you can work with building local docker images yourself and
deploy these to Kubernetes.

As alternative you may also just use the corresponding container images 
already available remotely:

* [andifalk/library-server-container-root](https://hub.docker.com/repository/docker/andifalk/library-server-container-root)
* [andifalk/library-server-container-rootless](https://hub.docker.com/repository/docker/andifalk/library-server-container-rootless)

Use tags _latest_ or _1.0_.

## Labs

* [Docker as Root](lab6/library-server-container-root)
* [Docker as NonRoot](lab6/library-server-container-rootless)
* [Kubernetes Deployment](lab6/kubernetes/first-iteration)
* [Secure Kubernetes Deployment](lab6/kubernetes/second-iteration)



