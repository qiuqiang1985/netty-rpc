package netty.rpc.client;

import netty.rpc.coder.TransportDecoder;
import netty.rpc.coder.TransportEncoder;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;


/**
 * Client Pipeline Factory
 * @author steven.qiu
 *
 */
public class PipelineClientFactory implements ChannelPipelineFactory{
	
	private SimpleChannelUpstreamHandler handler;
	
	public PipelineClientFactory(SimpleChannelUpstreamHandler handler){
		this.handler = handler;
	}

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		
		pipeline.addLast("decoder", new TransportDecoder());
		pipeline.addLast("encoder", new TransportEncoder());
		pipeline.addLast("handler", handler);
		
		return pipeline;
	}

}
