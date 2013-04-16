package io.bitflip.api;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * ApiRequest
 * Provides decoding for JSON API requests to generic API call instances
 */
public class ApiRequest {
    
    private static JSONParser jsonParser = new JSONParser();
    private static ContainerFactory containerFactory = new ContainerFactory(){
        public List creatArrayContainer() {  return new LinkedList(); }
        public Map createObjectContainer() { return new LinkedHashMap(); }
    };
    
    private ApiServer server;
    private String name;
    private List<Object> args;
    
    protected ApiRequest(ApiServer server) {
        this.server = server;
    }
    
    public ApiRequestResult execute() {
        switch (name.toLowerCase()) {
            case "ticker":
                return new Ticker(server).call(args);
            default:
                return ApiRequestResult.RESULT_INVALID_CALL;
        }
    }
    
    public static ApiRequest fromJson(ApiServer server, String jsonData) throws ParseException, ClassCastException {
        ApiRequest request = new ApiRequest(server);
        request.args = new LinkedList<>();
        
        Map jsonMap = (Map)jsonParser.parse(jsonData, containerFactory);
        Iterator jsonIterator = jsonMap.entrySet().iterator();
        
        Map.Entry entry;
        while (jsonIterator.hasNext()) {
            entry = (Map.Entry)jsonIterator.next();
            
            if (entry.getKey() instanceof String) {
                String key = (String)entry.getKey();
                
                if (key.equalsIgnoreCase("call")) {
                    request.name = (String)entry.getValue();
                } else if (key.equalsIgnoreCase("args")) {
                    request.args = (LinkedList)entry.getValue();
                }
            }
        }
        
        return request;
    }
    
}
