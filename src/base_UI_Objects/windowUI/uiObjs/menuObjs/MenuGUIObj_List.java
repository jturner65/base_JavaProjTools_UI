package base_UI_Objects.windowUI.uiObjs.menuObjs;

import base_UI_Objects.windowUI.uiObjs.base.base.GUIObj_Type;

/**
 * UI object that supports selecting between a list of available values.
 */
public class MenuGUIObj_List extends MenuGUIObj_Int {
	
	/**
	 * List of different values to be displayed for this list-based object
	 */
	private String[] listVals = new String[] {"None"};
	
	/**
	 * Original list of different values to be displayed for this list based-object
	 */
	private String[] origListVals;
	
	/**
	 * 
	 * Build a list-based UI object
	 * @param _objID the index of the object in the managing container
	 * @param _name the name/display label of the object
	 * @param _minMaxMod the minimum and values this object can hold, and the base modifier amount
	 * @param _initVal the initial value of this object
	 * @param _objType Type of object (for child classes)
	 * @param _flags any preset behavior flags
	 * @param _listVals Initial list of values this object holds
	 */
	public MenuGUIObj_List(int _objID, String _name, double[] _minMaxMod, double _initVal, GUIObj_Type _objType, boolean[] _flags, String[] _listVals) {
		super(_objID, _name, _minMaxMod, _initVal, _objType, _flags);
		setListVals(_listVals, true);
	}
	
	/**
	 * Build a list-based UI object
	 * @param _objID the index of the object in the managing container
	 * @param _name the name/display label of the object
	 * @param _minMaxMod the minimum and values this object can hold, and the base modifier amount
	 * @param _initVal the initial value of this object
	 * @param _flags any preset behavior flags
	 * @param _listVals Initial list of values this object holds
	 */
	public MenuGUIObj_List(int _objID, String _name, double[] _minMaxMod, double _initVal, boolean[] _flags, String[] _listVals) {
		this(_objID, _name, _minMaxMod, _initVal, GUIObj_Type.ListVal, _flags, _listVals);
	}

	/**
	 * Instance-specific reset
	 */
	@Override
	protected void resetToInit_Indiv() {
		//reset list values to be original list values
		listVals = new String[origListVals.length];
		System.arraycopy(origListVals, 0, listVals, 0, origListVals.length);
	}
	
	/**
	 * Get this UI object's value as a string
	 * @return
	 */
	@Override
	protected final String getValueAsString(double _val) {	return listVals[(((int)_val) % listVals.length)];}
	
	/**
	 * return the string representation corresponding to the passed index in the list of this object's values, if any exist
	 * @param idx index in list of value to retrieve
	 * @return
	 */
	public final String getListValStr(int idx) {	return getValueAsString(idx);}
	
	/**
	 * Get all list values
	 */
	public final String[] getListValues() {return listVals;}
	
	/**
	 * Set this list object's list of values
	 * @param vals The new list of values to set for this object
	 * @param setAsDefault Whether these values should be set as the default values (i.e. reloaded on reset)
	 * @return returns current val cast to int as idx
	 */
	public final int setListVals(String[] vals, boolean setAsDefault) {
		if((null == vals) || (vals.length == 0)) {	listVals = new String[] {"List Not Initialized!"};	} 
		else {
			listVals = new String[vals.length];
			System.arraycopy(vals, 0, listVals, 0, vals.length);
			if (setAsDefault) {
				origListVals = new String[listVals.length];
				System.arraycopy(listVals, 0, origListVals, 0, listVals.length);				
			}
		}
		//Update new max value
		double curVal = getVal();
		setNewMax(listVals.length-1);
		curVal = setVal(curVal);
		return (int) curVal;		
	}
	
	/**
	 * Get the number of entries this list UI object supports
	 */
	public final int getNumEntries() {return listVals.length;}
	
	/**
	 * Return the index of the passed string in the array of values this object manages.
	 * @param tok the string to find
	 * @return
	 */
	public final int getIDXofStringInArray(String tok) {
		for(int i=0;i<listVals.length;++i) {if(listVals[i].trim().equals(tok.trim())) {return i;}}
		return -1;
	}
	
	/**
	 * set list to display passed token, if it exists, otherwise return -1
	 * @param tok string in list to display
	 * @return ara [idx of string in list, otherwise -1, 0 if ok, 1 if bad]
	 */
	public final int[] setValInList(String tok) {
		int idx = getIDXofStringInArray(tok);
		if(idx >=0){		return new int[] {(int) setVal(idx), 0};}
		return new int[] {idx, 1};
	}
	
	/**
	 * Get string data array representing the value this list-based UI object holds - overrides Base_GUIObj impl.
	 * @return
	 */
	@Override
	protected String[] getStrDataForVal() {
		String[] tmpRes = new String[(1 + listVals.length) + (1 + origListVals.length)];
		tmpRes[0] = "Current Value: `"+ getValueAsString() + "`|Index in list : " + getValueAsInt() + "| Current List of values:";
		int i;
		for(i=0;i<listVals.length;++i) {tmpRes[i+1] = "\tidx" + i + ":"+getListValStr(i);	}
		tmpRes[i++] = "Init Value : "+ getValueAsString(initVals[3]) + "`|Index in list : " + ((int) initVals[3]) + "| Original List of values:";
		for(int j=i;j<i+origListVals.length;++j) {int listIdx = (j-i);tmpRes[j+1] = "\tidx" + listIdx + ":"+origListVals[listIdx];	}
		return tmpRes;
	}
}//class myGUIObj_List
