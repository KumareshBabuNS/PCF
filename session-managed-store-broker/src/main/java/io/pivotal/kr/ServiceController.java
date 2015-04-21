package io.pivotal.kr;

import io.pivotal.kr.dao.BindingsStore;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Handles requests for the application home page.
 */
@Controller
public class ServiceController {
	
	private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);
	
	@Autowired
	BindingsStore bindingsStore;
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);
		
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		
		String formattedDate = dateFormat.format(date);
		
		model.addAttribute("serverTime", formattedDate );
		
		return "home";
	}
	
	@RequestMapping(value = "/v2/catalog", method = RequestMethod.GET)
	public void catalog(HttpServletRequest req, HttpServletResponse res) throws JSONException, IOException {
		if (HttpUtils.isValidVersion(req) == false) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			
			return;
		}
		
		JSONObject body = new JSONObject();
		JSONArray svcs = new JSONArray();
		
		JSONObject svc = new JSONObject();
		svc.put("id", "external-service.session-managed-store");
		svc.put("name", "session-managed-store");
		svc.put("description", "Session Managed Key-Value Store");
		svc.put("bindable", true);
		svc.put("plan_updateable", false);
		svc.put("metadata", new JSONObject());
		
		JSONArray tags = new JSONArray();
		tags.put("SVC");
		svc.put("tags", tags);
		
		JSONArray plans = new JSONArray();
		
		JSONObject plan1 = new JSONObject();
		plan1.put("id", "session-managed-store-plan1");
		plan1.put("name", "plan1");
		plan1.put("description", "Plan1 for Session Managed Key-Value Store");
		plan1.put("metadata", new JSONObject());
		plan1.put("label", "Session Store Service");
		
		plans.put(0, plan1);
		
		JSONObject dashboard = new JSONObject();
		dashboard.put("id", "session-managed-store-client");
		dashboard.put("secret", "session-managed-store-secret");
		dashboard.put("redirect_uri", "http://session-managed-store.cfapps.io/");

		JSONObject meta = new JSONObject();
		meta.put("displayName", "Session Managed Store");
		meta.put("imageUrl", "https://pbs.twimg.com/profile_images/2758435924/f4f3f5b8cb8904937bc0c9c24faed860_200x200.jpeg");
		meta.put("longDescription", "Provides Session Managed Key-Value Store Service");
		meta.put("providerDisplayName", "Pivotal Software");
		meta.put("documentationUrl", "http://session-managed-store.cfapps.io/");
		meta.put("supportUrl", "http://session-managed-store.cfapps.io/");
		
		svc.put("plans", plans);
		svc.put("dashboard_client", dashboard);
		svc.put("metadata", meta);
		
		svcs.put(0, svc);
		
		body.put("services", svcs);
		
		/*
{
  "services": [{
    "id": "service-guid-here",
    "name": "mysql",
    "description": "A MySQL-compatible relational database",
    "bindable": true,
    "plans": [{
      "id": "plan1-guid-here",
      "name": "small",
      "description": "A small shared database with 100mb storage quota and 10 connections"
    },{
      "id": "plan2-guid-here",
      "name": "large",
      "description": "A large dedicated database with 10GB storage quota, 512MB of RAM, and 100 connections",
      "free": false
    }],
    "dashboard_client": {
      "id": "client-id-1",
      "secret": "secret-1",
      "redirect_uri": "https://dashboard.service.com"
    }
  }]
}
		 */
		
		res.getWriter().print(body.toString(2));
	}
	
	@RequestMapping(value = "/v2/service_instances/{instance_id}", method = RequestMethod.PUT)
	public void instantiate(@PathVariable("instance_id") String instanceId,
							  HttpServletRequest req, HttpServletResponse res) throws IOException {
		if (HttpUtils.isValidVersion(req) == false) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			
			return;
		}
		
		JSONObject obj = HttpUtils.readBodyAsJson(req);
		
		if (HttpUtils.isValidServiceId(obj, "external-service.session-managed-store") == false) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			
			return;
		}
		
		if (HttpUtils.isValidPlan(obj, new String[] {"session-managed-store-plan1", "session-managed-store-plan2"}) == false) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			
			return;
		}
		
		String serviceId = HttpUtils.getServiceId(obj);
		String planId = HttpUtils.getPlanId(obj);
		
		/*
		 * 요청된 plan을 가지고서 resource 인스턴스를 만들거나, 외부 resource에 대해 접속권한을 만들어 둠.
		 * 혹은, 이들 resource가 access에 있어 아무런 제약이 없다면, 별다른 구현 없이 진행.
		 */
		Status status = bindingsStore.getStatus(instanceId, serviceId, planId);
		
		if (status == Status.Existent) {
			res.setStatus(200);
		} else if (status == Status.NonExistent) {
			bindingsStore.register(instanceId, serviceId, planId);
			res.setStatus(201);
		} else {
			res.setStatus(409);
		}
		
		JSONObject body = new JSONObject();
		body.put("dashboard_url", "http://session-managed-store.cfapps.io/");
		
		if (status == Status.Conflict) {
			res.getWriter().print("{}");
		} else {
			res.getWriter().print(body.toString(2));
		}
	}
	
	@RequestMapping(value = "/v2/service_instances/{instance_id}/service_bindings/{binding_id}", method = RequestMethod.PUT)
	public void instantiate(@PathVariable("instance_id") String instanceId,
							  @PathVariable("binding_id") String bindingId,
							  HttpServletRequest req, HttpServletResponse res) throws IOException {
		if (HttpUtils.isValidVersion(req) == false) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			
			return;
		}
		
		JSONObject obj = HttpUtils.readBodyAsJson(req);
		
		if (HttpUtils.isValidServiceId(obj, "external-service.session-managed-store") == false) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			
			return;
		}
		
		if (HttpUtils.isValidPlan(obj, new String[] {"session-managed-store-plan1", "session-managed-store-plan2"}) == false) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			
			return;
		}
		

		String serviceId = HttpUtils.getServiceId(obj);
		String planId = HttpUtils.getPlanId(obj);
		
		Status status = bindingsStore.getBindingStatus(instanceId, serviceId, planId, bindingId);
		
		if (status == Status.Existent) {
			res.setStatus(200);
		} else if (status == Status.NonExistent) { 
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet getSessionRequest = new HttpGet("http://session-managed-store.cfapps.io/get/session");
			HttpResponse response = httpClient.execute(getSessionRequest);
			
			String session = EntityUtils.toString(response.getEntity());
			
			JSONObject body = new JSONObject();
			JSONObject cred = new JSONObject();
			
			cred.put("uri", "http://session-managed-store.cfapps.io");
			cred.put("session", session);
			
			body.put("credentials", cred);
			
			bindingsStore.storeCredential(instanceId, bindingId, body.toString(2));
	
			res.setStatus(201);
		} else {
			res.setStatus(409);
		}
		
		if (status == Status.Conflict) {
			res.getWriter().print("{}");
		} else {
			res.getWriter().print(bindingsStore.getCredential(instanceId, bindingId));
		}
	}
	
	@RequestMapping(value = "/v2/service_instances/{instance_id}/service_bindings/{binding_id}", method = RequestMethod.DELETE)
	public void deleteBinding(@PathVariable("instance_id") String instanceId,
							  @PathVariable("binding_id") String bindingId,
							  HttpServletRequest req, HttpServletResponse res) throws IOException {
		
		String serviceId = req.getParameter("service_id");
		String planId = req.getParameter("plan_id");
		
		if (HttpUtils.isValidVersion(req) == false) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			
			return;
		}
		
		if ("external-service.session-managed-store".equals(serviceId) == false) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			
			return;
		}
		
		boolean isValidPlan = false;
		for (String plan : new String[] {"session-managed-store-plan1", "session-managed-store-plan2"}) {
			if (plan.equals(planId)) {
				isValidPlan = true;
			}
		}
		
		if (isValidPlan == false) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			
			return;
		}
		
		Status status = bindingsStore.getBindingStatus(instanceId, serviceId, planId, bindingId);
		
		if (status == Status.Existent) {
			String storedCredential = bindingsStore.getCredential(instanceId, bindingId);
			JSONObject credential = (JSONObject)new JSONObject(new JSONTokener(storedCredential)).get("credentials"); 
			
			String session = credential.getString("session");
			
			if (session != null) {
				HttpClient httpClient = HttpClientBuilder.create().build();
				HttpGet getSessionRequest = new HttpGet("http://session-managed-store.cfapps.io/remove/session/" + session);
			
				httpClient.execute(getSessionRequest);
			}
			
			if (bindingsStore.unbind(instanceId, bindingId)) {
				res.setStatus(200);				
			} else {
				res.setStatus(410);
			}
		} else {
			res.setStatus(410);
		}
		
		res.getWriter().print("{}");
	}
	
	@RequestMapping(value = "/v2/service_instances/{instance_id}", method = RequestMethod.DELETE)
	public void deleteInstance(@PathVariable("instance_id") String instanceId,
							  HttpServletRequest req, HttpServletResponse res) throws IOException {
		
		String serviceId = req.getParameter("service_id");
		String planId = req.getParameter("plan_id");
		
		if (HttpUtils.isValidVersion(req) == false) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			
			return;
		}
		
		if ("external-service.session-managed-store".equals(serviceId) == false) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			
			return;
		}
		
		boolean isValidPlan = false;
		for (String plan : new String[] {"session-managed-store-plan1", "session-managed-store-plan2"}) {
			if (plan.equals(planId)) {
				isValidPlan = true;
			}
		}
		
		if (isValidPlan == false) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			
			return;
		}
		
		Status status = bindingsStore.getStatus(instanceId, serviceId, planId);
		
		if (status == Status.Existent) {
			if (bindingsStore.deprovision(instanceId)) {
				res.setStatus(200);				
			} else {
				res.setStatus(410);
			}
		} else {
			res.setStatus(410);
		}
		
		res.getWriter().print("{}");
	}
}
