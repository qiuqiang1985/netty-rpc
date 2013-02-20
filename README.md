netty-rpc
=========

rpc based on netty

Server端只要实现AbstractServerHandler类的processor方法。该方法用来对消息的具体处理。
Client端只要提供了三个方法与服务端通讯：（1）阻塞式的消息处理；（2）非阻塞式的消息处理；（3）只发送消息，没有消息返回的情况。

详细处理形式，Server端：
public class TestServerHandler extends AbstractServerHandler{

  @Override
	protected void processor(Channel channel, Transport transport) {
		
		try{
			
			byte[] ret = transport.getValue(); //处理之后的返回结果
			//System.out.println("8080端口接受到的数据："+new String(ret));
			
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

Client端:
public class TestClient {
  
	public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException, IOException{
		
		final ClientProxy proxy = ClientProxy.createClientProxy("localhost", 8080);
		
		
		long startTime = System.currentTimeMillis();
		
				for(int i=0;i<10000;i++){
					try{
//						ClientSender sender = proxy.NoBlockingGet("你好".getBytes());
//						sender.get(10000);
						//proxy.blockingGet("你好".getBytes(), 10000);
						System.out.println(new String((byte[])proxy.blockingGet("你好".getBytes(), 10000)));
					}catch(Exception e){
						System.out.println("cuowu");
					}
					
				}
			
		System.out.println("all total time: "+ startTime);
		proxy.close();
	}
}
