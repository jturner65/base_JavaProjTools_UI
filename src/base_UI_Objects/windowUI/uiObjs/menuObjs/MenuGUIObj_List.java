package base_UI_Objects.windowUI.uiObjs.menuObjs;

import base_Math_Objects.vectorObjs.floats.myPointf;
import base_UI_Objects.windowUI.uiObjs.base.Base_NumericGUIObj;
import base_UI_Objects.windowUI.uiObjs.base.base.GUIObj_Type;

public class MenuGUIObj_List extends Base_NumericGUIObj {
	
	protected String[] listVals = new String[] {"None"};

	/**
	 * Build a list-based UI object
	 * @param _ri render interface
	 * @param _objID the index of the object in the managing container
	 * @param _name the name/display label of the object
	 * @param _start the upper left corner of the hot spot for this object
	 * @param _end the lower right corner of the hot spot for this object
	 * @param _minMaxMod the minimum and values this object can hold, and the base modifier amount
	 * @param _initVal the initial value of this object
	 * @param _flags any preset configuration flags
	 * @param _off offset from label in x,y for placement of drawn ornamental box. make null for none
	 * @param _listVals
	 * @param strkClr stroke color of text
	 * @param fillClr fill color around text
	 */
	public MenuGUIObj_List(int _objID, String _name, myPointf _start, myPointf _end,
			double[] _minMaxMod, double _initVal, boolean[] _flags, double[] _off, String[] _listVals, int[] strkClr, int[] fillClr) {
		super(_objID, _name, _start, _end, _minMaxMod, _initVal, GUIObj_Type.ListVal, _flags, _off, strkClr, fillClr);
		setListVals(_listVals);
	}
	
	@Override
	public final double modVal(double mod){
		double oldVal = val;
		val += (mod*modMult);
		val = Math.round(val);
		val = forceBounds(val);
		if (oldVal != val) {setIsDirty(true);}		
		return val;		
	}
	
	/**
	 * Get this UI object's value as a string
	 * @return
	 */
	@Override
	protected String getValueAsString(double _val) {
		return listVals[(((int)_val) % listVals.length)];
	}
	
	/**
	 * return the string representation corresponding to the passed index in the list of this object's values, if any exist
	 * @param idx index in list of value to retrieve
	 * @return
	 */
	public final String getListValStr(int idx) {
		return getValueAsString(idx);
	}// getListValStr

	
	/**
	 * Set this list object's list of values
	 * @param _vals
	 * @return returns current val cast to int as idx
	 */
	public final int setListVals(String[] _vals) {
		if((null == _vals) || (_vals.length == 0)) {	listVals = new String[] {"List Not Initialized!"};	} 
		else {
			listVals = new String[_vals.length];
			System.arraycopy(_vals, 0, listVals, 0, listVals.length);			
		}
		double curVal = getVal();
		setNewMax(listVals.length-1);
		curVal = setVal(curVal);
		return (int) curVal;
		
	}//setListVals
	
	public final int getIDXofStringInArray(String tok) {
		for(int i=0;i<listVals.length;++i) {
			if(listVals[i].trim().equals(tok.trim())) {return i;}
		}return -1;
	}//getIDXofStringInArray
	
	
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


}//class myGUIObj_List
