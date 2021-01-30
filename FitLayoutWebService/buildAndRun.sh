#!/bin/sh
mvn clean package && mvn payara-micro:bundle && docker build -t fitlayout/fitlayout-server .
docker rm -f fitlayout-server || true && docker run -d -p 8400:8400 --name fitlayout-server --restart unless-stopped fitlayout/fitlayout-server 
