package base_UI_Objects.windowUI.uiObjs.renderer;

import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.menuObjs.MenuGUIObj_Button;
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
	 * Length of the longest label in the state labels array
	 */
	protected float longestLabelLen;
	/**
	 * Width of button edge line
	 */
	protected final float lineWidth = 1.0f;
	
	/**
	 * Color of clicked button
	 */
	protected static final int[] clickedColor = {150,160,170,255};
	/**
	 * Colors of highlit and shadowed edges
	 */
	protected static final int[][] edgeColors = new int[][] {{255,255,255,255}, {55,55,55,255}};

	protected final float yUpOffLine;

	protected final float yLowOffLine;
	
	/**
	 * Base stroke(idx 0) and fill(idx 1) colors, short cut object for drawing rectangles
	 */
	protected int[][] rectClickStrkFillColor = new int[][] {{0,0,0,255},{150,160,170,255}};
	
	/**
	 * 
	 * @param _ri render interface
	 * @param _owner Gui object that owns this renderer
	 * @param _off offset for ornament
	 * @param _menuWidth the allowable width of the printable area. Single line UI objects will be this wide, 
	 * 						while multi line will be some fraction of this wide.
	 * @param _clrs array of stroke, fill and possibly text colors. If only 2 elements, text is idx 1.
	 * 			fill is ignored for this object
	 * @param _guiFormatBoolVals array of boolean flags describing how the object should be constructed
	 * 		idx 0 : Should be multiline
	 * 		idx 1 : Text should be centered (default is false)
	 * 		idx 2 : Object should be rendered with outline (default for btns is true, for non-buttons is false)
	 * 		idx 3 : Should have ornament
	 * 		idx 4 : Ornament color should match label color
	 * @param _labelColors color for each of the labels for this multi-state button
	 * @param _rendererType whether single or multi line renderer
	 */
	public ButtonGUIObjRenderer(IRenderInterface _ri, MenuGUIObj_Button _owner, double[] _offset, float _menuWidth, 
			int[][] _clrs, boolean[] _guiFormatBoolVals, int[][] _labelColors) {
		super(_ri, _owner, _offset, _menuWidth, _clrs,_guiFormatBoolVals, "Button Renderer");
		colors = new int[_labelColors.length][4];
		for(int i=0;i<_labelColors.length; ++i) {	System.arraycopy(_labelColors, 0, colors, 0, colors.length);}
		updateFromObject();
		// lines need the -yOffset because the UI data has been translated an extra yOffset already from base class
		yUpOffLine = -yOffset+lineWidth;
		yLowOffLine = -yOffset-lineWidth;
		System.arraycopy(strkClr, 0, rectClickStrkFillColor[0], 0, strkClr.length);		
	}
	
	protected int[] getStateColor() {return colors[((MenuGUIObj_Button) owner).getButtonState()];}	
	
	private void _drawButton(int onIdx, int offIdx) {
		float[] dims = getHotSpotDims();
		
		// draw 3d button edges			
		ri.setStrokeWt(lineWidth);
		ri.setStroke(strkClr, strkClr[3]);
		
		//bottom/right
		ri.setStroke(edgeColors[onIdx], edgeColors[onIdx][3]);
		ri.drawLine(lineWidth, yLowOffLine+dims[1], 0, dims[0]-lineWidth, yLowOffLine+dims[1], 0);//bottom line
		ri.drawLine(dims[0]-lineWidth, yUpOffLine, 0, dims[0]-lineWidth, yLowOffLine+dims[1], 0);//right side line
		//top/left
		ri.setStroke(edgeColors[offIdx], edgeColors[offIdx][3]);
		ri.drawLine(lineWidth, yUpOffLine, 0, dims[0]-lineWidth, yUpOffLine, 0); // top line
		ri.drawLine(lineWidth, yUpOffLine, 0, lineWidth, yLowOffLine+dims[1], 0);		
	}//_drawButton

	@Override
	protected void _drawUIData(boolean isClicked) {
		ri.pushMatState();
			if(isClicked) {			_drawButton(0, 1); } 
			else {					_drawButton(1, 0);}
		ri.popMatState();
		// show Button Text		
		ri.showText(owner.getValueAsString(), yOffset , 0);
		//ri.showCenteredText(owner.getValueAsString(), (end.x - start.x)*.5f , 0);
	}
	
	/**
	 * TODO come up with a mechanism to perform this - it must be aware and able to modify previous button's hotspot. 
	 * For now, this is done in UIObjectManager. Or, conversely, get rid of this being in the renderer
	 * 
	 * @param newStartPoint new upper left point proposal.
	 * @param lineHeight the height of a single line of text
	 * @param menuStartX the x coord of the start of the menu region
	 * @param menuWidth 
	 * @return the next object's new start location
	 */
	@Override
	public myPointf reCalcHotSpot(myPointf newStart, float lineHeight, float menuStartX, float menuWidth) {	return new myPointf(newStart);}

	@Override
	public float getMaxWidth() {return yOffset+ longestLabelLen + _ornament.getWidth();}

	/**
	 * Update renderer when the state values change in the underlying button (i.e. the longest label needs to be re-determined)
	 */
	@Override	
	public final void updateFromObject() {
		// update the width
		float longLbl = 0, curLblLen;
		String[] stateLabels = ((MenuGUIObj_Button) owner).getStateLabels();
		for(String label : stateLabels) {
			curLblLen = ri.getTextWidth(label);
			longLbl = (longLbl < curLblLen ? curLblLen : longLbl);
		}
		longestLabelLen = longLbl;		
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
