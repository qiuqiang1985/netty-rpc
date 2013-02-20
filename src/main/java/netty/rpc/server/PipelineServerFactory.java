package netty.rpc.server;

import netty.rpc.coder.TransportDecoder;
import netty.rpc.coder.TransportEncoder;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;


/**
 * Server PipeLine Factory
 * @author steven.qiu
 *
 */
public class PipelineServerFactory implements ChannelPipelineFactory{
	
	
	public static final int readerIdleTimeSeconds = 100; //单位为秒
	public static final int writerIdleTimeSeconds = 200; //单位为秒
	public static final int allIdleTimeSeconds = 300; //单位为妙
	
	private AbstractServerHandler serverHandler;
	private static final HashedWheelTimer WHEEL_TIMER = new HashedWheelTimer();
	
	public PipelineServerFactory(AbstractServerHandler serverHandler){
		this.serverHandler = serverHandler;
	}

	public ChannelPipeline getPipeline() throws Exception {
		
		ChannelPipeline pipeline = Channels.pipeline();
		
		pipeline.addLast("decoder", new TransportDecoder());
		pipeline.addLast("encoder", new TransportEncoder());
		pipeline.addLast("timeout", new IdleStateHandler(WHEEL_TIMER, readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds));
		pipeline.addLast("heartbeat", new HeartBeat());
		pipeline.addLast("handler", this.serverHandler);
		
		return pipeline;
	}

}
