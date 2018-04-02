package com.akash;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HelloWorldController {

	@RequestMapping(value = "/helloworld", method = RequestMethod.GET)
	public String getHello(Model model) {
		System.out.println("hi this is the Controller");
		return "hello";

	}
}
