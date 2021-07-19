package cn.bp.scada.webservice;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface HelloWebservice {
	@WebMethod
	public String sayHello(String name);
}
