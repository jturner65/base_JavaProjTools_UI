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
	 * Number of buttons to try to fit per line
	 */
	protected final float numBtnsPerLine;
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
			curLblLen = ri.textWidth(label);
			longLbl = (longLbl < curLblLen ? curLblLen : longLbl);
		}
		longestLabelLen = longLbl;
		//TODO support more buttons per line
		numBtnsPerLine = 2.0f;
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
		ri.showText(owner.getLabel(), 0, 0);
	}
	
	/**
	 * Recalculate the lower right location of the hotspot for the owning UI object
	 * Button obj will be the width of the label, and 1 text line high. 
	 * Needs to be moved to a new line if will not fit in currently specified menuWidth
	 * @param newStartPoint new upper left point proposal. Might be changed if moved to a new line
	 * @param lineHeight the height of a single line of text
	 * @param menuStartX the x coord of the start of the menu region
	 * @param menuWidth the possible display with for the object
	 * @return the next object's new start location
	 */
	@Override
	public myPointf reCalcHotSpot(myPointf newStart, float lineHeight, float menuStartX, float menuWidth) {
		//set based on passed new start
		start = new myPointf(newStart);
		// max text width is width of longest label
		float textWidth = getMaxWidth();
		
		// End point x is width of text further than start x, lineHeight further than start y text
		end = new myPointf(start.x + textWidth, start.y + lineHeight, start.z);
		// return the next object's start location
		return new myPointf(end.x, end.y, start.z);	
	}

	@Override
	public float getMaxWidth() {
		return longestLabelLen;
	}

}
