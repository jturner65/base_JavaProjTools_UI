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
	
	protected final float yUpOffLine;
	protected final float yLowOffLine;

	/**
	 * 
	 * @param _ri
	 * @param _owner
	 * @param _off
	 * @param _menuWidth
	 * @param _txtColor color for outline and text
	 * @param _colors fill colors to render for each state of the button being rendered
	 */
	public ButtonGUIObjRenderer(IRenderInterface _ri, MenuGUIObj_Button _owner, double[] _offset, float _menuWidth, int[] _txtColor, int[][] _labelColors) {
		super(_ri, _owner, _offset, _menuWidth, _txtColor, _txtColor,false, false, "Button Renderer");
		colors = new int[_labelColors.length][4];
		for(int i=0;i<_labelColors.length; ++i) {	System.arraycopy(_labelColors, 0, colors, 0, colors.length);}
		updateWidth();
		yUpOffLine = -yOffset+lineWidth;
		yLowOffLine = -yOffset-lineWidth;
	}
	
	protected int[] getStateColor() {return colors[((MenuGUIObj_Button) owner).getButtonState()];}	
	
	protected static final int[] clickedColor = {150,160,170,255};
	protected static final int[][] edgeColors = new int[][] {{255,255,255,255}, {55,55,55,255}};
	/**
	 * Need to use this since we already have moved to start.x/y
	 */
	protected final float lineWidth = 1.0f;
	
	private void _drawButton(int[] clr, int onIdx, int offIdx) {
		ri.setStrokeWt(1.0f);
		ri.setStroke(strkClr, strkClr[3]);
		ri.setFill(clr, clr[3]);
		float[] dims = getHotSpotDims();
		// draw primary rectangle
		ri.drawRect(0,-yOffset, dims[0], dims[1]);
		
		// draw 3d button edges			
		ri.setStrokeWt(lineWidth);
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
			if(isClicked) {			_drawButton(clickedColor, 0, 1); 
			} else {					_drawButton(getStateColor(), 1,0);}
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
