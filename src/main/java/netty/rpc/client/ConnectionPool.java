package netty.rpc.client;

import java.util.concurrent.LinkedBlockingQueue;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * connection pool.
 * 连接池，clientProxy从该池中获取一个客户端发送对象
 * @author steven.qiu
 *
 */
public class ConnectionPool {
	
	private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
	
	private LinkedBlockingQueue<Channel> poolBlockingQueue; //阻塞队列
	
	public ConnectionPool(){
		this.poolBlockingQueue = new LinkedBlockingQueue<Channel>();
	}
	
	/**
	 * 从队列中获取一个channel
	 * @return
	 */
	public Channel getChannel(){
		try{
			return poolBlockingQueue.poll();
		}catch (Exception e) {
			logger.error("Pop a channel error.", e);
			return null;
		}
	}
	
	/**
	 * 将channel插入到队列的尾部
	 * @param channel
	 */
	public void addLast(Channel channel){
		try{
			poolBlockingQueue.put(channel);
		}catch (Exception e) {
			logger.error("Add a channel error.", e);
		}
		
	}
	
	/**
	 * 关闭连接池
	 * 将连接池中的链接全部关闭，并清空连接池
	 */
	public synchronized void close(){
		
		Channel channel = null;
		
		while(true){
			
			try{
				channel = poolBlockingQueue.poll();
				if(channel!=null){
					channel.close().awaitUninterruptibly();
				}else{
					break;
				}
				
			}catch (Exception e) {
				logger.error("Close all channel in pool.", e);
			}
		}
		
		poolBlockingQueue.clear();
	}

}
