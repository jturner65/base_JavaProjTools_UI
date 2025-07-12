package base_UI_Objects.windowUI.uiObjs.renderer;

import base_Render_Interface.IGraphicsAppInterface;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;
import base_UI_Objects.windowUI.uiObjs.menuObjs.buttons.GUIObj_Button;
import base_UI_Objects.windowUI.uiObjs.renderer.base.Base_GUIObjRenderer;

/**
 * 
 */
public class ButtonGUIObjRenderer extends Base_GUIObjRenderer {
    /**
     *  1 4-element color per supported state
     */
    protected int[][] colors;
    
    /**
     * Base stroke(idx 0) and fill(idx 1) colors, short cut object for drawing rectangles when clicked
     */
    protected int[][] rectClickStrkFillColor = new int[][] {{0,0,0,255},{150,160,170,255}};
    
    /**
     * Build a button/switch renderer object
     * @param _ri render interface
     * @param _owner Gui object that owns this renderer
     * @param _off offset for ornament
     * @param _menuWidth the allowable width of the printable area. Single line UI objects will be this wide, 
     *                         while multi line will be some fraction of this wide.
     * @param _argObj GUIObjParams that describe colors, render format and other components of the owning gui object
     */
    public ButtonGUIObjRenderer(IGraphicsAppInterface _ri, GUIObj_Button _owner, double[] _offset, GUIObj_Params _argObj) {
        super(_ri, _owner, _offset, _argObj, "Button Renderer");
        _updateLabelColors( _argObj.getBtnFillColors());
        updateFromObject();
        //copy stroke color into convenience array
        System.arraycopy(strkClr, 0, rectClickStrkFillColor[0], 0, strkClr.length);        
    }
    
    protected int[] getStateColor() {return colors[((GUIObj_Button) owner).getButtonState()];}    

    @Override
    protected void _drawUIData() {            ri.showText(owner.getUIDispAsSingleLine(), txtHeight , 0);}    
    @Override
    protected void _drawUIDataCentered() {    ri.showCenteredText(owner.getUIDispAsSingleLine(), _getCenterX(), 0);}
    
    protected void _updateLabelColors(int[][] _labelColors) {
        colors = new int[_labelColors.length][4];
        for(int i=0;i<_labelColors.length; ++i) {    System.arraycopy(_labelColors, 0, colors, 0, colors.length);}   
    }
    
    /**
     * Return the max width feasible for this UI object's text (based on possible values + label length if any)
     * @return
     */
    @Override
    public final float getMaxTextWidth() {return txtHeight + longestTextLine + _ornament.getWidth();}

    /**
     * Return the # of text lines the owning object will need to render. 
     * @return
     */
    @Override
    public final int getNumTextLines() {        return 1;    }
    
    /**
     * Update renderer when the state values change in the underlying button (i.e. the longest label needs to be re-determined)
     */
    @Override    
    public final void updateFromObject() {
        // update the width
        float longLbl = 0, curLblLen;
        String[] stateLabels = ((GUIObj_Button) owner).getStateLabels();
        for(String label : stateLabels) {
            curLblLen = ri.getTextWidth(label);
            longLbl = (longLbl < curLblLen ? curLblLen : longLbl);
        }
        longestTextLine = longLbl;        
    }//updateFromObject

    @Override
    protected int[][] getRectStrkFillClr(boolean isClicked) {
        if(isClicked) {
            return rectClickStrkFillColor;
        } else {
            return new int[][] { rectClickStrkFillColor[0], getStateColor()};
        }        
    }
}//class ButtonGUIObjRenderer<E
