package netty.rpc.server;

import netty.rpc.coder.Transport;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 服务端回复消息
 * @author steven.qiu
 *
 */
public class ServerSender {
	
	private static final Logger logger = LoggerFactory.getLogger(ServerSender.class);
	
	private Channel channel;
	private Transport transport;
	
	public ServerSender(Channel channel, Transport transport) {
		this.channel = channel;
		this.transport = transport;
	}
	
	/**
	 * 向客户端返回消息
	 * @param ret
	 */
	public void send(byte[] ret){
		transport.setValue(ret);
		channel.write(transport).addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture future) throws Exception {
				if(!future.isSuccess()){
					logger.error("Send a message not success.");
				}
			}
		});
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public Transport getTransport() {
		return transport;
	}

	public void setTransport(Transport transport) {
		this.transport = transport;
	}	
}
