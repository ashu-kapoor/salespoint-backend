
cd F:/WorkProjects/kafka_2.13-3.4.0/bin

./kafka-topics.sh --bootstrap-server localhost:9092 --topic INVENTORY --create --partitions 1
./kafka-topics.sh --bootstrap-server localhost:9092 --topic CUSTOMER --create --partitions 1
./kafka-topics.sh --bootstrap-server localhost:9092 --topic SALES_SAGA --create --partitions 1
