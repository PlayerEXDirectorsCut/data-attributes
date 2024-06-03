package com.bibireden.data_attributes.json;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;

public final class EntityTypesJson {
	@Expose private HashMap<String, HashMap<String, Double>> values;
	
	private EntityTypesJson() {}
	
	public void merge(Map<String, Map<String, Double>> entityTypesIn) {
		for(String key : this.values.keySet()) {
			Map<String, Double> entityTypes = entityTypesIn.getOrDefault(key, new HashMap<String, Double>());
            entityTypes.putAll(this.values.get(key));
			entityTypesIn.put(key, entityTypes);
		}
	}
}
