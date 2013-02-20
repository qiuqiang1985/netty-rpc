package netty.rpc.http.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Http Server
 * @author steven.qiu
 *
 */
public class HttpServer {
	
	public static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
	
	private static final ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
	
	private int port;
	private HttpChannelServerHandler serverHandler;
	private Channel listenChannel;
	
	public HttpServer(int port, HttpChannelServerHandler serverHandler){
		this.port = port;
		this.serverHandler = serverHandler;
	}
	
	
	/**
	 * 启动服务
	 * @throws Exception
	 */
	public void startUp() throws Exception{
		bootstrap.setPipelineFactory(new HttpPipelineServerFactory(this.serverHandler));
		listenChannel=bootstrap.bind(new InetSocketAddress(this.port));
		logger.info("Server is started on port: "+this.port);
	}
	
	/**
	 * 关闭服务
	 * @throws Exception
	 */
	public void shutDown() throws Exception{
		try{
			listenChannel.close().awaitUninterruptibly();
		}catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(),e);
		}finally{
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
