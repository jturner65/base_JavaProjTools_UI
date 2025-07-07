package base_UI_Objects.windowUI.uiObjs.renderer;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;
import base_UI_Objects.windowUI.uiObjs.renderer.base.Base_GUIObjRenderer;

/**
 * 
 */
public class MultiLineGUIObjRenderer extends Base_GUIObjRenderer {

    /**
     * Build a multi-line renderer
     * @param _ri render interface
     * @param _owner Gui object that owns this renderer
     * @param _off offset for ornament
     * @param _argObj GUIObjParams that describe colors, render format and other components of the owning gui object
     */
    public MultiLineGUIObjRenderer(IRenderInterface _ri, Base_GUIObj _owner, double[] _offset, GUIObj_Params _argObj) {
        super(_ri, _owner, _offset,  _argObj, "Multi-Line");
    }

    @Override
    protected void _drawUIData() {            ri.showTextAra(0, 0, owner.getUIDispAsMultiLine());}
    @Override
    protected void _drawUIDataCentered() {    ri.showCenteredTextAra(_getCenterX(), 0, owner.getUIDispAsMultiLine());}
    
    /**
     * Return the max width feasible for this UI object's text (based on possible values + label length if any)
     * TODO : possibly support more than 2 lines?
     * @return
     */
    @Override
    public final float getMaxTextWidth() {
        var vals = owner.getUIDispAsMultiLine();
        float labelWidth = ri.getTextWidth(vals[0]) + _ornament.getWidth();
        float dispWidth = ri.getTextWidth(vals[1]);
        return 1.01f * (labelWidth > dispWidth ? labelWidth : dispWidth);
    }    
    /**
     * Return the # of text lines the owning object will need to render. 
     * TODO : possibly support more than 2 lines?
     * @return
     */
    @Override
    public final int getNumTextLines() {        return 2;    }
    
    /**
     * Get the stroke and fill colors to use for a rectangle around the UI object.
     * @return
     */
    @Override
    protected final int[][] getRectStrkFillClr(boolean isClicked){ 
        if(isClicked) {            return hlRectStrkFillColor;        }
        return rectStrkFillColor;
    }
        
    /**
     * Update renderer when the state/label values change in the underlying UI object
     */
    @Override    
    public final void updateFromObject() {}
}//MultiLineGUIObjRenderer
