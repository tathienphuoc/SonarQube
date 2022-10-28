import java.util.Arrays;

import com.sun.tools.sjavac.Log;

/**
 * Description for class MyClass_01 <br>
 * 
 * @author tathienphuoc
 * @see MyClass_01
 * @since J2EE 1.6
 */
class CheckNullDereferencingTestRule {
//	String getName() {
//		return "";
//	}
//
	public boolean isNameEmpty() {
		String name = null;
		name.length();// Noncompliant
		try {
			return name.length() == 0;
		} catch (NullPointerException e) {
			Log.error(e.getMessage());
			return 0;
		}
	}
}
