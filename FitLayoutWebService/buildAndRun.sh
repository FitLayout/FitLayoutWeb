#!/bin/sh
mvn clean package && docker build -t com.airhacks/fitlayout-web .
docker rm -f fitlayout-web || true && docker run -d -p 8080:8080 -p 4848:4848 --name fitlayout-web com.airhacks/fitlayout-web 
