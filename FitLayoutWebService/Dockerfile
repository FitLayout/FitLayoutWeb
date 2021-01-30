FROM ubuntu

RUN apt-get update \
&& DEBIAN_FRONTEND=noninteractive apt-get -y install openjdk-11-jdk locales \
&& rm -rf /var/lib/apt/lists/* \
&& localedef -i en_US -c -f UTF-8 -A /usr/share/locale/locale.alias en_US.UTF-8

ENV LANG en_US.utf8
ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-amd64

ENV DEPLOYMENT_DIR /opt
ENV BUNDLE fitlayout-web-microbundle.jar
ENV PORT 8400

COPY ./target/${BUNDLE} ${DEPLOYMENT_DIR}
COPY ./config/config.properties ${DEPLOYMENT_DIR}

ENTRYPOINT java -jar ${BUNDLE} --port ${PORT} --systemproperties ${DEPLOYMENT_DIR}/config.properties
WORKDIR ${DEPLOYMENT_DIR}
EXPOSE ${PORT}
