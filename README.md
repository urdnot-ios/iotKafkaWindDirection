# Wind Vane

This code is from 2017 and needs a refresh.

The source data flows from an outdoor anemometer to a Kafka topic. This code picks it up from there, verifies and parses it, then loads it in an InfluxDB for consumption

##### TODO (8/2020):
1. ~~Upgrade to Scala 2.13.2~~
2. ~~Swap out Play JSON for circe (in progress)~~
3. ~~Improve error handling and future returns~~
4. Add kafka and influxdb tests