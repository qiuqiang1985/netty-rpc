package server;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import netty.rpc.client.ClientProxy;


public class TestClient {
	
	public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException, IOException{
		
		final ClientProxy proxy = ClientProxy.createClientProxy("localhost", 8080);
		
		long startTime = System.currentTimeMillis();
		
		Thread thread1 = new Thread(new Runnable(){

			
			public void run() {
				// TODO Auto-generated method stub
				for(int i=0;i<10000;i++){
					try{
//						ClientSender sender = proxy.NoBlockingGet("你好".getBytes());
//						sender.get(10000);
						proxy.blockingGet("你好1".getBytes(), 10000);
						//proxy.sendNoBack("你好".getBytes());
						//System.out.println(new String((byte[])proxy.blockingGet("你好".getBytes(), 10000)));
					}catch(Exception e){
						System.out.println("cuowu");
					}
					
				}
				
				System.out.println("thread1 done "+System.currentTimeMillis());
				
			}
			
		});
		
		Thread thread2 = new Thread(new Runnable(){

			
			public void run() {
				// TODO Auto-generated method stub
				for(int i=0;i<10000;i++){
					try{
//						ClientSender sender = proxy.NoBlockingGet("你好".getBytes());
//						sender.get(10000);
						proxy.blockingGet("你好".getBytes(), 10000);
//						proxy.sendNoBack("你好1".getBytes());
						//System.out.println(new String((byte[])proxy.blockingGet("你好".getBytes(), 10000)));
					}catch(Exception e){
						System.out.println("cuowu");
					}
					
				}
				
				System.out.println("thread1 done "+System.currentTimeMillis());
				
			}
			
		});
		
		
		
		thread1.start();
		thread2.start();

		thread1.join();
		thread2.join();

		System.out.println("all total time: "+ (System.currentTimeMillis()-startTime));
		
		
		proxy.close();
	}
}
