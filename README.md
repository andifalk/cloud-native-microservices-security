![License](https://img.shields.io/badge/License-Apache%20License%202.0-brightgreen.svg)
![Java Build](https://github.com/andifalk/cloud-native-microservices-security/workflows/Java%20CI/badge.svg)
[![Release](https://img.shields.io/github/release/andifalk/cloud-native-microservices-security.svg?style=flat)](https://github.com/andifalk/cloud-native-microservices-security/releases)

# Cloud-Native Microservice Security Boot-Camp

## Tutorial

[Tutorial](https://andifalk.gitbook.io/cloud-native-microservices-security)

## Slides

* [Slides for Workshop (PDF)](https://github.com/andifalk/cloud-native-microservices-security/raw/master/Cloud%20Native%20Microservice%20Security.pdf)

## Introduction

* [Requirements and Setup](setup)  
* [Demo Application Architecture](application-architecture)  


## Hands-On Labs
     
* [1.Security via Spring Boot Auto-Configuration](lab1)    
* [2.Customized Authentication](lab2)
* [3.Mutual TLS (MTLS)](lab3)
* [4.Authorization](lab4)
* [5.Automated Testing](lab5)
* [6.Kubernetes Security](lab6)
  * [6.1.Docker as Root](lab6/library-server-container-root)
  * [6.2.Docker as NonRoot](lab6/library-server-container-rootless)
  * [6.3.Kubernetes Deployment](lab6/kubernetes/first-iteration)
  * [6.4.Secure Kubernetes Deployment](lab6/kubernetes/second-iteration)

## Bonus Labs

* [CSRF Attack Demo](bonus-labs/csrf-attack-demo)
* [Web Authn](bonus-labs/webauthn)