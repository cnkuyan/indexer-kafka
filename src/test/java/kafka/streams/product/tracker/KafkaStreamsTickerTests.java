package kafka.streams.product.tracker;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.streams.product.tracker.model.Tick;
import kafka.streams.product.tracker.model.TickStats;
import kafka.streams.product.tracker.model.TickStatsList;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KafkaStreamsTickerTests {

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafkaRule = new EmbeddedKafkaRule(1, true, "foobar");

	private static EmbeddedKafkaBroker embeddedKafka = embeddedKafkaRule.getEmbeddedKafka();

	@Autowired
	StreamsBuilderFactoryBean streamsBuilderFactoryBean;

	//@Autowired
	//private MockMvc mockMvc;

	@LocalServerPort
	int randomServerPort;


	private String jsonString(Object obj) throws JsonProcessingException {

		return  new ObjectMapper().writeValueAsString(obj);
	}

	@Before
	public void before() {
		streamsBuilderFactoryBean.setCloseTimeout(0);
	}

	@BeforeClass
	public static void setUp() {
		System.setProperty("spring.cloud.stream.kafka.streams.binder.brokers", embeddedKafka.getBrokersAsString());
	}

	@AfterClass
	public static void tearDown() {
		System.clearProperty("spring.cloud.stream.kafka.streams.binder.brokers");
	}

	@Test
	public void testKafkaTopic() throws Exception {
		Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafka);
		ObjectMapper mapper = new ObjectMapper();
		Serde<Tick> tickSerde = new JsonSerde<>(Tick.class, mapper);

		senderProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		senderProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, tickSerde.serializer().getClass());

		DefaultKafkaProducerFactory<String, Tick> pf = new DefaultKafkaProducerFactory<>(senderProps);
		try {


			KafkaTemplate<String, Tick> template = new KafkaTemplate<>(pf, true);
			template.setDefaultTopic("ticks");

			ZonedDateTime now_minus_5secs = ZonedDateTime.now().minusSeconds(5);
			Tick aTick = new Tick("XYZ",50.0, now_minus_5secs.toEpochSecond() * 1000);

			template.sendDefault("", aTick);

 			Thread.sleep(1000);
			RestTemplate restTemplate = new RestTemplate();
			String xyz_resource_url
					= "http://localhost:" + randomServerPort + "/statistics/XYZ";
			ResponseEntity<TickStats> response
					= restTemplate.getForEntity(xyz_resource_url, TickStats.class);

			assertThat(response.getBody().getCount() == 1);
			assertThat(response.getBody().getInstrument() == "XYZ");


			aTick = new Tick("XYZ",30.0, now_minus_5secs.toEpochSecond() * 1000);


			template.sendDefault("", aTick);

			Thread.sleep(1000);
			response = restTemplate.getForEntity(xyz_resource_url, TickStats.class);

			assertThat(response.getBody().getInstrument() == "XYZ");
			assertThat(response.getBody().getCount() == 2);
			assertThat(response.getBody().getMin() == 30.0);
			assertThat(response.getBody().getMax() == 50.0);


			String all_resource_url
					= "http://localhost:" + randomServerPort + "/statistics";
			ResponseEntity<TickStatsList> tickStats_list
					= restTemplate.getForEntity(all_resource_url,TickStatsList.class);
			assertEquals(1, tickStats_list.getBody().getTickStatsList().size());

			TickStats ts = tickStats_list.getBody().getTickStatsList().get(0);
			assertThat(ts.getInstrument() == "XYZ");
			assertThat(ts.getCount() == 2);
			assertThat(ts.getMin() == 30.0);
			assertThat(ts.getMax() == 50.0);



		}
		finally {
			pf.destroy();
		}
	} //Test





}
