# Spline Server Open lineage Integration
Applications in this repo provide a way to send an **open lineage** messages into **spline server**.


- **rest-endpoint** provides rest open lineage api and inserts all messages directly to kafka
- **Aggregator** consumes the messages form kafka and merges all messages for one run. 
Once the `COMPLETE` message arrives all the data are converted into one Spline Plan and one Event message. 
In the last step messages are inserted into another kafka topic where they can be consumed by spline kafka server.

### Hwo To Build
Building artifacts jar/war
```bash
mvn clean package
```

Building docker images
```bash
mvn clean install -Pdocker -Ddockerfile.repositoryUrl=my
```
### How To Run
#### Prerequisities
- Running kafka broker, for this example it is on `localhost:9092`
- Two kafka topics (one for messages in open lineage format and one for messages in spline format). 
For this example it will be `open-lineage-topic` and `spline-lineage-topic`

#### Runing in docker
For rest endpoint I have to map the port so I am able to acces it outside of docker and also set the open lineage topic and addres of kafka broker. 
It's important to replace localhost for appropriate address that works from inside the container. For macOS it is `host.docker.internal`.

```
docker run -p 8086:8080 \
    -e JAVA_OPTS="-Dspline.ol.topic=open-lineage-topic -Dspline.ol.producer.bootstrap.servers=host.docker.internal:9092" \
    my/spline-open-lineage-rest-endpoint
```

```
docker run \
    -e JAVA_OPTS="-Dspline.ol.inputTopic=open-lineage-topic -Dspline.ol.outputTopic=spline-lineage-topic -Dspline.ol.streams.bootstrap.servers=host.docker.internal:9092" \
    my/spline-open-lineage-aggregator
```
