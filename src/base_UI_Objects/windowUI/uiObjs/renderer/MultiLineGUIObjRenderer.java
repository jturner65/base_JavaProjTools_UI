package base_UI_Objects.windowUI.uiObjs.renderer;

import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.renderer.base.Base_GUIObjRenderer;

/**
 * 
 */
public class MultiLineGUIObjRenderer extends Base_GUIObjRenderer {

	/**
	 * @param _ri
	 * @param _owner
	 * @param _start
	 * @param _end
	 * @param _off
	 * @param _strkClr
	 * @param _fillClr
	 * @param buildPrefix
	 * @param matchLabelColor
	 */
	public MultiLineGUIObjRenderer(IRenderInterface _ri, Base_GUIObj _owner, myPointf _start, myPointf _end,
			double[] _offset, float _menuWidth, int[] _strkClr, int[] _fillClr, boolean buildPrefix, boolean matchLabelColor) {
		super(_ri, _owner, _start, _end, _offset, _menuWidth, _strkClr, _fillClr, buildPrefix, matchLabelColor);
	}

	@Override
	protected void _drawUIData() {
		ri.showTextAra(0,new String[] {owner.getLabel(), owner.getValueAsString()});	
	}

}//MultiLineGUIObjRenderer
