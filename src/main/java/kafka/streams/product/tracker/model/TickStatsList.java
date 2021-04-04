package kafka.streams.product.tracker.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TickStatsList {

    @JsonProperty
    private List<TickStats> tickStatsList = null;

    public TickStatsList() {
    }

    public TickStatsList(List<TickStats> tickStatsList) {
        this.tickStatsList = tickStatsList;
    }

    public List<TickStats> getTickStatsList() {
        return tickStatsList;
    }

    public void setTickStatsList(List<TickStats> tickStatsList) {
        this.tickStatsList = tickStatsList;
    }

}
