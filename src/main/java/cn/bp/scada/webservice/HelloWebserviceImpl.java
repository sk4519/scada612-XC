package cn.bp.scada.webservice;

import org.springframework.stereotype.Component;

@Component
public class HelloWebserviceImpl implements HelloWebservice {

	@Override
	public String sayHello(String name) {
		return "hello,this is springboot and cxfwebservice "+name;
	}

}
