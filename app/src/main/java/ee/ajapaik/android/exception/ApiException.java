package ee.ajapaik.android.exception;

import com.google.gson.JsonElement;

public class ApiException extends Exception {

    private final JsonElement response;

    public ApiException(JsonElement response) {
        super();
        this.response = response;
    }

    @Override
    public String toString() {
        return "API request failed! Response: " + response;
    }
}
