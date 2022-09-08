package demo;
import java.nio.file.attribute.FileStoreAttributeView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class A extends Main {
	/**
	 * 
	 * @param String[] wrapper
	 * @param String param
	 * @throws Exception
	 */
	@Override
	public void doWork(String wrapper, String[] param) throws Exception{}
	public static void main(String[] args) {
		try {
			HashMap<String,String> a=new HashMap<>();
			System.out.println(a.getClass().getName());
			String name =Class.forName("java.util.HashMap").getSimpleName();
			System.out.println("name "+name);
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}