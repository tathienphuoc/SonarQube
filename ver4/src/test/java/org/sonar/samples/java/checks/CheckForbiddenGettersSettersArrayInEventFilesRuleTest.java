/*=========================================================
*Copyright(c) 2022 CyberLogitec
*@FileName : CheckClassCommentTest.java
*@FileTitle : CheckClassCommentTest
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
 * Check Class Comment Test
 * 
 * @author tathienphuoc
 * @see CheckForbiddenGettersSettersArrayInEventFilesRuleTest
 * @since J2EE 1.6
 */
class CheckForbiddenGettersSettersArrayInEventFilesRuleTest {
	@Test
	void testClassComment() {
		CheckVerifier.newVerifier().onFile("src/test/files/CheckForbiddenGettersSettersArrayInEventFilesTestRuleEvent.java")
				.withCheck(new CheckForbiddenAssignmentStatementForPrivateArrayFieldRule()).verifyIssues();
	}
}