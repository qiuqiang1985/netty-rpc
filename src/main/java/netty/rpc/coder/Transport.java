package netty.rpc.coder;

/**
 * 传输协议
 * key是本次传输的唯一标示符
 * value是本次传输的二进制数据
 * @author steven.qiu
 *
 */
public class Transport {

	private byte[] key; 
	private byte[] value;
	
	public Transport(){
		
	}
	
	public Transport(byte[] key){
		this(key, null);
	}
	
	public Transport(byte[] key, byte[] value){
		this.key = key;
		this.value = value;
	}

	public byte[] getKey() {
		return key;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}
	
	

}
