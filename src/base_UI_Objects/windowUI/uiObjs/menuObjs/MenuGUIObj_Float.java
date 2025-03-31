package base_UI_Objects.windowUI.uiObjs.menuObjs;

import base_UI_Objects.windowUI.uiObjs.base.Base_NumericGUIObj;
import base_UI_Objects.windowUI.uiObjs.base.base.GUIObj_Type;

public class MenuGUIObj_Float extends Base_NumericGUIObj {
	/**
	 * Build a float-based UI object
	 * @param _objID the index of the object in the managing container
	 * @param _name the name/display label of the object
	 * @param _minMaxMod the minimum and values this object can hold, and the base modifier amount
	 * @param _initVal the initial value of this object
	 * @param _flags any preset behavior flags
	 * @param strkClr stroke color of text
	 * @param fillClr fill color around text
	 */
	public MenuGUIObj_Float(int _objID, String _name, double[] _minMaxMod, double _initVal, boolean[] _flags, int[] strkClr, int[] fillClr) {
		super(_objID, _name, _minMaxMod, _initVal, GUIObj_Type.FloatVal, _flags, strkClr, fillClr);	
	}

	//Float object has no manipulations during modification
	@Override
	protected final double modValAssign(double _val) {return _val;}
	
	/**
	 * Set a new modifier value to use for this object
	 * @param _newval
	 */
	@Override
	public final void setNewMod(double _newval){	
		if (_newval > (maxVal-minVal)) {
			_newval = (maxVal-minVal);
		}
		modMult = _newval;
		if(modMult >=1) {
			formatStr = "%.0f";
		} else {
			int formatVal = (int) Math.ceil(-Math.log10(modMult*1.00001))+1;
			formatStr = "%."+formatVal+"f";
		}
	}	
	@Override
	protected final String getValueAsString(double _val) {	return String.format(formatStr,_val);}



}//class myGUIObj_Float
