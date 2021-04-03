package kafka.streams.product.tracker.service;

import kafka.streams.product.tracker.TickProducer;
import kafka.streams.product.tracker.controller.TickController;
import kafka.streams.product.tracker.model.Tick;
import kafka.streams.product.tracker.model.TickStats;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static kafka.streams.product.tracker.KafkaStreamsTickerApp.MSECS_PER_SECOND;
import static kafka.streams.product.tracker.KafkaStreamsTickerApp.TUMBLING_WINDOWS_SECS;

@Service
public class TickService {

    Logger log = LoggerFactory.getLogger(TickController.class);

    private  Map<String, TickStats> statsMap = null;

    @Autowired
    private InteractiveQueryService interactiveQueryService;

    @Autowired
    TickProducer myKafkaProducer;

    public static boolean is_within_window(Instant tickTs, Temporal now, long windowMillis) {

        Duration dur = Duration.between(tickTs, now);
        boolean ret = (dur.getSeconds() * MSECS_PER_SECOND > windowMillis ? false : true);

        return ret;

    }

    public TickService() {
        statsMap = new ConcurrentHashMap<>();
    }

    public ResponseEntity sendDataToKafka(Tick aTick) {

        Temporal now = ZonedDateTime.now() ;

        /*
        Instant tick_ts = Instant.ofEpochMilli(aTick.getTimestamp());
        Duration dur = Duration.between(tick_ts, now);

        */

        Instant tickts = Instant.ofEpochMilli(aTick.getTimestamp());
        if(! is_within_window(tickts,ZonedDateTime.now(), TUMBLING_WINDOWS_SECS * MSECS_PER_SECOND)) {
            log.error("HttpStatus.NO_CONTENT");
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        } else {
            myKafkaProducer.sendDataToKafka(aTick);
            return new ResponseEntity<>(null, HttpStatus.OK);
        }

    }


    private Map<String, TickStats> refreshMap() {

        ReadOnlyWindowStore<String, TickStats> store = interactiveQueryService.getQueryableStore("tick-stats", QueryableStoreTypes.windowStore());


        KeyValueIterator<Windowed<String>, TickStats> iterator = store.all();
        while (iterator.hasNext()) {

            KeyValue<Windowed<String>, TickStats> next = iterator.next();
            statsMap.put( next.key.key(), next.value);
        }

        return statsMap;

    }

    public Map<String, TickStats> stats() {
        Map<String, TickStats> ret = refreshMap();
        return ret;
    }

    public TickStats stats(String instrument) {
        TickStats ret = refreshMap().get(instrument);
        return ret;
    }
}
