package base_UI_Objects.windowUI.uiObjs.renderer.ornaments.base;

import base_Render_Interface.IRenderInterface;

/**
 * Class to describe a possible UI element that acts like an ornament in front of a UI control
 * @author John Turner
 *
 */
public abstract class Base_GUIPrefixObj{
    public Base_GUIPrefixObj() {}
    
    public abstract void drawPrefixObj(IRenderInterface ri);
    
    public abstract void setYCenter(float _y);
    
    public abstract float getWidth();
    public abstract float getHeight();
    
}//class Base_GUIOrnament