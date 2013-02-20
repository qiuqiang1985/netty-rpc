package netty.rpc.http.server;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;



/**
 * HttpServer Pipeline Factory
 * @author steven.qiu
 *
 */
public class HttpPipelineServerFactory implements ChannelPipelineFactory{
	
	private HttpChannelServerHandler serverHandler;
	
	public HttpPipelineServerFactory(HttpChannelServerHandler serverHandler){
		this.serverHandler = serverHandler;
	}

	public ChannelPipeline getPipeline() throws Exception {
		
		ChannelPipeline pipeline = Channels.pipeline();
		
		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("deflater", new HttpContentCompressor());
		pipeline.addLast("handler", this.serverHandler);
		
		return pipeline;
	}

}
