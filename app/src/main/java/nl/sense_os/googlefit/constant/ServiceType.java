package nl.sense_os.googlefit.constant;

/**
 * Created by panjiyudasetya on 5/15/17.
 */

public class ServiceType {
    private ServiceType() { }

    public class Awareness {
        private Awareness() { }

        @SuppressWarnings("SpellCheckingInspection")
        public static final int GEOFENCING = 1;
        public static final int ACTIVITIES = 2;
        public static final int LOCATION_UPDATES = 3;
        public static final int ALL = 4;
    }

    public class Fitness {
        private Fitness() { }

        public static final int STEPS_COUNT = 5;
        public static final int ALL = 6;
    }
}
