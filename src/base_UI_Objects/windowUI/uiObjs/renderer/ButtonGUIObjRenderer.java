package base_UI_Objects.windowUI.uiObjs.renderer;

import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.menuObjs.MenuGUIObj_Button;
import base_UI_Objects.windowUI.uiObjs.renderer.base.Base_GUIObjRenderer;

/**
 * 
 */
public class ButtonGUIObjRenderer extends Base_GUIObjRenderer {
	// 1 4-element color per supported state
	protected int[][] colors;
	
	protected final int[] clickedColor = {150,150,150,255};
	
	/**
	 * Length of the longest label in the state labels array
	 */
	protected float longestLabelLen;
	
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
	}
	
	protected int[] getStateColor() {return colors[((MenuGUIObj_Button) owner).getButtonState()];}
	
	protected final int[][] onColors = new int[][] {{255,255,255,255}, {55,55,55,255}};
	protected final int[][] offColors = new int[][] {{55,55,55,255},{255,255,255,255}};	
	/**
	 * Need to use this since we already have moved to start.x/y
	 */
	protected final float lineWidth = 1.0f;

	@Override
	protected void _drawUIData(boolean isClicked) {
		ri.pushMatState();
			ri.setStrokeWt(1.0f);
			ri.setStroke(strkClr, strkClr[3]);
			int[] clr = isClicked ? clickedColor : getStateColor();
			ri.setFill(clr, clr[3]);
			float[] dims = getHotSpotDims();
			// draw primary rectangle
			ri.drawRect(0,-yOffset, dims[0], dims[1]);
			// draw edges 
			int[][] clrs = isClicked ?  onColors : offColors;
			
			float yUpOffLine = -yOffset+lineWidth;
			float yLowOffLine = -yOffset+dims[1]-lineWidth;

			ri.setStrokeWt(lineWidth);
			ri.setStroke(clrs[0], clrs[0][3]);
			ri.drawLine(lineWidth, yLowOffLine, 0, dims[0]-lineWidth, yLowOffLine, 0);//bottom line
			ri.drawLine(dims[0]-lineWidth, yUpOffLine, 0, dims[0]-lineWidth, yLowOffLine, 0);//right side line
			
			ri.setStroke(clrs[1], clrs[1][3]);
			ri.drawLine(lineWidth, yUpOffLine, 0, dims[0]-lineWidth, yUpOffLine, 0); // top line
			ri.drawLine(lineWidth, yUpOffLine, 0, lineWidth, yLowOffLine, 0);
		ri.popMatState();
		// show Button Text
		ri.showText(owner.getValueAsString(), yOffset, 0);
	}
	/**
	 * TODO come up with a mechanism to perform this - it must be aware and able to modify previous button's hotspot. 
	 * For now, this is done in UIObjectManager.
	 * 
	 * Recalculate the lower right location of the hotspot for the owning UI object
	 * Buttons are dependent on the size of their neighbors for their own size, shrinking or stretching to fit, 
	 * depending on the space available, so this function will not successfully automate this process for buttons, 
	 * and instead should be used to set the start and end points for the button after the values have been already
	 * calculated.
	 * 
	 * @param newStartPoint new upper left point proposal.
	 * @param lineHeight the height of a single line of text
	 * @param menuStartX the x coord of the start of the menu region
	 * @param menuWidth 
	 * @return the next object's new start location
	 */
	@Override
	public myPointf reCalcHotSpot(myPointf newStart, float lineHeight, float menuStartX, float menuWidth) {
		return new myPointf(newStart);	
	}

	@Override
	public float getMaxWidth() {return yOffset+ longestLabelLen + _ornament.getWidth();}
	/**
	 * Whether the gui object this renderer manages is multi-line or single line
	 * @return
	 */
	@Override
	public boolean isMultiLine() {return false;	}
	/**
	 * When the state values change in the underlying button, the longest label needs to be re-determined
	 */
	@Override
	public void updateWidth() {
		float longLbl = 0, curLblLen;
		String[] stateLabels = ((MenuGUIObj_Button) owner).getStateLabels();
		for(String label : stateLabels) {
			curLblLen = ri.getTextWidth(label);
			longLbl = (longLbl < curLblLen ? curLblLen : longLbl);
		}
		longestLabelLen = longLbl;		
	}
}//class ButtonGUIObjRenderer<E
