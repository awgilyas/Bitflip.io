package io.bitflip.api;

import io.bitflip.engine.Order;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Ticker extends ApiModule {
    
    public Ticker(ApiServer server) {
        super(server);
    }

    @Override
    public ApiRequestResult call(List<Object> args) {
        if (args.size() == 1) {
            String action = ((String)args.get(0)).toLowerCase();
            
            switch (action) {
                case "last": return getLast();
                case "avg": return getAverage();
                case "high": return getHigh();
                case "low": return getLow();
            }
        } else {
            List<Double> values = new LinkedList<>();
            Collections.addAll(values, getLastValue(), 0D, 0D, 0D);
            
            return new ApiRequestResult(ApiRequestResult.Status.Result, values, null);
        }
        
        return ApiRequestResult.RESULT_INVALID_CALL;
    }
    
    public ApiRequestResult getLast() {
        Double lastValue = getLastValue();
        
        if (lastValue != null) {
            return new ApiRequestResult(ApiRequestResult.Status.Result, lastValue, null);
        } else {
            return ApiRequestResult.RESULT_UNAVAILABLE;
        }
    }
    
    private Double getLastValue() {
        Order lastOrder = super.getLastOrder();
        return (lastOrder != null ? lastOrder.getPrice() : null);
    }
    
    public ApiRequestResult getAverage() {
        return ApiRequestResult.RESULT_NOT_IMPLEMENTED;
    }
    
    public ApiRequestResult getHigh() {
        return ApiRequestResult.RESULT_NOT_IMPLEMENTED;
    }
    
    public ApiRequestResult getLow() {
        return ApiRequestResult.RESULT_NOT_IMPLEMENTED;
    }

}
