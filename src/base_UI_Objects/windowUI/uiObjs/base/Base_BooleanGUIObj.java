package base_UI_Objects.windowUI.uiObjs.base;

import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.base.GUIObj_Type;

/**
 * Base class for UI objects that manage boolean/clickable multi-state data
 * @author John Turner
 *
 */
public abstract class Base_BooleanGUIObj extends Base_GUIObj {
	
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
	 */
	public Base_BooleanGUIObj(IRenderInterface _ri, int _objID, String _name, myPointf _start, myPointf _end,
			double _initVal, GUIObj_Type _objType, boolean[] _flags, double[] _off) {
		super(_ri, _objID, _name, _start, _end, _objType, _flags, _off);
	}//ctor

	
	@Override
	public void resetToInit() {

	}

	@Override
	protected void _drawObject_Indiv() {

	}

	@Override
	protected void setValueFromString(String str) {
	}

	@Override
	protected String getValueAsString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String[] getStrDataForVal() {
		// TODO Auto-generated method stub
		return null;
	}

}//class Base_BooleanGUIObj
