# Wind Vane

Data flows from an outdoor anemometer to a Kafka topic. This code picks it up from there, verifies and parses it, then loads it in an InfluxDB for consumption