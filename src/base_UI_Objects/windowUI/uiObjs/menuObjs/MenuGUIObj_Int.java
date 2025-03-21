package base_UI_Objects.windowUI.uiObjs.menuObjs;

import base_UI_Objects.windowUI.uiObjs.base.Base_NumericGUIObj;
import base_UI_Objects.windowUI.uiObjs.base.base.GUIObj_Type;

public class MenuGUIObj_Int extends Base_NumericGUIObj {

	/**
	 * Build an int-based UI object
	 * @param _ri render interface
	 * @param _objID the index of the object in the managing container
	 * @param _name the name/display label of the object
	 * @param _minMaxMod the minimum and values this object can hold, and the base modifier amount
	 * @param _initVal the initial value of this object
	 * @param _flags any preset configuration flags
	 * @param _off offset from label in x,y for placement of drawn ornamental box. make null for none
	 * @param strkClr stroke color of text
	 * @param fillClr fill color around text
	 */
	public MenuGUIObj_Int(int _objID, String _name, double[] _minMaxMod, double _initVal, boolean[] _flags, double[] _Off, int[] strkClr, int[] fillClr) {
		super(_objID, _name, _minMaxMod, _initVal, GUIObj_Type.IntVal, _flags, _Off, strkClr, fillClr);
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

	@Override
	protected String getValueAsString(double _val) {	return String.format("%.0f",_val);}

}//class myGUIObj_Int
