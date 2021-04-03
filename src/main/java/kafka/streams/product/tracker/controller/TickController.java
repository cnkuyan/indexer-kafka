package kafka.streams.product.tracker.controller;

import kafka.streams.product.tracker.TickProducer;
import kafka.streams.product.tracker.model.Tick;
import kafka.streams.product.tracker.model.TickStats;
import kafka.streams.product.tracker.service.TickService;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.*;

@RestController
public class TickController {

    Logger log = LoggerFactory.getLogger(TickController.class);

    @Autowired
    private final TickService tickService = null;


    @PostMapping(value = "/ticks",consumes = {"application/json"},produces = {"application/json"})
    public ResponseEntity registerTick(@Valid @RequestBody Tick aTick) {
        ResponseEntity ret = tickService.sendDataToKafka(aTick);
        return ret;
    }


    @GetMapping("/statistics")
    public ResponseEntity<List<TickStats>> stats() {
        Map<String, TickStats> retmap = tickService.stats();
        List<TickStats>  values = null;
        if (retmap != null) {
            values = new ArrayList<>(retmap.values());
        }
        return ResponseEntity.of(Optional.ofNullable(values));
    }


    @GetMapping("/statistics/{instrument}")
    public ResponseEntity<TickStats> stats_for_tick(@PathVariable String instrument) {
        TickStats ret = tickService.stats(instrument);
        return ResponseEntity.of(Optional.ofNullable(ret));
    }

}
