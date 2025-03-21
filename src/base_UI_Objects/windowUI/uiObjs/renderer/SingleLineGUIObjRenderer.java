package base_UI_Objects.windowUI.uiObjs.renderer;

import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.renderer.base.Base_GUIObjRenderer;

/**
 * 
 */
public class SingleLineGUIObjRenderer extends Base_GUIObjRenderer {

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
	public SingleLineGUIObjRenderer(IRenderInterface _ri, Base_GUIObj _owner, myPointf _start, myPointf _end,
			double[] _off, int[] _strkClr, int[] _fillClr, boolean buildPrefix, boolean matchLabelColor) {
		super(_ri, _owner, _start, _end, _off, _strkClr, _fillClr, buildPrefix, matchLabelColor);
	}

	@Override
	protected void _drawUIData() {
		ri.showText(owner.getLabel() + owner.getValueAsString(), 0,0);	

	}

}//class SingleLineGUIObjRenderer
