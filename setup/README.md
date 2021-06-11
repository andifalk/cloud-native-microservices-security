# Requirements and Setup

## Requirements

### General Requirements

* [Java SDK](https://adoptopenjdk.net) Version 11 (LTS version) or later should work
* A Java IDE like
  * [Eclipse](https://www.eclipse.org/downloads)
  * [Spring Toolsuite](https://spring.io/tools)
  * [IntelliJ](https://www.jetbrains.com/idea/download)
  * [Visual Studio Code](https://code.visualstudio.com)
  * ...
* [Git](https://git-scm.com)
* [Postman](https://www.getpostman.com/downloads), [Httpie](https://httpie.org/#installation), or [Curl](https://curl.haxx.se/download.html) for REST calls

In case you select [Postman](https://www.getpostman.com/downloads), then the provided [Postman Collection](../postman/library-server/Library-Server.postman_collection.json) might be helpful.
Just import this [Postman Collection (Version 2.1 format)](../postman/library-server/Library-Server.postman_collection.json) into Postman.

### Requirements for the MTLS lab

* [mkcert](https://mkcert.dev/) to create trusted certificates for localhost. Please follow 
  the [installation instructions](https://github.com/FiloSottile/mkcert#installation) to set this up
  on your machine.
* [Keystore Explorer](https://keystore-explorer.org/) to manage keystore contents. To install it just go to the [Keystore Downloads](https://keystore-explorer.org/downloads.html) page and get the appropriate
  installer for your operating system
  
### Requirements for the Kubernetes lab  

Depending on your operating system you have different choices to install Docker and a local Kubernetes cluster for this lab.

* __Windows__: On Windows you can install [Docker Desktop for Windows](https://docs.docker.com/docker-for-windows/install) together with included standalone Kubernetes server/client
* __Mac OS__: On MAC you can install [Docker Desktop for Mac](https://docs.docker.com/docker-for-mac/install) together with included standalone Kubernetes server/client
* __Linux__: On Linux you might already have installed native Docker support, otherwise install [Docker for Linux](https://hub.docker.com/search?q=&type=edition&offering=community&operating_system=linux).
  Regarding Kubernetes, you can either install [Minikube](https://minikube.sigs.k8s.io/docs/start) + [VirtualBox](https://www.virtualbox.org/) or an alternative one like [Kind](https://kind.sigs.k8s.io/docs/user/quick-start).

__Note:__ [Minikube](https://minikube.sigs.k8s.io/docs/start) + [VirtualBox](https://www.virtualbox.org/) would also be an alternative for Windows or Mac as well.

### IntelliJ specific requirements

IntelliJ does not require any specific additional plugins or configuration.

### Eclipse IDE specific requirements

If you are an Eclipse user, then the usage of the Eclipse-based [Spring Toolsuite](https://spring.io/tools) is strongly recommended.
This eclipse variant already has all the required gradle and spring boot support pre-installed.

In case you want to stick to your plain Eclipse installation then you have to add the following features via the
eclipse marketplace: 

* Buildship Gradle Integration (Version 3.x). This might be already pre-installed depending 
on your eclipse variant (e.g., Eclipse JavaEE) installed
* Spring Tools 4 for Spring Boot (Spring Tool Suite 4)

### Visual Studio Code specific requirements

To be able to work properly in Visual Studio Code with this Spring Boot Java Gradle project you need at least these extensions:

* Java Extension Pack
* vscode-gradle-language
* VS Code Spring Boot Application Development Extension Pack

## Get the source code
                       
Clone this GitHub repository (https://github.com/andifalk/cloud-native-microservices-security):

```
git clone https://github.com/andifalk/cloud-native-microservices-security.git security_workshop
```

After that you can import the whole workshop project directory into your IDE as a __gradle project__:

* [IntelliJ](https://www.jetbrains.com/idea): "New project from existing sources..." and then select 'Gradle' when prompted
* [Eclipse](https://www.eclipse.org/) or [Spring ToolSuite](https://spring.io/tools): "Import/Gradle/Existing gradle project"
* [Visual Studio Code](https://code.visualstudio.com/): Just open the root directory and wait until VS Code configured the project


