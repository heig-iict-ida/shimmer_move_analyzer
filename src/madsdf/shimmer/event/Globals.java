package madsdf.shimmer.event;

import com.google.common.eventbus.EventBus;

// This is really just a big, fat, ugly singleton so everybody can use the
// same eventbus
public class Globals {
    // Global eventbus
    public static final EventBus eventBus = new EventBus();
}
