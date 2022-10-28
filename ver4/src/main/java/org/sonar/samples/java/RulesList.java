/*
 * Copyright (C) 2012-2022 SonarSource SA - mailto:info AT sonarsource DOT com
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package org.sonar.samples.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sonar.plugins.java.api.JavaCheck;
import org.sonar.samples.java.checks.CheckClassCommentRule;
import org.sonar.samples.java.checks.CheckFileCommentRule;
import org.sonar.samples.java.checks.CheckFloatingPointValues;
import org.sonar.samples.java.checks.CheckForbiddenStringEqualExpressionRule;
import org.sonar.samples.java.checks.CheckICWE209Rule;
import org.sonar.samples.java.checks.CheckMethodCommentRule;
import org.sonar.samples.java.checks.NoIfStatementInTestsRule;

public final class RulesList {

	private RulesList() {
	}

	public static List<Class<? extends JavaCheck>> getChecks() {
		List<Class<? extends JavaCheck>> checks = new ArrayList<>();
		checks.addAll(getJavaChecks());
		checks.addAll(getJavaTestChecks());
		return Collections.unmodifiableList(checks);
	}

	/**
	 * These rules are going to target MAIN code only
	 */
	public static List<Class<? extends JavaCheck>> getJavaChecks() {
		return Collections.unmodifiableList(Arrays.asList(CheckClassCommentRule.class, CheckMethodCommentRule.class,
				CheckFileCommentRule.class, CheckForbiddenStringEqualExpressionRule.class, CheckICWE209Rule.class,
				CheckFloatingPointValues.class
		/*
		 * CheckForbiddenAssignmentStatementForPrivateArrayFieldRule.class,
		 * CheckNullDereferencingRule.class
		 */));
	}

	/**
	 * These rules are going to target TEST code only
	 */
	public static List<Class<? extends JavaCheck>> getJavaTestChecks() {
		return Collections.unmodifiableList(Arrays.asList(NoIfStatementInTestsRule.class));
	}
}
