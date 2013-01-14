package madsdf.shimmer.event;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import java.util.HashMap;

// This is really just a big, fat, ugly singleton so everybody can use the
// same eventbuses
public class Globals {
    // Each connected shimmer will have its own eventbus
    public static final HashMap<String, EventBus> eventBuses = Maps.newHashMap();
    
    public static EventBus getBusForShimmer(String btid) {
        EventBus bus = eventBuses.get(btid);
        if (bus == null) {
            bus = new EventBus();
            eventBuses.put(btid, bus);
        }
        return bus;
    }
    
    // Global eventbus
    //public static final EventBus eventBus = new EventBus();
}
