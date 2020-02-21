# Non-Root Docker Container using Google JIB

This time we configure a non-root user in the _Dockerfile_ to build a container image that will run using
without the root user.

```dockerfile
FROM openjdk:11.0.6-jre-buster
COPY step3-hello-rootless-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
RUN addgroup --system --gid 1002 app && adduser --system --uid 1002 --gid 1002 appuser
USER 1002
ENTRYPOINT java -jar /app.jar
```

Regarding the group-id (gid) and user-id (uid) you should use one above '1000' to avoid using any system user.
If you want to be really on the safe side you even leave out all local users (reserved numbers up to 10000) by chosing a number above '10000' (reserved for remote users).

You can prove that the container now does not run with root any more by using these commands:

```bash
docker container run --rm --detach --name hello-rootless \
-p 8080:8080 andifalk/hello-rootless:latest
docker exec hello-rootless whoami
```

This should return the following user information (it should not be root any more)

```bash
appuser
```

But this time instead of using a _Dockerfile_ we will use [Google JIB](https://github.com/GoogleContainerTools/jib) to build the container image.

Using JIB has the following advantages compared to classical image creation using Dockerfile:  

* With JIB you even can build a container image without a docker daemon installed on your machine.
* Building images repeatedly is much faster as JIB optimizes this to the typical development flow (i.e. the application code changes much more frequently then dependencies).
* JIB uses the [Google Distroless Base Images](https://github.com/GoogleContainerTools/distroless) that only include the minimum components just to execute the desired process (e.g. Go or Java)

JIB works by using adding a plugin to your maven or gradle build.
So here we add the plugin to our gradle build and also configure a non-root user in the _gradle.build_ file to build a container image that will run using
without the root user.

```groovy
plugins {
    id "com.google.cloud.tools.jib" version "2.0.0"
}
jib {
    to {
        image = 'andifalk/hello-rootless-jib:latest'
    }
    container {
        user = 1002
    }
}
```
  
You can prove this by using these commands:

```shell
docker container run --rm --detach --name hello-rootless-jib \
-p 8080:8080 andifalk/hello-rootless-jib:latest
docker exec hello-rootless-jib whoami
```

This time this should report an error as in the [distroless image](https://github.com/GoogleContainerTools/distroless), as used by JIB as default, there even is no shell installed and so no _whoami_ command is possible.

You should also be able to reach the dockerized application again via [localhost:8080](http://localhost:8080).

Finally stop the running container by using the following command:

```shell
docker stop hello-rootless-jib
```

## Check image for Vulnerabilities

Now we can check our image for vulnerabilities with high and critical severities using this command:

```shell
trivy --severity HIGH,CRITICAL andifalk/hello-rootless-jib:latest
```

## Next

[Next: Initial Unsafe K8s Deploy](../step5-initial-k8s-deploy)
