package org.sonar.samples.java.checks;
 
import org.junit.jupiter.api.Test;
import org.sonar.java.checks.verifier.CheckVerifier;

class CheckMethodCommentTest {

	@Test
	  void testMethodComment() {
	    CheckVerifier.newVerifier()
	      .onFile("src/test/files/CheckMethodCommentTestRule.java")
	      .withCheck(new CheckMethodCommentRule())
	      .verifyIssues();
	  }
}