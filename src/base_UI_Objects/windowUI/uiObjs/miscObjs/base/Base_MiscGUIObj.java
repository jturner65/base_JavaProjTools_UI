package base_UI_Objects.windowUI.uiObjs.miscObjs.base;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Type;

/**
 * Class for UI Objects that do not draw prefix box ornament
 * @author John Turner
 *
 */
public abstract class Base_MiscGUIObj extends Base_GUIObj {

	public Base_MiscGUIObj(IRenderInterface _p, int _objID, String _name, double _xst, double _yst, double _xend,
			double _yend, double[] _minMaxMod, double _initVal, GUIObj_Type _objType, boolean[] _flags, double[] _off) {
		super(_p, _objID, _name, _xst, _yst, _xend, _yend, _minMaxMod, _initVal, _objType, _flags, _off);
	}

	@Override
	public void draw() {
		p.pushMatState();
			p.translate(start.x,start.y,0);
			p.setFill(_cVal,255);
			p.setStroke(_cVal,255);			
			//draw specifics for this UI object
			_drawIndiv();
		p.popMatState();
	}//draw
}//class Base_MiscGUIObj
