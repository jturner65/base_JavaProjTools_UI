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
	 */
	public MenuGUIObj_Int(int _objID, String _name, double[] _minMaxMod, double _initVal, boolean[] _flags) {
		super(_objID, _name, _minMaxMod, _initVal, GUIObj_Type.IntVal, _flags);
	}
	
	/**
	 * Build an int-based UI object
	 * @param _objID the index of the object in the managing container
	 * @param _name the name/display label of the object
	 * @param _minMaxMod the minimum and values this object can hold, and the base modifier amount
	 * @param _initVal the initial value of this object
	 * @param _objType the type of object being constructed (this is called from child classes)
	 * @param _flags any preset behavior flags
	 */
	public MenuGUIObj_Int(int _objID, String _name, double[] _minMaxMod, double _initVal, GUIObj_Type _objType, boolean[] _flags) {
		super(_objID, _name, _minMaxMod, _initVal, _objType, _flags);
	}
	
	//Called after UI modification but before range checking - this class only supports integer values.
	@Override
	protected final double modValAssign(double _val) {return Math.round(_val);}
	
	/**
	 * Set a new modifier value to use for this object : Mod values for integer-based objects will always be 1
	 * @param _unused
	 */
	@Override
	public final void setNewMod(double _unused){	
		modMult = 1.0;
		formatStr = "%.0f";
	}
	
	/**
	 * Get this UI object's value as a string
	 * @return
	 */
	@Override
	protected String getValueAsString(double _val) {	return String.format(formatStr,_val);}
	
}//class myGUIObj_Int
