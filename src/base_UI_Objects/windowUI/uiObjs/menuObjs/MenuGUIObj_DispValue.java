package base_UI_Objects.windowUI.uiObjs.menuObjs;

import base_UI_Objects.windowUI.uiObjs.base.Base_NumericGUIObj;
import base_UI_Objects.windowUI.uiObjs.base.base.GUIObj_Type;

/**
 * Label that displays a value that may change but cannot be interacted with.
 */
public class MenuGUIObj_DispValue extends Base_NumericGUIObj {
	
	/**
	 * @param _objID
	 * @param _name
	 * @param _flags
	 * @param _off
	 * @param _strkClr
	 * @param _fillClr
	 */
	public MenuGUIObj_DispValue(int _objID, String _name, double[] _minMaxMod, double _initVal, double[] _off, int[] _strkClr, int[] _fillClr) {
		super(_objID, _name, _minMaxMod, _initVal, GUIObj_Type.labelVal, new boolean[]{false, false, false},_off, _strkClr, _fillClr);
	}
	
	/**
	 * Set a new modifier value to use for this object : Mod values for Value Display objects will always be 0
	 * @param _unused
	 */
	@Override
	public final void setNewMod(double _unused){	
		modMult = 0.0;
		formatStr = "%.0f";
	}

	@Override
	public final double modVal(double mod) {
		//display only - no mod
		return val;
	}
	
	@Override
	protected final String getValueAsString(double _val) {	return String.format(formatStr,_val);}

	/**
	 * Labels should never update the consuming updater
	 */
	@Override
	public boolean shouldUpdateConsumer() {return false;}
}//class MenuGUIObj_DispValue
