package base_UI_Objects.windowUI.uiObjs.menuObjs;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Type;
import base_UI_Objects.windowUI.uiObjs.menuObjs.base.Base_MenuGUIObj;

public class MenuGUIObj_Float extends Base_MenuGUIObj {

	public MenuGUIObj_Float(IRenderInterface _p, int _objID, String _name, double _xst, double _yst, double _xend,
			double _yend, double[] _minMaxMod, double _initVal, boolean[] _flags, double[] _Off) {
		super(_p, _objID, _name, _xst, _yst, _xend, _yend, _minMaxMod, _initVal, GUIObj_Type.FloatVal, _flags, _Off);
	}
	
	@Override
	public final double modVal(double mod){
		double oldVal = val;
		val += (mod*modMult);
		val = forceBounds(val);
		if (oldVal != val) {setIsDirty(true);}		
		return val;		
	}
	

	@Override
	protected void _drawObject_Indiv() {		p.showText(dispText + String.format("%.5f",val), 0,0);	}

}//class myGUIObj_Float
