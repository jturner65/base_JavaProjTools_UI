package base_UI_Objects.windowUI.uiObjs.miscObjs.base;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.Base_NumericGUIObj;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Type;

/**
 * Class for UI Objects that do not draw prefix box ornament
 * @author John Turner
 *
 */
public abstract class Base_MiscGUIObj extends Base_NumericGUIObj {

	/**
	 * 
	 * @param _p
	 * @param _objID
	 * @param _name
	 * @param _xst
	 * @param _yst
	 * @param _xend
	 * @param _yend
	 * @param _minMaxMod
	 * @param _initVal
	 * @param _objType
	 * @param _flags
	 * @param _off
	 */
	public Base_MiscGUIObj(IRenderInterface _p, int _objID, String _name, double _xst, double _yst, double _xend,
			double _yend, double[] _minMaxMod, double _initVal, GUIObj_Type _objType, boolean[] _flags, double[] _off) {
		super(_p, _objID, _name, _xst, _yst, _xend, _yend, _minMaxMod, _initVal, _objType, _flags);
	}

	@Override
	public final void drawPrefixObj() {}//drawPrefixObj
}//class Base_MiscGUIObj
