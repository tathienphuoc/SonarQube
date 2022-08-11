/*=========================================================
*Copyright(c) 2022 CyberLogitec
*@FileName : CheckMethodCommentTest.java
*@FileTitle : CheckMethodCommentTest
*Open Issues :
*Change history :
*@LastModifyDate : 2022.08.11
*@LastModifier : 
*@LastVersion : 1.0
* 2022.08.11
* 1.0 Creation
=========================================================*/

package org.sonar.samples.java.checks;

import org.junit.jupiter.api.Test;
import org.sonar.java.checks.verifier.CheckVerifier;

/**
 * Check Method Comment Test
 * 
 * @author tathienphuoc
 * @see CheckMethodCommentTest
 * @since J2EE 1.6
 */
class CheckMethodCommentTest {

	@Test
	void testMethodComment() {
		CheckVerifier.newVerifier().onFile("src/test/files/CheckMethodCommentTestRule.java")
				.withCheck(new CheckMethodCommentRule()).verifyIssues();
	}
}