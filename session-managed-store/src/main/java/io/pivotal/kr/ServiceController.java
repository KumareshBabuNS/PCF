package io.pivotal.kr;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

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
	
	@RequestMapping(value = "/get/session", method = RequestMethod.GET)
	public void getSession(Model model, HttpServletResponse res) throws IOException {
		String session = UUID.randomUUID().toString().replaceAll("-", "");
		
		sessionStore.register(session);
		res.getWriter().print(session);
	}
	
	@RequestMapping(value = "/get/value/{session}/{key}", method = RequestMethod.GET)
	public void getValue(Model model, HttpServletResponse res,
						   @PathVariable("session") String session, @PathVariable("key") String key) throws IOException {
		
		res.setCharacterEncoding("UTF-8");
		
		if (sessionStore.exists(session) == false) {
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		
		String value = sessionStore.get(session, key);
		
		if (value == null) {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		res.getWriter().print(value);
	}
	
	@RequestMapping(value = "/put/value/{session}/{key}/{value}", method = RequestMethod.GET)
	public void putValue(Model model, HttpServletResponse res,
						   @PathVariable("session") String session, 
						   @PathVariable("key") String key,
						   @PathVariable("value") String value) {
		
		if (sessionStore.exists(session) == false) {
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		
		sessionStore.put(session, key, value);
	}
	
	@RequestMapping(value = "/remove/session/{session}", method = RequestMethod.GET)
	public void removeSession(Model model, HttpServletResponse res,
						   @PathVariable("session") String session) {
		
		sessionStore.deregister(session);
	}
}
