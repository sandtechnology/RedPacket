package sandtechnology.jielong.util;

import com.google.gson.Gson;

public class JsonHelper<T> {
    private static final Gson gson = new Gson();

    private JsonHelper() {
    }

    public static Gson getGson() {
        return gson;
    }
}
