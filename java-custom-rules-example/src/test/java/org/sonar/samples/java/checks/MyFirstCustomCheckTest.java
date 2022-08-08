package org.sonar.samples.java.checks;
 
import org.junit.jupiter.api.Test;
import org.sonar.java.checks.verifier.CheckVerifier;

class MyFirstCustomCheckTest {

	@Test
	  void testMethodComment() {
	    CheckVerifier.newVerifier()
	      .onFile("src/test/files/CheckMethodCommentTestRule.java")
	      .withCheck(new CheckMethodCommentRule())
	      .verifyIssues();
	  }
	@Test
	  void testClassComment() {
	    CheckVerifier.newVerifier()
	      .onFile("src/test/files/CheckMethodCommentTestRule.java")
	      .withCheck(new CheckClassCommentRule())
	      .verifyIssues();
	  }
}