/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kafka.streams.product.tracker;

import java.time.Duration;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.streams.product.tracker.model.Tick;
import kafka.streams.product.tracker.model.TickStats;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.TimestampExtractor;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class KafkaStreamsTickerApp {

	public static final long TUMBLING_WINDOWS_SECS = 60;
	public static final long MSECS_PER_SECOND = 1000L;

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamsTickerApp.class, args);
	}


	/**
	 *
	 * @return  BiFunction that takes a stream of Tick values, and outputs a stream of TickStats values
	 */
	@Bean
	public Function<KStream<Object, Tick>,KStream<String,TickStats>> processticks() {

		ObjectMapper mapper = new ObjectMapper();
		Serde<Tick> tickSerde = new JsonSerde<>( Tick.class, mapper );
		Serde<TickStats> tickStatusSerde = new JsonSerde<>( TickStats.class, mapper );

		return input -> input
				.map((key, value) -> new KeyValue<>(value, value))
				.groupBy( (k,v) -> v.getInstrument(), Grouped.with(Serdes.String(),tickSerde))
				.windowedBy(TimeWindows.of(Duration.ofSeconds(TUMBLING_WINDOWS_SECS)).
						               advanceBy(Duration.ofSeconds(TUMBLING_WINDOWS_SECS)).
						grace(Duration.ofSeconds(0)))
				.aggregate(() -> new TickStats("", Double.MAX_VALUE, Double.MIN_VALUE,0),
						(key,value,aggregate) -> new TickStats( key,
								Math.min(value.getPrice(), ((TickStats)aggregate).getMin()),
								Math.max(value.getPrice(), ((TickStats)aggregate).getMax()),
								((TickStats)aggregate).getCount() + 1),
						Materialized.<String, TickStats, WindowStore<Bytes, byte[]>>as("tick-stats")
								.withKeySerde(Serdes.String()).
								withValueSerde(tickStatusSerde))
				.toStream()
				.map((key, value) -> new KeyValue<>(value.getInstrument(),value ));


	}



	@Bean(name="TickTimestampExtractor")
	public TimestampExtractor timestampExtractor() {
		return new TickTimestampExtractor();
	}



}//SpringBootApplication
