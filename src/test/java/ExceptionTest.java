import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExceptionTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionTest.class);
	
	public static void main(String[] args){
		
		
		try{
			int b = 0;
			int a = 10/b;
		}catch (Exception e) {
			e.printStackTrace();
			System.out.println("test");
			LOGGER.error("输出有问题", e);
		}
	}

}
