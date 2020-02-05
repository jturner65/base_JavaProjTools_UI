package base_UI_Objects.windowUI.sidebar;

/**
 * struct to hold desired configuration of sidebar buttons
 * @author john
 *
 */
public class mySidebarMenuBtnConfig {
	/**
	 * set row names for each row of ui action buttons getMouseOverSelBtnNames()
	 * @param _funcRowNames array of names for each row of functional buttons 
	 * @param _numBtnsPerFuncRow array of # of buttons per row of functional buttons - size must match # of entries in _funcRowNames array
	 * @param _numDbgBtns # of debug buttons
	 * @param _inclWinNames include the names of all the instanced windows
	 * @param _inclMseOvValues include a row for possible mouse over values
	 */
	public final String[] funcRowNames;
	public final int[] numBtnsPerFuncRow;
	public final int numDbgBtns;
	public final boolean inclWinNames;
	public final boolean inclMseOvValues;
	
	public mySidebarMenuBtnConfig(String[] _funcRowNames, int[] _numBtnsPerFuncRow, int _numDbgBtns, boolean _inclWinNames, boolean _inclMseOvValues) {
		funcRowNames = _funcRowNames;
		numBtnsPerFuncRow=_numBtnsPerFuncRow;
		numDbgBtns=_numDbgBtns;
		inclWinNames=_inclWinNames;
		inclMseOvValues=_inclMseOvValues;
	}

}//class mySidebarMenuBtnConfig
