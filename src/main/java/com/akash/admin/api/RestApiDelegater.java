package com.akash.admin.api;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/admin/api")
public class RestApiDelegater {
	@RequestMapping(value = "/helloworld", method = RequestMethod.GET)
	public void getHello(Model model) {
		RestTemplate restTemplate = new RestTemplate();
	}
}
