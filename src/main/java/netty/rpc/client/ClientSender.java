package netty.rpc.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import netty.rpc.coder.Transport;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客服端消息发送类，以及获取服务端的响应
 * @author steven.qiu
 *
 */
public class ClientSender {
	
	private static final Logger logger = LoggerFactory.getLogger(ClientSender.class);
	
	private static final AtomicLong ID_ATOMIC_LONG = new AtomicLong(0);
	
	class Result{
		Object object;
	}
	
	private Channel channel;
	private ConcurrentHashMap<String, ResultHandler> callbackHandlerMap;
	private ConnectionPool connectionPool;
	private final Result result = new Result();
	private volatile boolean hasNotified = false;
	
	public static final NullResultHandler nullResultHandler = new NullResultHandler(); //对返回结果不做任何处理
	
	public ClientSender(Channel channel, ConcurrentHashMap<String, ResultHandler> callbackHandlerMap, ConnectionPool connectionPool){
		this.channel = channel;
		this.callbackHandlerMap = callbackHandlerMap;
		this.connectionPool = connectionPool;
	}
	
	/**
	 * 发送不等待结果，结果通过get方法获取
	 * @param messages
	 */
	public void preGet(byte[] messages){
		invoke(messages, new ResultHandler() {
			
			public void processor(Object message) {
				synchronized (result) {
					result.object = message;
					result.notify();
					hasNotified = true;
				}
			}
		});
	}
	
	/**
	 * 发送消息，等待返回
	 * @param messages
	 * @param timeout
	 * @return
	 */
	public Object blockingGet(byte[] messages, long timeout){
		synchronized (result) {
			try{
				invoke(messages, new ResultHandler() {
					
					public void processor(Object message) {
						synchronized (result) {
							result.object = message;
							result.notify();
						}
						
					}
				});
				result.wait(timeout);
			}catch (Exception e) {
				logger.error("BlockingGet error.", e);
			}
			
			return result.object;
		}
	}
	
	/**
	 * 发送消息，不等待返回
	 * @param messages
	 */
	public void sendNoBack(byte[] messages){
		invoke(messages, nullResultHandler);
	}
	
	/**
	 * 立即获取结果
	 * @return
	 */
	public Object get(){
		return get(0);
	}
	
	/**
	 * 获取结果，最大超时时间timeout
	 * @param timeout
	 * @return
	 */
	public Object get(long timeout){
		synchronized (result) {
			if(!hasNotified){
				try{
					result.wait(timeout);
				}catch (Exception e) {
					logger.error("Get result error.", e);
				}
			}
		}
		return result.object;
	}
	
	/**
	 * 写消息
	 * @param messages
	 * @param resultHandler
	 */
	private void invoke(final byte[] messages, final ResultHandler resultHandler){
		
		final String keyString = ID_ATOMIC_LONG.incrementAndGet()+"";
		
		Transport transport = new Transport(keyString.getBytes(), messages);
		
		if(resultHandler!=null){
			if(callbackHandlerMap.put(keyString, resultHandler)!=null){
				logger.error("the same key, please redesign your key function!");
			}
		}
		
		ChannelFuture channelFuture = this.channel.write(transport);
		
		channelFuture.addListener(new ChannelFutureListener() {
			
			public void operationComplete(ChannelFuture future) throws Exception {
				if(!future.isSuccess()){
					
					callbackHandlerMap.remove(keyString);
					
					if(future.isCancelled()){
						logger.error("write operation cancelled, the keyString {}", keyString);
					}else{
						logger.error("write failed!");
					}
				}
				
				free(); //将链接返回到连接池中
			}
		});
		
		
		
	}
	
	/**
	 * 释放连接
	 * 将该连接加入到连接池中
	 */
	public void free(){
		connectionPool.addLast(channel);
	}

}
