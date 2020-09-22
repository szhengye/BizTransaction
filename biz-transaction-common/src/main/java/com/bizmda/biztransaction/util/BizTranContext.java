package com.bizmda.biztransaction.util;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class BizTranContext {
    private Map<String,Object> contextMap = new HashMap<String, Object>();

    public Object getAttribute(String name) {
        if (this.contextMap == null) {
            this.contextMap = new HashMap<String,Object>();
        }
        return this.contextMap.get(name);
    }

    public void setAttribute(String name,Object value) {
        if (this.contextMap == null) {
            this.contextMap = new HashMap<String,Object>();
        }
        this.contextMap.put(name,value);
    }
}
