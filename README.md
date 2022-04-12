# Spline Server Open lineage Integration
Applications in this repo provide a way to send an **open lineage** messages into **spline server**.


- **rest-endpoint** provides rest open lineage api and inserts all messages directly to kafka
- **Aggregator** consumes the messages form kafka and merges all messages for one run. 
Once the `COMPLETE` message arrives all the data are converted into one Spline Plan and one Event message. 
In the last step messages are inserted into another kafka topic where they can be consumed by spline kafka server.
