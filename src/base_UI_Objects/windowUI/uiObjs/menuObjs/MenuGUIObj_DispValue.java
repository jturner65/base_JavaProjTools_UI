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
	public MenuGUIObj_DispValue(int _objID, String _name, double _initVal) {
		super(_objID, _name, new double[] {-Double.MAX_VALUE, Double.MAX_VALUE, 0}, _initVal, GUIObj_Type.LabelVal, new boolean[]{false, false, false});
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

	//display only object ignores any mod so just return original val
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

}//class MenuGUIObj_DispValue
