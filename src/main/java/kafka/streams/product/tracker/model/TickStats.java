package kafka.streams.product.tracker.model;


import com.fasterxml.jackson.annotation.JsonProperty;

public class TickStats {

    @JsonProperty
    private String instrument;
    @JsonProperty
    private double min;
    @JsonProperty
    private double max;
    @JsonProperty
    private long count;

    public TickStats() {
        super();
    }

    public TickStats(String instrument, double min, double max, long count) {
        this.instrument = instrument;
        this.min = min;
        this.max = max;
        this.count = count;
   }


    public String getInstrument() {       return instrument;     }
    public void setInstrument(String instrument) {         this.instrument = instrument;     }

    public double getMin() {        return min;     }

    public double getMax() {         return max;     }

    public long getCount() {
        return count;
    }
    public void setCount(long count) {
        this.count = count;
    }

}
