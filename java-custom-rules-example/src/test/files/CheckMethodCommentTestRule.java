/*=========================================================
*Copyright(c) 2022 CyberLogitec
*@FileName : CheckMethodCommentRule.java
*@FileTitle : CheckMethodCommentRule
*Open Issues :
*Change history :
*@LastModifyDate : 2022.08.11
*@LastModifier : 
*@LastVersion : 1.0
* 2022.08.11
* 1.0 Creation
=========================================================*/
class MyClass {

	/**
	 * Carry out business scenarios for each event<br>
	 * Branch processing of all events occurring in ALPS-MoneyMgmt system work<br>
	 * list ber: * fdsfsa *
	 *
	 * @param Event e
	 * @return EventResponse
	 * @exception EventException
	 */
	public EventResponse perform(Event e) throws EventException {
		return eventResponse;
	}
	public HashMap<String, List<ABC>> perform2(List<ArrayList<String>> e, int b, HashMap<String, List<ABC>> c) throws Exception { // Noncompliant
		return 10;
	}
}
