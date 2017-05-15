package aop;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.akash.aop.Test;

public class Aspect {

  public static void main(String[] args) {
    @SuppressWarnings("resource")
    ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-config.xml");
    Test test = (Test) applicationContext.getBean("Test");
    test.get();
  }

}
