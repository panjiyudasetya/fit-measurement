package nl.sense_os.googlefit.entities;

import static java.util.concurrent.TimeUnit.MINUTES;

@SuppressWarnings("SpellCheckingInspection")
public class GeofenceLocation {
    public final String name;
    public final double latitude;
    public final double longitude;
    public final int radius;
    private long loiteringDelay;

    public static final GeofenceLocation SENSE_ID_HQ_LOCATION = new GeofenceLocation(
            "Sense ID HQ",
            -6.874171d,
            107.590409d,
            1000,
            MINUTES.toMillis(10)
    );

    public GeofenceLocation(String name, double latitude, double longitude, int radius, long loiteringDelay) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.loiteringDelay = loiteringDelay;
    }

    public long getLoiteringDelay() {
        return this.loiteringDelay;
    }

    public void setLoiteringDelay(long delay) {
        this.loiteringDelay = delay;
    }
}
