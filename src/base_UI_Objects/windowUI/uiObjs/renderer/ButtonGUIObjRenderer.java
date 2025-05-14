/**
 * 
 */
package base_UI_Objects.windowUI.uiObjs.renderer;

import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.menuObjs.MenuGUIObj_Button;
import base_UI_Objects.windowUI.uiObjs.renderer.base.Base_GUIObjRenderer;

/**
 * 
 */
public class ButtonGUIObjRenderer<E extends Enum<E>> extends Base_GUIObjRenderer {
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
	 * @param _colors fill colors to render for each state
	 * @param _txtColor color for outline and text
	 */
	public ButtonGUIObjRenderer(IRenderInterface _ri, MenuGUIObj_Button _owner, double[] _off, float _menuWidth,
			int[][] _colors, int[] _txtColor) {
		super(_ri, _owner, _off, _menuWidth, _txtColor, _txtColor,false, false, "Button Renderer");
		colors = new int[_colors.length][];
		for(int i=0;i<_colors.length; ++i) {	colors[i] = _colors[i];		}
		float longLbl = 0, curLblLen;
		String[] stateLabels = ((MenuGUIObj_Button) owner).getStateLabels();
		for(String label : stateLabels) {
			curLblLen = ri.getTextWidth(label);
			longLbl = (longLbl < curLblLen ? curLblLen : longLbl);
		}
		longestLabelLen = longLbl;
	}

	protected int[] getStateColor() {
		return colors[((MenuGUIObj_Button) owner).getButtonState()];
	}

	@Override
	protected void _drawUIData() {
		ri.pushMatState();
			ri.setStrokeWt(1.0f);
			ri.setStroke(strkClr, strkClr[3]);
			int[] clr = getStateColor();
			ri.setFill(clr, clr[3]);
			_drawRectangle();	
		ri.popMatState();
		// show Button Text
		ri.showText(owner.getLabel(), 0, 0);
	}
	
	/**
	 * Recalculate the lower right location of the hotspot for the owning UI object
	 * Buttons are dependent on the size of their neighbors for their own size, shrinking or stretching to fit, 
	 * depending on the space available, so this function will not successfully automate this process for buttons, 
	 * and instead should be used to set the start and end points for the button after the values have been already
	 * calculated.
	 * 
	 * @param newStartPoint new upper left point proposal.
	 * @param buttonHeight the height of the button
	 * @param buttonWidth the width of the button
	 * @param _notUsed 
	 * @return the next object's new start location
	 */
	@Override
	public myPointf reCalcHotSpot(myPointf newStart, float buttonHeight, float buttonWidth, float _notUsed) {
		//set based on passed new start
		start = new myPointf(newStart);		
		// End point x is width of text further than start x, lineHeight further than start y text
		end = new myPointf(start.x + buttonWidth, start.y + buttonHeight, start.z);
		// return the next object's start location
		return new myPointf(end.x, end.y, start.z);	
	}

	@Override
	public float getMaxWidth() {return longestLabelLen;}

}//class ButtonGUIObjRenderer<E
