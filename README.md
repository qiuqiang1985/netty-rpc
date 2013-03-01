netty-rpc
=========

rpc based on netty

Server端只要实现AbstractServerHandler类的processor方法。该方法用来对消息的具体处理。

			byte[] ret = transport.getValue(); //处理之后的返回结果
			//System.out.println("8080端口接受到的数据："+new String(ret));
			
			ServerSender sender = new ServerSender(channel, transport);
			sender.send(ret);
			//System.out.println("向8081端口发送数据完成");

Client端只要提供了三个方法与服务端通讯：（1）阻塞式的消息处理；（2）非阻塞式的消息处理；（3）只发送消息，没有消息返回的情况。
	
			nonBlockingGet(byte[] request);
			blockingGet(byte[] request, long timeout);
			sendNoBack(byte[] request);
			

详细使用参考test的代码。

程序主体参考：http://www.iteye.com/topic/1124280
二进制协议参考：http://www.blogjava.net/hankchen/archive/2012/02/04/369378.html



