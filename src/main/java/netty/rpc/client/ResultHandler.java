package netty.rpc.client;

/**
 * 返回结果处理handler
 * @author steven.qiu
 *
 */
public interface ResultHandler {
	
	public void processor(Object message);

}
