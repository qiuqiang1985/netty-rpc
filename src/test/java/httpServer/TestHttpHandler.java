package httpServer;

import netty.rpc.http.server.HttpChannelServerHandler;
import netty.rpc.http.server.HttpServer;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;

public class TestHttpHandler extends HttpChannelServerHandler{

	public TestHttpHandler(){
	}
	@Override
	protected byte[] processor(HttpRequest httpRequest) {
		
		if(httpRequest.getMethod() == HttpMethod.GET){
			QueryStringDecoder decoder = new QueryStringDecoder(httpRequest.getUri());
//			 QueryStringDecoder decoder = new QueryStringDecoder("/hello?recipient=world");
//			 assert decoder.getPath().equals("/hello");
//			 assert decoder.getParameters().get("recipient").equals("world");
			
			StringBuilder sbBuilder = new StringBuilder();
			for (int i = 0; i < 1000000; i++) {
				sbBuilder.append('a');
				if(i%1000==0){
					sbBuilder.append("<br>");
				}
			}
			
			return sbBuilder.toString().getBytes();
		}else if(httpRequest.getMethod() == HttpMethod.POST){
			String string = httpRequest.getContent().toString(CharsetUtil.UTF_8);
			System.out.println("post==="+string);
			String tempString = "{"+string.replace('=', ':').replace('&', ',')+"}";
			return tempString.getBytes();
		}else{
			//others
		}
		
		return null;
	}
	
	public static void main(String[] args){
		try{
			HttpChannelServerHandler serverHandler = new TestHttpHandler();
			HttpServer server=new HttpServer(8080, serverHandler);
			server.startUp();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
