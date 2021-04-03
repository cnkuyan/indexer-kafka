package kafka.streams.product.tracker;

import kafka.streams.product.tracker.model.Tick;
import kafka.streams.product.tracker.model.TickStats;
import kafka.streams.product.tracker.service.TickService;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest
public class TickControllerTest {

    private final static String STATS_URL = "/statistics/";
    private Map<String,TickStats> tickStatsMap = buildTickStatsMap();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TickService tickService;




    @Test
    void getSingleTickStats() throws Exception {

        // when
        when(tickService.stats("ABC")).thenReturn(tickStatsMap.get("ABC"));


        String resultJson = "{\"instrument\":\"ABC\",\"min\":93.4,\"max\":110.3,\"count\":46}";

        mockMvc.perform(get(STATS_URL+"ABC"))
                .andExpect(status().isOk())
                .andExpect(content().json(resultJson));

    }

    @Test
    void getAllStats() throws Exception {

        // when
        when(tickService.stats()).thenReturn(tickStatsMap);


        String resultJson = "[" +
                "{\"instrument\":\"ABC\",\"min\":93.4,\"max\":110.3,\"count\":46}" +
                ","+
                "{\"instrument\":\"XYZ\",\"min\":10.0,\"max\":11.2,\"count\":34}]";

        mockMvc.perform(get(STATS_URL))
                .andExpect(status().isOk())
                .andExpect(content().json(resultJson));

    }

    private Map<String, TickStats> buildTickStatsMap() {

        TickStats tick1 = new TickStats("XYZ",10.0,11.2,34);
        TickStats tick2 = new TickStats("ABC",93.4,110.3,46);

        Map<String,TickStats> map = new HashMap<>(2);
        map.put("ABC",tick2);
        map.put("XYZ",tick1);

        return map;
    }
}

