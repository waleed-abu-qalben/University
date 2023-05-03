package org.example.customGson;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomGson {

    private static Gson gson;

    private static DateFormat timeFormat = new SimpleDateFormat("HH:mm");

    private CustomGson() {
    }

    public static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .registerTypeAdapter(Time.class, new JsonDeserializer<Time>() {
                        public Time deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                                throws JsonParseException {
                            try {
                                Date date = timeFormat.parse(json.getAsString());
                                return new Time(date.getTime());
                            } catch (ParseException e) {
                                throw new JsonParseException(e);
                            }
                        }
                    }).create();
        }
        return gson;
    }
}
