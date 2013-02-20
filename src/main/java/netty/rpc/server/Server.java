package netty.rpc.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server to provide service
 * @author steven.qiu
 *
 */
public class Server {
	
	private static final Logger logger = LoggerFactory.getLogger(Server.class);
	
	private static final ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
	
	private int port;
	private AbstractServerHandler serverHandler;
	private Channel listenChannel;
	
	public Server(int port, AbstractServerHandler serverHandler){
		this.port = port;
		this.serverHandler = serverHandler;
	}
	
	
	/**
	 * 启动服务
	 * @throws Exception
	 */
	public void startUp() throws Exception{
		bootstrap.setPipelineFactory(new PipelineServerFactory(this.serverHandler));
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		bootstrap.setOption("reuseAddress", true);
		listenChannel=bootstrap.bind(new InetSocketAddress(this.port));
		logger.info("Server is started on port: "+this.port);
	}
	
	/**
	 * 关闭服务
	 * @throws Exception
	 */
	public void shutDown() throws Exception{
		
		try{
			listenChannel.close().awaitUninterruptibly(); //close listen channel
		}catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(),e);
		}finally{
			serverHandler.close();
			logger.info("Server is shutdown on port: "+this.port);
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
