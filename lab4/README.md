# Lab 4: Securing Microservices with Mutual TLS

## System Requirements

For this lab you need the following requirements:

* [mkcert](https://mkcert.dev/) to create trusted certificates for localhost. Please follow 
  the [installation instructions](https://github.com/FiloSottile/mkcert#installation) to set this up
  on your machine.
* [Keystore Explorer](https://keystore-explorer.org/) to manage keystore contents. To install it just 
  go to the [Keystore Downloads](https://keystore-explorer.org/downloads.html) page and get the appropriate
  installer for your operating system  

## Setup a local Certificate Authority (CA)

To create a local certificate authority (with your own root certificate)
use the following command.
Make sure you also have set the _JAVA_HOME_ environment variable if you also want 
to install the root certificate into the trust store of your JDK. 

```shell
export JAVA_HOME="$(dirname $(dirname $(readlink -f $(which java))))"
mkcert -install
```

This leads to an output similar to the following.

```
Using the local CA at "/home/xxx/.local/share/mkcert" ✨
The local CA is installed in the system trust store! 👍
The local CA is installed in the Firefox and/or Chrome/Chromium trust store! 👍
Sudo password:xxxx
The local CA is now installed in Java's trust store! ☕️
```

## Setup HTTPS (SSL/TLS) for the application

At first you need a valid trusted server certificate.  
To create a keystore containing the certificate with private/public key pair 
open a command line terminal then navigate to the subdirectory _src/main/resources_ of this project 
and use the following command.

```
mkcert -p12-file server-keystore.p12 -pkcs12 localhost mydev.local
```

Now you should have created a new file _server-keystore.p12_ in the subdirectory _src/main/resources_.

To enable SSL/TLS in the spring boot application add the following entries to the application.properties

```properties
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:server-keystore.p12
server.ssl.key-store-type=PKCS12
server.ssl.key-store-password=changeit
server.ssl.key-password=changeit
```

## Setup the client certificate

First we need of course again a valid trusted client certificate to authenticate 
our client at the server.
Open a command line terminal again and navigate to subdirectory _src/main/resources_ of this project
and then use the following command.

```
mkcert -p12-file client-keystore.p12 -client -pkcs12 myuser
```

This file contains the client certificate including the private/public key pair.
To authenticate your web browser for our Spring Boot server application just import
the file _client-keystore.p12_ into the browsers certificate store.

But this is not sufficient, the server application also needs just the certificate (with public key)
to be able to validate the client certificate.
To achieve this we also need to configure a trust keystore for Spring Boot. 
You must not use the keystore we just created because the server should not get access to the private key.

Instead we have to create another keystore using the [Keystore Explorer](https://keystore-explorer.org/)
that only contains the certificate.

But first we have to export the certificate from the existing keystore _client-keystore.p12_:

1. Open keystore with the Keystore Explorer. Select _client-keystore.p12_ in file dialog.
2. Then right click on the single entry and select _Export/Export certificate chain_ and then use the 
   settings as shown in the figure below.
   
![CertExport](images/cert_export.png)   

Now we can import the exported single certificate into a new keystore.

1. Open the explorer and then create a new keystore using the menu _File/New_. 
2. Then chose _PKCS#12_ as type
3. Now select the menu _Tools/Import Trusted Certificate_
4. Select the exported file from previous section
5. Save the keystore as _myuser-trust.p12_ and use password _changeit_ when prompted for

Now let's use this new keystore:

```properties
server.ssl.trust-store=classpath:myuser-trust.p12
server.ssl.trust-store-password=changeit
server.ssl.client-auth=need
```

### Reference Documentation
For further reference, please consider the following sections:

* [Spring Security](https://docs.spring.io/spring-boot/docs/2.2.4.RELEASE/reference/htmlsingle/#boot-features-security)

### Guides
The following guides illustrate how to use some features concretely:

* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)



