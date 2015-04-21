package io.pivotal.kr;

import java.io.IOException;
import java.net.URLEncoder;

import javax.annotation.PostConstruct;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.stereotype.Service;

@Service
public class SessionStoreService {
	private HttpClient httpClient;
	private String svcUrl;
	private String session;
	
	@PostConstruct
	private void initStore() {
		httpClient = HttpClientBuilder.create().build();
		
		String vcapServices = System.getenv("VCAP_SERVICES");
		JSONObject vcapJsonObj = new JSONObject(new JSONTokener(vcapServices));
		
		svcUrl = vcapJsonObj.getJSONArray("session-managed-store").getJSONObject(0).getJSONObject("credentials").getString("uri");
		session = vcapJsonObj.getJSONArray("session-managed-store").getJSONObject(0).getJSONObject("credentials").getString("session");
	}
	
	public String get(String key) throws ClientProtocolException, IOException {
		HttpGet request = new HttpGet(svcUrl + "/get/value/" + session + "/" + key);
		HttpResponse response = httpClient.execute(request);
		
		return EntityUtils.toString(response.getEntity());
	}
	
	public void put(String key, String value) throws ClientProtocolException, IOException {
		HttpGet request = new HttpGet(svcUrl + "/put/value/" + session + "/" + key + "/" + URLEncoder.encode(value, "UTF-8"));
		httpClient.execute(request);
	}
}
