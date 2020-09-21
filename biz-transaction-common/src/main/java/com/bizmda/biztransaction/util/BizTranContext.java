package com.bizmda.biztransaction.util;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class BizTranContext {
    private Map<String,Object> contextMap;
}
