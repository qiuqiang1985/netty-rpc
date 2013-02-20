package server;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import netty.rpc.client.ClientProxy;


public class Test {
	
	class Task implements Runnable{
		
		private CyclicBarrier cyclicBarrier;
		private ClientProxy  clientProxy;
		private int request;
		private int latency;
		private int requestContentLength;
		
		
		public Task(CyclicBarrier cyclicBarrier, ClientProxy clientProxy, int request, int latency, int requestContentLength){
			this.cyclicBarrier = cyclicBarrier;
			this.clientProxy = clientProxy;
			this.request = request;
			this.latency = latency;
			this.requestContentLength = requestContentLength;
		}
		
		public void run(){
			try{
				//等待所有任务准备就绪
				byte[] content = new byte[this.requestContentLength];
				for(int n=0;n<requestContentLength;n++){
					content[n]='a';
				}
				cyclicBarrier.await();
				//测试内容
				long startTime = System.currentTimeMillis();
				int errorCount = 0;
				int successCount = 0;
				byte[] result = null;
				for(int j=0;j<request;j++){
					result = (byte[])clientProxy.blockingGet(content,latency);
					if(result==null){
						errorCount++;
					}else{
						successCount++;
					}
				}
				System.out.println("totalTime=="+(System.currentTimeMillis()-startTime));
				System.out.println("errorCount==="+errorCount);
				System.out.println("successCount==="+successCount);
				System.out.println("request==="+request);
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args){
		
		if(args.length!=6){
			System.out.println("Usage: javaClass threadNum requestNum latency contentLength serverAddress serverPort");
			System.out.println("threadNum---并发线程数");
			System.out.println("requestNum---每个线程的请求数");
			System.out.println("latency---请求最大时间延迟");
			System.out.println("contentLength---请求内容长度");
			System.out.println("serverAddress---远程server地址");
			System.out.println("serverPort---远程server端口");
			
			System.exit(-1);
		}
		int threadNum = Integer.parseInt(args[0]); //并发线程数
		int request = Integer.parseInt(args[1]); //每个线程的请求数
		int latency = Integer.parseInt(args[2]); //请求最大时间延迟
		int contentLenght = Integer.parseInt(args[3]); //请求内容长度
		String serverAddress = args[4]; //远程server地址
		int serverPort = Integer.parseInt(args[5]); //远程server端口
		CyclicBarrier cyclicBarrier = new CyclicBarrier(threadNum);
		ClientProxy clientProxy = ClientProxy.createClientProxy(serverAddress, serverPort);
		ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
		
		for(int i=0;i<threadNum;i++){
			executorService.execute(new Test().new Task(cyclicBarrier,clientProxy,request,latency, contentLenght));
		}
		
		executorService.shutdown();
		
		while(!executorService.isTerminated()){
			try{
				Thread.sleep(100);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		System.out.println("关闭clientProxy");
		try{
			System.exit(0);
			//clientProxy.closePool();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}

}
