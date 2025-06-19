package base_UI_Objects.windowUI.uiObjs.renderer;

import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.renderer.base.Base_GUIObjRenderer;

/**
 * 
 */
public class SingleLineGUIObjRenderer extends Base_GUIObjRenderer {

	/**
	 * 
	 * @param _ri render interface
	 * @param _owner Gui object that owns this renderer
	 * @param _off offset for ornament
	 * @param _menuWidth the allowable width of the printable area. Single line UI objects will be this wide, 
	 * 						while multi line will be some fraction of this wide.
	 * @param _clrs array of stroke, fill and possibly text colors. If only 2 elements, text is idx 1.
	 * @param _guiFormatBoolVals array of boolean flags describing how the object should be constructed
	 * 		idx 0 : Should be multiline
	 * 		idx 1 : Text should be centered (default is false)
	 * 		idx 2 : Object should be rendered with outline (default for btns is true, for non-buttons is false)
	 * 		idx 3 : Should have ornament
	 * 		idx 4 : Ornament color should match label color
	 */
	public SingleLineGUIObjRenderer(IRenderInterface _ri, Base_GUIObj _owner,
			double[] _offset, float _menuWidth, int[][] _clrs, boolean[] _guiFormatBoolVals) {
		super(_ri, _owner, _offset, _menuWidth, _clrs, _guiFormatBoolVals, "Single Line");
	}

	@Override
	protected void _drawUIData(boolean isClicked) {
		ri.showText(owner.getUIDispAsSingleLine(), 0,0);
	}
		
	/**
	 * Return the maximum width of the owning UI object in the display.
	 * @return
	 */
	@Override
	public final float getMaxWidth() {
		return ri.getTextWidth(owner.getLabel() + owner.getValueAsString()) + this._ornament.getWidth();
	}
	
	/**
	 * Get the stroke and fill colors to use for a rectangle around the UI object. This renderer always uses the specified stroke and fill colors
	 * @return
	 */
	@Override
	protected final int[][] getRectStrkFillClr(boolean isClicked){ return rectStrkFillColor;}
	
	/**
	 * Recalculate the lower right location of the hotspot for the owning UI object.
	 * Single line always has same width.
	 * @param newStartPoint new upper left point
	 * @param lineHeight the height of a single line of text
	 * @param menuStartX the x coord of the start of the menu region
	 * @param menuWidth the possible display with for the object
	 * @return the next object's new start location
	 */
	@Override
	public myPointf reCalcHotSpot(myPointf newStart, float lineHeight, float menuStartX, float menuWidth) {
		start = new myPointf(newStart);
		end = new myPointf(menuWidth, start.y + lineHeight, start.z);
		// return the next object's start location
		return new myPointf(start.x, end.y, start.z);	
	}
	
	/**
	 * Update renderer when the state/label values change in the underlying UI object
	 */
	@Override	
	public final void updateFromObject() {
		
	}

}//class SingleLineGUIObjRenderer
