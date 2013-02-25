package server;

import netty.rpc.coder.Transport;
import netty.rpc.server.AbstractServerHandler;
import netty.rpc.server.Server;
import netty.rpc.server.ServerSender;

import org.jboss.netty.channel.Channel;




/**
 * 服务端处理类
 * @author steven.qiu
 *
 */
public class TestServerHandler extends AbstractServerHandler{

	@Override
	protected void processor(Channel channel, Transport transport) {
		
		try{
			
			byte[] ret = transport.getValue(); //处理之后的返回结果
			System.out.println("8080端口接受到的数据："+new String(ret));
			
			ServerSender sender = new ServerSender(channel, transport);
			sender.send(ret);
			//System.out.println("向8081端口发送数据完成");
		}catch (Exception e) {
			e.printStackTrace();
			throw new IllegalAccessError(e.getMessage());
		}		
	}
	
	public static void main(String[] args){
		
		try{
			
			Server server=new Server(8080, new TestServerHandler());
			
			server.startUp();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
