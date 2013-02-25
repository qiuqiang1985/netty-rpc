package netty.rpc.client;

/**
 * 对返回的消息不做任何处理
 * @author steven.qiu
 *
 */
public class NullResultHandler implements ResultHandler{

	public void processor(Object message) {}

}
