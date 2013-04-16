package io.bitflip.api;

import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

public class ApiRequestResult {
    
    public static final ApiRequestResult RESULT_INVALID_CALL = new ApiRequestResult(Status.Error, null, "Invalid API call");
    public static final ApiRequestResult RESULT_PARSE_ERROR = new ApiRequestResult(Status.Error, null, "Invalid JSON data; encountered an error while parsing");
    public static final ApiRequestResult RESULT_MALFORMED_REQUEST = new ApiRequestResult(Status.Error, null, "Malformed JSON request data");
    public static final ApiRequestResult RESULT_UNAVAILABLE = new ApiRequestResult(Status.Error, null, "The specified result is currently unavailable; please try again later");
    public static final ApiRequestResult RESULT_NOT_IMPLEMENTED = new ApiRequestResult(Status.Error, null, "The specified call has not been implemented yet; please try again later");

    @Getter @Setter private Status status;
    @Getter @Setter private Object result;
    @Getter @Setter private String error;
    
    public ApiRequestResult(Status status, Object result, String error) {
        this.status = status;
        this.result = result;
        this.error = error;
    }
    
    public String toJsonString() {
        JSONObject jsonObject = new JSONObject();
        
        jsonObject.put("status", status.value);
        
        if (status.equals(Status.Error)) {
            jsonObject.put("error", error);
        } else {
            jsonObject.put("result", result);
        }
        
        return jsonObject.toJSONString();
    }
    
    @Override
    public String toString() {
        return toJsonString();
    }
    
    public enum Status {
        Result("result"),
        Error("error");
        
        private String value;
        Status(String value) {
            this.value = value;
        }
        
        public static Status fromString(String value) {
            for (Status s : Status.values()) {
                if (s.value.equalsIgnoreCase(value)) {
                    return s;
                }
            }

            return null;
        }
    }
    

}
