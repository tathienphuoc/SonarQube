/*=========================================================
*Copyright(c) 2022 CyberLogitec
*@FileName : ClassCommentUtils.java
*@FileTitle : ClassCommentUtils
*Open Issues :
*Change history :
*@LastModifyDate : 2022.08.11
*@LastModifier : 
*@LastVersion : 1.0
* 2022.08.11
* 1.0 Creation
=========================================================*/
package org.sonar.samples.java.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sound.midi.Soundbank;

import org.sonar.api.server.ws.WebService.NewAction;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.SyntaxTrivia;

public class CheckForbiddenStringEqualExpressionTestRule {
	public void hello(String a) {
		"".equals(a);
		a.equals("");// Noncompliant
	}
}