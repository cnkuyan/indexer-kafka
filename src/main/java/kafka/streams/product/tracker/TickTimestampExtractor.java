package kafka.streams.product.tracker;

import kafka.streams.product.tracker.model.Tick;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TickTimestampExtractor implements TimestampExtractor {

    Logger log = LoggerFactory.getLogger(TickTimestampExtractor.class);

    @Override
    public long extract(ConsumerRecord<Object, Object> consumerRecord, long l) {


        long timestamp = 0;

        if (consumerRecord.value() != null )
        {
            String instrument  = ( (Tick) consumerRecord.value()).getInstrument();
            timestamp = ( (Tick) consumerRecord.value()).getTimestamp();
            log.debug(String.format("Extracted timestamp for [%s] = %d", instrument, timestamp));
        }

        if ( timestamp < 0 ) {
            log.debug(String.format("Failed to extract timestamp"));
            timestamp =  System.currentTimeMillis();
        }

        return timestamp;
    }

}
