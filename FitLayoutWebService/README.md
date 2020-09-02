# Build
mvn clean package && docker build -t com.airhacks/fitlayout-web .

# RUN

docker rm -f fitlayout-web || true && docker run -d -p 8080:8080 -p 4848:4848 --name fitlayout-web com.airhacks/fitlayout-web 

# System Test

Switch to the "-st" module and perform:

mvn compile failsafe:integration-test