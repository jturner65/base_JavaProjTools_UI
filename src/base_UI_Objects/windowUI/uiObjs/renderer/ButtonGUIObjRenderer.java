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
	
	/**
	 * Length of the longest label in the state labels array
	 */
	protected final float longestLabelLen;
	
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
		float longLbl = 0, curLblLen;
		String[] stateLabels = ((MenuGUIObj_Button) owner).getStateLabels();
		for(String label : stateLabels) {
			curLblLen = ri.getTextWidth(label);
			longLbl = (longLbl < curLblLen ? curLblLen : longLbl);
		}
		longestLabelLen = longLbl;
	}
	
	protected int[] getStateColor() {return colors[((MenuGUIObj_Button) owner).getButtonState()];}
	
	/**
	 * Need to use this since we already have moved to start.x/y
	 */
	protected void _drawButtonRect() { ri.drawRect(0,-yOffset, end.x-start.x, end.y-start.y);}

	@Override
	protected void _drawUIData() {
		ri.pushMatState();
			ri.setStrokeWt(1.0f);
			ri.setStroke(strkClr, strkClr[3]);
			int[] clr = getStateColor();
			ri.setFill(clr, clr[3]);
			_drawButtonRect();	
		ri.popMatState();
		// show Button Text
		ri.showText(owner.getValueAsString(), 0, 0);
	}
	
	private static boolean isFirstButton = true;
	/**
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
		//set based on passed new start
		float halfWidth =  menuWidth*.5f;
		float[] startXendX = (isFirstButton ? new float[] {newStart.x, halfWidth} : new float[] {halfWidth, menuWidth} );
		start = new myPointf(startXendX[0],newStart.y, newStart.z);
		float buttonHeight = 1.1f * lineHeight;
		// End point x is width of text further than start x, lineHeight further than start y text
		end = new myPointf(startXendX[1], start.y + buttonHeight, start.z);
		// return the next object's start location
		float nextPtY = (isFirstButton ? start.y : end.y);
		isFirstButton = !isFirstButton;
		return new myPointf(newStart.x, nextPtY, start.z);	
	}

	@Override
	public float getMaxWidth() {return longestLabelLen;}
	/**
	 * Whether the gui object this renderer manages is multi-line or single line
	 * @return
	 */
	@Override
	public boolean isMultiLine() {return false;	}

}//class ButtonGUIObjRenderer<E
