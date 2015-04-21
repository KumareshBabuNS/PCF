package io.pivotal.kr;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;
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
	
	@Autowired
	private SessionStoreService sessionStore;
	
	private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);
	
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
	
	@RequestMapping(value = "/get/{key}", method = RequestMethod.GET)
	public void getValue(Model model, HttpServletResponse res,
						   @PathVariable("key") String key) throws ClientProtocolException, IOException {
		
		res.getWriter().print(sessionStore.get(key));
	}
	
	@RequestMapping(value = "/put/{key}/{value}", method = RequestMethod.GET)
	public void putValue(Model model, HttpServletResponse res,
						   @PathVariable("key") String key,
						   @PathVariable("value") String value) throws ClientProtocolException, IOException {
		
		sessionStore.put(key, value);
	}
}
