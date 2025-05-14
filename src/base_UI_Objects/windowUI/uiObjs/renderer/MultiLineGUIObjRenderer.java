package base_UI_Objects.windowUI.uiObjs.renderer;

import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.renderer.base.Base_GUIObjRenderer;

/**
 * 
 */
public class MultiLineGUIObjRenderer extends Base_GUIObjRenderer {

	/**
	 * @param _ri
	 * @param _owner
	 * @param _off
	 * @param _strkClr
	 * @param _fillClr
	 * @param buildPrefix
	 * @param matchLabelColor
	 */
	public MultiLineGUIObjRenderer(IRenderInterface _ri, Base_GUIObj _owner, double[] _offset, 
			float _menuWidth, int[] _strkClr, int[] _fillClr, boolean buildPrefix, boolean matchLabelColor) {
		super(_ri, _owner, _offset, _menuWidth, _strkClr, _fillClr, buildPrefix, matchLabelColor, "Multi-Line");
	}

	@Override
	protected void _drawUIData() {
		ri.showTextAra(0, owner.getUIDispAsMultiLine());	
	}
	/**
	 * Return the maximum width of the owning UI object in the display.
	 * For multi-line-rendered UI objects, this will be the largest of 
	 * the label's width in pixels and the value as a string's width in pixels.
	 * 
	 * TODO : possibly support more than 2 lines?
	 * @return
	 */
	public final float getMaxWidth() {
		float labelWidth = ri.getTextWidth(owner.getLabel());
		float dispWidth = ri.getTextWidth(owner.getValueAsString());
		return (labelWidth > dispWidth ? labelWidth : dispWidth);
	}	
	
	/**
	 * Recalculate the lower right location of the hotspot for the owning UI object
	 * Multi-line will be the width of the longest of either the label or the data, and 2 text lines high. 
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
		//TODO change this if more than 2 lines for multi-line UI object
		float newLineHeight = 2.0f * lineHeight;
		
		// Calculate the width of the text of the largest of 2 strings, the displayed label or
		// the displayed value as a string, and use this to calculate the new end point x value, making sure 
		// it is smaller than the menuWidth to determine whether the UI object needs to be on a new line or not.
		float textWidth = getMaxWidth();
		boolean newLine = ((start.x + textWidth) > (menuStartX + menuWidth));
		if (newLine) {
			// On a new line : move start to next UI object location in Y and start x value at beginning of menu area
			start.x = menuStartX;
			start.y += newLineHeight;
		} 
		// End point x is width of text further than start x, lineHeight further than start y text
		end = new myPointf(start.x + textWidth, start.y + newLineHeight, start.z);
		// return the next object's start location
		return new myPointf(end.x, end.y, start.z);	
	}

}//MultiLineGUIObjRenderer
