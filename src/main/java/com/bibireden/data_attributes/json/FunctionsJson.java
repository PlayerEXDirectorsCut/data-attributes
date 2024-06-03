package com.bibireden.data_attributes.json;

import java.util.HashMap;
import java.util.Map;

import com.bibireden.data_attributes.data.AttributeFunction;
import com.google.gson.annotations.Expose;

public final class FunctionsJson {
	@Expose private HashMap<String, HashMap<String, AttributeFunction>> values;
	
	private FunctionsJson() {}
	
	public void merge(Map<String, Map<String, AttributeFunction>> functionsIn) {
		for(String key : this.values.keySet()) {
			Map<String, AttributeFunction> functions = functionsIn.getOrDefault(key, new HashMap<>());
            functions.putAll(this.values.get(key));
			functionsIn.put(key, functions);
		}
	}
}
