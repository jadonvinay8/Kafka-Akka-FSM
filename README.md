# Kafka-Akka-FSM

## Commands:


- For starting zookeeper

  .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
  
- For starting server

  .\bin\windows\kafka-server-start.bat .\config\server.properties
  
- Creating topic

  .\bin\windows\kafka-topics.bat --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic <topic-name>
  
- Listing all topics

  .\bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092
