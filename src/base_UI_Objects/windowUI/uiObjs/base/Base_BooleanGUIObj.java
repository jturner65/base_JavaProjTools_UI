package base_UI_Objects.windowUI.uiObjs.base;

import base_Math_Objects.vectorObjs.floats.myPointf;
import base_UI_Objects.windowUI.uiObjs.base.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.base.GUIObj_Type;

/**
 * Base class for UI objects that manage boolean/clickable multi-state data
 * @author John Turner
 *
 */
public abstract class Base_BooleanGUIObj extends Base_GUIObj {
	
	protected int state = 0;
	/**
	 * Build a boolean/multi-state button
	 * @param _ri render interface
	 * @param _objID the index of the object in the managing container
	 * @param _name the name/display label of the object
	 * @param _start the upper left corner of the hot spot for this object
	 * @param _end the lower right corner of the hot spot for this object
	 * @param _initVal the initial value setting for this object
	 * @param _objType the type of UI object this is
	 * @param _flags any preset configuration flags
	 * @param _off offset from label in x,y for placement of drawn ornamental box. make null for none
	 * @param strkClr stroke color of text
	 * @param fillClr fill color around text
	 */
	public Base_BooleanGUIObj(int _objID, String _name, myPointf _start, myPointf _end,
			double _initVal, GUIObj_Type _objType, boolean[] _flags, double[] _off, int[] strkClr, int[] fillClr) {
		super(_objID, _name, _start, _end, _objType, _flags, _off, strkClr, fillClr);
	}//ctor
	
	@Override
	public void resetToInit() {

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
		// TODO Auto-generated method stub
		return null;
	}

}//class Base_BooleanGUIObj