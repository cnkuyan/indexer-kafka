package kafka.streams.product.tracker.model;

public class Tick {
    private String instrument;

    private double price;

    private long timestamp;

    public Tick() {
        super();
    }


    public String getInstrument() {
        return instrument;
    }

    public double getPrice() {
        return price;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Tick(String instrument, double price, long timestamp) {
        this.instrument = instrument;
        this.price = price;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Tick{" +
                "instrument='" + instrument + '\'' +
                ", price=" + price +
                ", timestamp=" + timestamp +
                '}';
    }
}
