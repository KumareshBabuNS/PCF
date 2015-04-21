package io.pivotal.kr.dao;

import io.pivotal.kr.Status;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

@Service
public class BindingsStore {
	private Map<String, Map<String, String>> store;
	
	@PostConstruct
	private void initializeStore() {
		store = new HashMap<String, Map<String, String>>();
	}
	
	public Status getStatus(String instanceId, String serviceId, String planId) {
		if (store.containsKey(instanceId) == false) {
			return Status.NonExistent;
		}
		
		String storedServiceId = store.get(instanceId).get("ServiceId");
		String storedPlanId = store.get(instanceId).get("PlanId");
		
		if (serviceId.equals(storedServiceId) && planId.equals(storedPlanId)) {
			return Status.Existent;
		}
		
		return Status.Conflict;
	}
	
	public Status getBindingStatus(String instanceId, String serviceId, String planId, String bindingId) {
		if (store.containsKey(instanceId) == false) {
			return Status.Conflict;
		}
		
		String storedServiceId = store.get(instanceId).get("ServiceId");
		String storedPlanId = store.get(instanceId).get("PlanId");
		String credentials = store.get(instanceId).get("bindingId:"+bindingId);
		
		if (serviceId.equals(storedServiceId) && planId.equals(storedPlanId)) {
			if (credentials == null) {
				return Status.NonExistent;
			} else {
				return Status.Existent;
			}
		}
		
		return Status.Conflict;
	}
	
	public void register(String instanceId, String serviceId, String planId) {
		if (store.containsKey(instanceId) == false) {
			synchronized (store) {
				store.put(instanceId, new HashMap<String, String>());
			}
		}
		
		synchronized (store.get(instanceId)) {
			store.get(instanceId).put("ServiceId", serviceId);
			store.get(instanceId).put("PlanId", planId);
		}
	}
	
	public boolean unbind(String instanceId, String bindingId) {
		if (store.containsKey(instanceId) && store.get(instanceId).containsKey("bindingId:"+bindingId)) {
			synchronized (store.get(instanceId)) {
				store.get(instanceId).remove("bindingId:"+bindingId);
			}
			
			return true;
		}
		
		return false;
	}
	
	public boolean deprovision(String instanceId) {
		if (store.containsKey(instanceId)) {
			synchronized (store) {
				store.remove(instanceId);
			}
			
			return true;
		}
		
		return false;
	}
	
	public void storeCredential(String instanceId, String bindingId, String credential) {
		synchronized (store.get(instanceId)) {
			store.get(instanceId).put("bindingId:"+bindingId, credential);
		}
	}
	
	public String getCredential(String instanceId, String bindingId) {
		return store.get(instanceId).get("bindingId:"+bindingId);
	}
}
