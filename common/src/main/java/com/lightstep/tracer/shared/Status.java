package com.lightstep.tracer.shared;

import com.lightstep.tracer.grpc.KeyValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal class used primarily for unit testing and debugging. This is not
 * part of the OpenTracing API and is not a supported API.
 */
public class Status {
    private final Map<String, String> tags;
    private final long spansDropped;

    Status(List<KeyValue> attrs, long spansDropped) {
        tags = new HashMap<>(attrs.size());
        for (KeyValue pair : attrs) {
            tags.put(pair.getKey(), pair.getStringValue());
        }
        this.spansDropped = spansDropped;
    }

    @SuppressWarnings("unused")
    public long getSpansDropped() {
        return spansDropped;
    }

    @SuppressWarnings("unused")
    public boolean hasTag(String key) {
        return tags.containsKey(key);
    }

    @SuppressWarnings("unused")
    public String getTag(String key) {
        return tags.get(key);
    }
}
