package base_UI_Objects.windowUI.sidebar;

/**
 * struct to hold desired configuration of sidebar buttons
 * @author john
 *
 */
public class SidebarMenuBtnConfig {
	public final String[] funcRowNames;
	public final String[][] funcBtnLabels;
	public final int[] numBtnsPerFuncRow;
	public final String[] debugBtnLabels;
	public final boolean inclWinNames;
	public final boolean inclMseOvValues;
	public final String[] winTitles;
	
	/**
	 * Configuration for important side bar features, 
	 * including to set row names for each row of ui action buttons getMouseOverSelBtnLabels()
	 * @param _funcRowNames array of names for each row of functional buttons 
	 * @param _funcBtnLabels array of arrays of labels for each button
	 * @param _numBtnsPerFuncRow array of # of buttons per row of functional buttons - size must match # of entries in _funcRowNames array
	 * @param _debugBtnLabels list of labels for debug buttons
	 * @param _inclWinNames include the names of all the instanced windows
	 * @param _inclMseOvValues include a row for possible mouse over values
	 * @param _winTitles the titles of all the windows in this application
	 */
	public SidebarMenuBtnConfig(String[] _funcRowNames, String[][] _funcBtnLabels, String[] _debugBtnLabels, String[] _winTitles, boolean _inclWinNames, boolean _inclMseOvValues) {
		funcRowNames = _funcRowNames;
		funcBtnLabels = _funcBtnLabels;
		numBtnsPerFuncRow = new int[funcRowNames.length];
		for (int i=0;i<funcRowNames.length;++i) {
			numBtnsPerFuncRow[i] = funcBtnLabels[i].length;
		}
		debugBtnLabels = _debugBtnLabels;
		inclWinNames=_inclWinNames;
		inclMseOvValues=_inclMseOvValues;
		winTitles = _winTitles;
	}

	
}//class mySidebarMenuBtnConfig
