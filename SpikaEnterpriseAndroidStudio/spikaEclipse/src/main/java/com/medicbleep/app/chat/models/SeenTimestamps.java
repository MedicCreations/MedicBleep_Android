package com.medicbleep.app.chat.models;

import com.medicbleep.app.chat.extendables.BaseModel;

import java.util.List;

/**
 * Created by mislav on 20/05/15.
 */
public class SeenTimestamps extends BaseModel {

    public List<SeenTimestamp> result;

    public SeenTimestamps() {

    }

    @Override
    public String toString() {
        return "SeenTimestamps{" +
                "result=" + result +
                '}';
    }

    public static class SeenTimestamp {
        public long message_id;
        public int seen_timestamp;

        public SeenTimestamp () {

        }

        @Override
        public String toString() {
            return "SeenTimestamp{" +
                    "message_id='" + message_id + '\'' +
                    ", seen_timestamp=" + seen_timestamp +
                    '}';
        }
    }
}
