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
//	double x = 0.0;
//	Double angle;

//	String getName() {
//		return "";
//	}
//
	public boolean isNameEmpty() {
		String name = null;
		double angle = 0.0;
		if (angle > 45.5) {
			setUpTime();
		}

		Double angle2 = new Double(0.0);
		if(angle==angle) {// Noncompliant
			
		}
//		if ( angle == 1.1 && angle == 2.2 || 0 == angle) {
//			setUpTime();
//		}
		if(0 == angle.compareTo(new Double(45.5))) {
			setUpTime();
		}
		try {
			return name.length() == 0;
		} catch (NullPointerException e) {
			Log.error(e.getMessage());
			return 0;
		}
	}
}
