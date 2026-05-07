# FitLayout/2 - Web Page Analysis Framework - Web Application Backend
(c) 2015-2026 Radek Burget (burgetr@fit.vutbr.cz)

This project provides a backend for [FitLayout](https://github.com/FitLayout/FitLayout) demo application. The frontend is provided by the [PageView](https://github.com/FitLayout/PageView) project. The project is also available as [docker images](https://github.com/FitLayout/docker-images). 

## Installation

FitLayoutWeb is a Java microservice that can run on any *microprofile* compliant application server, e.g. Open Liberty, Payara or Glassfish. For compiling the project, use

```bash
git clone https://github.com/FitLayout/FitLayoutWeb.git
cd FitLayoutWeb
mvn clean package
```

The compiled application can be found in `FitLayoutWebService/targer/fitlayout-web.war` and it is ready for deployment on a server.

The build already produces a standalone all-in-one runnable JAR with an embedded Open Liberty server at `FitLayoutWebService/target/fitlayout-web.jar`. Start it with:

```bash
java -jar FitLayoutWebService/target/fitlayout-web.jar
```

The server listens on port **8088** by default. The port and other configuration properties (see [an example](https://github.com/FitLayout/FitLayoutWeb/blob/main/FitLayoutWebService/src/main/resources/META-INF/microprofile-config-single.properties)) can be passed as JVM `-D` arguments:

```bash
java -Dhttp.port=8400 -Dfitlayout.rdf.storage=single -Dfitlayout.rdf.serverUrl=http://... \
  -jar FitLayoutWebService/target/fitlayout-web.jar
```
