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
	private void replanCOP(SceCopHdrVO tmpList) {
		interfaceToo(tmpList.getBkgNo());// Noncompliant
		if(tmpList!=null) {
			interfaceToo(tmpList.getBkgNo());
		}
	}
}
