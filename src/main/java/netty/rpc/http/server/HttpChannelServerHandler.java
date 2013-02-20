package netty.rpc.http.server;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;

public abstract class HttpChannelServerHandler extends SimpleChannelUpstreamHandler{
	
	protected abstract byte[] processor(HttpRequest httpRequest);
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		HttpRequest request = (HttpRequest) e.getMessage();
		String uri = request.getUri();
		System.out.println("uri:"+uri);
		if(uri.equalsIgnoreCase("/favicon.ico")){
			HttpResponse response = new DefaultHttpResponse(HTTP_1_1, NOT_FOUND);  
	        Channel ch = e.getChannel();
	        // Write the initial line and the header.
	     	ch.write(response);
	     	ch.disconnect();
	     	ch.close();
		}else{
			HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
	        ChannelBuffer buffer=new DynamicChannelBuffer(2048);
	        
	        byte[] temp = processor(request);
	        if(temp!=null){
	        	buffer.writeBytes(temp);
	        }else{
	        	buffer.writeBytes("{error:true}".getBytes("UTF-8"));
	        }
	        
			//buffer.writeBytes("hello!! 你好".getBytes("UTF-8"));
			response.setContent(buffer);
//			if(Access_Control_Allow_Origin!=null && Access_Control_Allow_Origin.length()>1){
//				response.addHeader("Access-Control-Allow-Origin", Access_Control_Allow_Origin); //解决跨与问题
//			}
			response.setHeader("Content-Type", "text/html; charset=UTF-8");
//			System.out.println(response.getContent().writerIndex());
			response.setHeader("Content-Length", response.getContent().writerIndex());
			Channel ch = e.getChannel();
			// Write the initial line and the header.
			ChannelFuture future = ch.write(response);
			final CountDownLatch downLatch = new CountDownLatch(1);
			future.addListener(new ChannelFutureListener() {
				
				public void operationComplete(ChannelFuture future) throws Exception {
					downLatch.countDown();
				}
			});
			downLatch.await(10,TimeUnit.SECONDS);
			ch.disconnect();
			ch.close();	
		} 
		
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		Channel ch = e.getChannel();
		Throwable cause = e.getCause();
		if (cause instanceof TooLongFrameException) {
			sendError(ctx, BAD_REQUEST);
			return;
		}

		cause.printStackTrace();
		if (ch.isConnected()) {
			sendError(ctx, INTERNAL_SERVER_ERROR);
		}
	}

	private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
		response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
		response.setContent(ChannelBuffers.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));

		// Close the connection as soon as the error message is sent.
		ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
	}
	
}
