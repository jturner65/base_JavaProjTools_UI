package base_UI_Objects.windowUI.uiObjs.menuObjs;

import base_UI_Objects.windowUI.uiObjs.base.Base_NumericGUIObj;
import base_UI_Objects.windowUI.uiObjs.base.base.GUIObj_Type;

public class MenuGUIObj_Int extends Base_NumericGUIObj {

	/**
	 * Build an int-based UI object
	 * @param _objID the index of the object in the managing container
	 * @param _name the name/display label of the object
	 * @param _minMaxMod the minimum and values this object can hold, and the base modifier amount
	 * @param _initVal the initial value of this object
	 * @param _flags any preset behavior flags
	 * @param strkClr stroke color of text
	 * @param fillClr fill color around text
	 */
	public MenuGUIObj_Int(int _objID, String _name, double[] _minMaxMod, double _initVal, boolean[] _flags, int[] strkClr, int[] fillClr) {
		super(_objID, _name, _minMaxMod, _initVal, GUIObj_Type.IntVal, _flags, strkClr, fillClr);
	}
	
	//Integer values only
	@Override
	protected final double modValAssign(double _val) {return Math.round(_val);}
	
	/**
	 * Set a new modifier value to use for this object : Mod values for list objects will always be 1
	 * @param _unused
	 */
	@Override
	public final void setNewMod(double _unused){	
		modMult = 1.0;
		formatStr = "%.0f";
	}
	
	@Override
	protected final String getValueAsString(double _val) {	return String.format(formatStr,_val);}

}//class myGUIObj_Int
