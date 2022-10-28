import java.util.Arrays;

import com.sun.tools.sjavac.Log;
import org.apache.log4j.Logger;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;

/**
 * Description for class MyClass_01 <br>
 * 
 * @author tathienphuoc
 * @see MyClass_01
 * @since J2EE 1.6
 */
class CheckNullDereferencingTestRule extends ServiceCommandSupport {
	public boolean isNameEmpty() throws NullPointerException {
		Logger log;
		try {
			return name;
		} catch (NullPointerException e) {
			e.printStackTrace();// Noncompliant
			log.error(e.getMessage());
			throw new Exception();
			return 0;
		}
	}
}
