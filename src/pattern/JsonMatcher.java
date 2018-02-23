package pattern;

import util.SerializationUtils;

public interface JsonMatcher {
    default String getJson() {
        return SerializationUtils.getJson(this);
    }
}
