package io.pivotal.kr;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

@Service
public class SessionStoreService {
	private Map<String, Map<String, String>> store;
	
	@PostConstruct
	private void initStore() {
		store = new HashMap<String, Map<String, String>>();
	}
	
	public void register(String session) {
		if (store.containsKey(session) == false) {
			store.put(session, new HashMap<String, String>());
		}
	}
	
	public void deregister(String session) {
		store.remove(session);
	}
	
	public boolean exists(String session) {
		return store.containsKey(session);
	}
	
	public String get(String session, String key) {
		return store.get(session).get(key);
	}
	
	public void put(String session, String key, String value) {
		synchronized (store.get(session)) {
			store.get(session).put(key, value);
		}
	}
}
