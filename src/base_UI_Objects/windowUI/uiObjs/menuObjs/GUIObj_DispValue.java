package base_UI_Objects.windowUI.uiObjs.menuObjs;

import base_UI_Objects.windowUI.uiObjs.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;

/**
 * Label that displays a value that may change but cannot be interacted with.
 */
public class GUIObj_DispValue extends Base_GUIObj {
	
	/**
	 * @param _objID
	 * @param _name
	 * @param _flags
	 * @param _off
	 * @param _strkClr
	 * @param _fillClr
	 */
	public GUIObj_DispValue(int _objID, GUIObj_Params objParams) {		super(_objID, objParams);	}
	
	/**
	 * Set a new modifier value to use for this object : Mod values for Value Display objects will always be 0
	 * @param _unused
	 */
	@Override
	public final void setNewMod(double _unused){	
		modMult = 0.0;
		formatStr = "%.0f";
	}

	/**
	 * Labels ignore any mod so just return original val
	 */
	@Override
	public final double modValAssign(double _notUsed) {	return getVal();}
	
	/**
	 * Dragging is disabled on labels
	 */
	@Override
	public final double dragModVal(double _notUsed) {return getVal();}
	
	/**
	 * Click modification is disabled on labels
	 */
	public final double clickModVal(double _notUsed, double _notUsed2) {return getVal();}
	
	@Override
	protected final String getValueAsString(double _val) {	return String.format(formatStr,_val);}

	/**
	 * Labels should never update the consuming updater
	 */
	@Override
	public boolean shouldUpdateConsumer() {return false;}
	

	@Override
	protected boolean checkUIObjectStatus_Indiv() {
		return true;
	}

}//class GUIObj_DispValue
