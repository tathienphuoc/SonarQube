package org.sonar.samples.java;

import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

class CheckNullDereferencingTestRule {
	
	public boolean getNum() {
		return (Boolean) null;
	}
	public boolean isNameEmpty(String x) {
		String a=null;
		if(getNum()) {
			a="fdasf";
		}
//		if(getNum()>20) {
			x=a;
//		}
//a=getNum();
		return x.length()==0;
	}
	
//	@CheckForNull
//	String getName(){return null;}
	
//	@CheckForNull
//	public boolean isNameEmpty() {
//	  return getName().length() == 0; 
//	}
}