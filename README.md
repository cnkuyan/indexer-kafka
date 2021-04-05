# Financial Indexer with Kafka Streams

This app provides an API, for the clients to query real-time price statistics of financial instruments received within the last 60 seconds (tumbling time interval), using Kafka Streams and Spring Cloud Streams Framework.

The reason I wanted to provide a Kafka Streams based solution is obvious (besides personal curiosity). That's because, at a larger scale  the characteristics of the problem calls for using such a scalable streaming framework.   ( Apache Flink would've also been a great candidate) .

Unfortunately the Kafka SlidingWindow feature was not officially coupled with the version of the Spring Cloud Stream framework that came with the 
starter parent ( 2.3.4.RELEASE) .

According to this here ( https://github.com/apache/kafka/pull/9039 ), the SlidingWindow feature has been accepted into the codebase .

And this one here  (https://www.confluent.io/blog/apache-kafka-2-7-features-updates-improvements/)  mentions that it has been included in release 2.7 of Apache Kafka .

But it took me a while to find out the latest version of Spring Cloud Stream hasn't packaged it yet,
That wasn't clearly reflected in the official documentation . Perhaps a little bit of more time was needed tweaking it
 
I had to contend with TumblingWindows, instead of SlidingWindows as I would've liked to.


There are three API endpoints:

• POST /ticks  :  This one is called by the clients to register a Tick. It is also the sole input of this rest API
   
• GET /statistics : This one returns the statistics based on the ticks of all instruments of the last 60 seconds (tumbling time interval)

• GET /statistics/{instrument_id} : This one returns the statistics based on the ticks of the given instrument of the last 60 seconds (tumbling time interval)

### Development Assumptions 

 I assumed ;
 1. The /ticks endpoint can get called several times a second.
 2. The application will not be running behind a load balancer. This implementation is a single node application, that can't know what statistics another instance has accumulated.
 

### Possible Improvements To The Application

 The application could possible be improved upon under following topics:
 1. Making it ready to work behind a load balancer, by using a distributed key-value store instead of the local map data structures. 
 2. Giving the ability to scale even more, by providing a sharding mechanism at the core, to distribute the load based on a certain key. 


### Did I Like Working On This Problem

 I like all kinds of challenges. I liked this one as well.
 Aside from personal pleasure, it gave me an introductory sense into the characteristics of the work that goes into the Financial Index collection and distribution .
 

### Configuring the Tumbling Time Window Period:
 
 The application has been configured with a default value of 60 seconds for the sliding time interval.
 In order to change it to another value,  please update the constant value TUMBLING_WINDOWS_SECS defined in KafkaStreamsTickerApp.java
 

### Prerequisites:

You need Maven, Git and  Java SE Runtime Environment 9 (minimum), docker, docker-compose  to run this project. 

```
https://www.oracle.com/java/technologies/javase/javase9-archive-downloads.html
https://maven.apache.org/install.html
https://git-scm.com
https://docker.com

```

### Installing and Running the Tests:

Clone repository to your local machine

```
git clone git@github.com:cnkuyan/indexer-kafka.git
```

Go to project folder

```
cd indexer-kafka
```

Bring up the kafka and zookeper in your local environment
```
docker-compose up -d
```


Run maven to create jar ( Without the last parameter to run the tests at this point)

```
mvn clean install package -DskipTests
```

### Running the app:

```
java -jar target/kafka-streams-ticker-0.0.1-SNAPSHOT.jar
```

### Interacting with the App:

The following will be carried using the `curl` tool.

#### Sending data with curl
```
curl http://localhost:8080/ticks --header "Content-Type: application/json"  --request POST  --data '{ "instrument": "IBM", "timestamp": 1617473494140, "price": 130.1 }'
```
 If the Tick's timestamp is older than 1 second, it will be rejected by the endpoint with `Http NoContent` response.

#### Querying the Statistics of all instruments:
```
curl http://localhost:8080/statistics
```

#### Querying the Statistics of a Specific instrument:
```
curl http://localhost:8080/statistics/{instrument_id}
```

### Built With

* [SpringBoot](https://projects.spring.io/spring-boot/) - The web framework used for creating APIs
* [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream/) - The cloud stream framework that provides the binder for Kafka and others
* [Maven](https://maven.apache.org/) - Dependency Management
 

### Author

* **Cenk Uyan** - [github/cnkuyan](https://github.com/cnkuyan)


