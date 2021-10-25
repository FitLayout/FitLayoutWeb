# FitLayout/2 - Web Page Analysis Framework - Web Application Backend
(c) 2015-2021 Radek Burget (burgetr@fit.vutbr.cz)

This project provides a backend for [FitLayout](https://github.com/FitLayout/FitLayout) demo application. The frontend is provided by the [PageView](https://github.com/FitLayout/PageView) project. The project is also available as [docker images](https://github.com/FitLayout/docker-images). 

## Installation

FitLayoutWeb is a Java microservice that can run on any *microprofile* compliant application server, e.g. Glassfish, Payara or Open Liberty. For compiling the project, use

```bash
git clone https://github.com/FitLayout/FitLayoutWeb.git
cd FitLayoutWeb
mvn clean package
```

The compiled application can be found in `FitLayoutWebService/targer/fitlayout-web.war` and it is ready for deployment on a server.

Additionally, a standalone all-in-one server with an embedded Payara-micro server can be built using

```bash
cd FitLayoutWebService
mvn payara-micro:bundle
```

Then the server can be started:

```bash
java -jar target/fitlayout-web-microbundle.jar
```

Use ``--help`` for additional options. The server configuration can be defined in a *properties* file (see [an example configuration](https://github.com/FitLayout/FitLayoutWeb/blob/main/FitLayoutWebService/src/main/resources/META-INF/microprofile-config-single.properties) here) and used using the `--systemproperties` switch.
