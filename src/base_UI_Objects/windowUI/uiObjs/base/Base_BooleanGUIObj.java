package base_UI_Objects.windowUI.uiObjs.base;

import base_UI_Objects.windowUI.uiObjs.base.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.base.GUIObj_Type;

/**
 * Base class for UI objects that manage boolean/clickable multi-state data
 * @author John Turner
 *
 */
public abstract class Base_BooleanGUIObj extends Base_GUIObj {
	
	protected int state = 0;
	protected final int initialState;
	/**
	 * Build a boolean/multi-state button
	 * @param _ri render interface
	 * @param _objID the index of the object in the managing container
	 * @param _name the name/display label of the object
	 * @param _start the upper left corner of the hot spot for this object
	 * @param _end the lower right corner of the hot spot for this object
	 * @param _initialState the initial state setting for this object
	 * @param _objType the type of UI object this is
	 * @param _flags any preset configuration flags
	 * @param _off offset from label in x,y for placement of drawn ornamental box. make null for none
	 * @param strkClr stroke color of text
	 * @param fillClr fill color around text
	 */
	public Base_BooleanGUIObj(int _objID, String _name, int _initialState, GUIObj_Type _objType,
			boolean[] _flags, double[] _off, int[] strkClr, int[] fillClr) {		
		super(_objID, _name, _objType, _flags, _off, strkClr, fillClr);
		state = _initialState;
		initialState = state;
	}//ctor
	
	@Override
	public void resetToInit() {
		state=initialState;
	}

	@Override
	protected void setValueFromString(String str) {
	}

	@Override
	public String getValueAsString() {
		return "" + state;
	}

	@Override
	protected String[] getStrDataForVal() {
		String[] tmpRes = new String[1];
		tmpRes[0]="Label :"+getLabel() + " State : "+ state;
		return tmpRes;
	}

}//class Base_BooleanGUIObj