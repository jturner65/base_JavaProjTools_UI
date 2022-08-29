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
	 * @param _funcBtnNames array of arrays of names for each button
	 * @param _numBtnsPerFuncRow array of # of buttons per row of functional buttons - size must match # of entries in _funcRowNames array
	 * @param _debugBtnNames list of names for debug buttons
	 * @param _inclWinNames include the names of all the instanced windows
	 * @param _inclMseOvValues include a row for possible mouse over values
	 */
	public final String[] funcRowNames;
	public final String[][] funcBtnNames;
	public final int[] numBtnsPerFuncRow;
	public final String[] debugBtnNames;
	public final boolean inclWinNames;
	public final boolean inclMseOvValues;
	
	public mySidebarMenuBtnConfig(String[] _funcRowNames, String[][] _funcBtnNames, String[] _debugBtnNames, boolean _inclWinNames, boolean _inclMseOvValues) {
		funcRowNames = _funcRowNames;
		funcBtnNames = _funcBtnNames;
		numBtnsPerFuncRow = new int[funcRowNames.length];
		for (int i=0;i<funcRowNames.length;++i) {
			numBtnsPerFuncRow[i] = funcBtnNames[i].length;
		}
		debugBtnNames = _debugBtnNames;
		inclWinNames=_inclWinNames;
		inclMseOvValues=_inclMseOvValues;
	}

	
}//class mySidebarMenuBtnConfig
