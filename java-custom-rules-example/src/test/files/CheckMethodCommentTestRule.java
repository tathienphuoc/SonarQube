import java.sql.SQLException;
/**
 * ALPS-MoneyMgmt Business Logic ServiceCommand - Process business transaction for ALPS-MoneyMgmt.
 * 
 * @author phuoc
 * @see MoneyMgmtDBDAO
 * @since J2EE 1.6
 */
class MyClass {
	/**
	 * Carry out business scenarios for each event<br>
	 * Branch processing of all events occurring in ALPS-MoneyMgmt system work<br>
	 * 
	 * @param Event e
	 * @return EventResponse
	 * @exception EventException
	 */
	public EventResponse perform(Event e) throws EventException {
		return eventResponse;
	}
}