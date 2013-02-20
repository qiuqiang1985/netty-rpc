package netty.rpc.coder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

/**
 * 协议解码
 * allLen--keyLen-->keyBytes--valueLen-->valueBytes
 * @author steven.qiu
 *
 */
public class TransportDecoder extends FrameDecoder{

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer) throws Exception {
		
		if(buffer.readableBytes()<4){
			return null;
		}
		
		int dataLength = buffer.getInt(buffer.readerIndex());
		if(buffer.readableBytes() < dataLength+4){
			return null;
		}
		
		buffer.skipBytes(4);
		
		Transport transport = new Transport();
		int keyLen = buffer.readInt();
		byte[] key = new byte[keyLen];
		buffer.readBytes(key);
		int valueLen = buffer.readInt();
		byte[] value = new byte[valueLen];
		buffer.readBytes(value);
		
		transport.setKey(key);
		transport.setValue(value);
		
		return transport;
	}

}
