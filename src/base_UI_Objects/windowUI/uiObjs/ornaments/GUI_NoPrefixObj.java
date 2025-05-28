package base_UI_Objects.windowUI.uiObjs.ornaments;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.ornaments.base.Base_GUIPrefixObj;

/**
 * Class for no prefix ornament to be displayed
 * @author John Turner
 */
public class GUI_NoPrefixObj extends Base_GUIPrefixObj{

	public GUI_NoPrefixObj() {super();}
	/**
	 * Nothing to draw
	 */
	@Override
	public void drawPrefixObj(IRenderInterface ri) {}
	@Override
	public float getWidth() {		return 0;	}
	@Override
	public float getHeight() {	return 0;	}
	
}//class GUI_NoPrefixOrnament