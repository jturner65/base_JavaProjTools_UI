package base_UI_Objects.windowUI.uiObjs.base.ornaments.base;

import base_Render_Interface.IRenderInterface;

/**
 * Class to describe a possible UI element that acts like an ornament in front of a UI control
 * @author John Turner
 *
 */
public abstract class Base_GUIPrefixObj{
	public Base_GUIPrefixObj() {}
	
	public abstract void drawPrefixObj(IRenderInterface ri);
}//class Base_GUIOrnament