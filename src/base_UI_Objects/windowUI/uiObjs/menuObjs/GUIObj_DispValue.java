package base_UI_Objects.windowUI.uiObjs.menuObjs;

import base_UI_Objects.windowUI.uiObjs.base.Base_ReadOnlyGUIObj;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;

/**
 * Label that displays a value that may change but cannot be interacted with.
 */
public class GUIObj_DispValue extends Base_ReadOnlyGUIObj {
	
	public GUIObj_DispValue(int _objID, GUIObj_Params objParams) {		
	    super(_objID, objParams);      
	    modMult = 0.0;
	    formatStr = "%.0f";	
	 }
	
	@Override
	protected final String getValueAsString(double _val) {	return String.format(formatStr,_val);}


}//class GUIObj_DispValue
