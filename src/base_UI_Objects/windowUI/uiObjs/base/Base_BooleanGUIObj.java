package base_UI_Objects.windowUI.uiObjs.base;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.base.Base_GUIObj;

/**
 * Base class for UI objects that manage boolean/clickable multi-state data
 * @author John Turner
 *
 */
public abstract class Base_BooleanGUIObj extends Base_GUIObj {
	
	/**
	 * Build a boolean/multi-state button
	 * @param _p
	 * @param _objID
	 * @param _name
	 * @param _xst
	 * @param _yst
	 * @param _xend
	 * @param _yend
	 * @param _objType
	 * @param _flags
	 * @param _off
	 */
	public Base_BooleanGUIObj(IRenderInterface _p, int _objID, String _name, double _xst, double _yst, double _xend,
			double _yend, GUIObj_Type _objType, boolean[] _flags, double[] _off) {
		super(_p, _objID, _name, _xst, _yst, _xend, _yend, _objType, _flags);
		// TODO Auto-generated constructor stub
	}//ctor

	@Override
	public void resetToInit() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void drawPrefixObj() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void _drawObject_Indiv() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setValueFromString(String str) {
		// TODO Auto-generated method stub

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
