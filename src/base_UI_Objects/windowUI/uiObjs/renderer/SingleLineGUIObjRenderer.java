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
	 * @param _ri
	 * @param _owner
	 * @param _start
	 * @param _end
	 * @param _off
	 * @param _strkClr
	 * @param _fillClr
	 * @param buildPrefix
	 * @param matchLabelColor
	 */
	public SingleLineGUIObjRenderer(IRenderInterface _ri, Base_GUIObj _owner,
			double[] _offset, float _menuWidth, int[] _strkClr, int[] _fillClr, boolean buildPrefix, boolean matchLabelColor) {
		super(_ri, _owner, _offset, _menuWidth, _strkClr, _fillClr, buildPrefix, matchLabelColor, "Single Line");
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
	 * Whether the gui object this renderer manages is multi-line or single line
	 * @return
	 */
	@Override
	public boolean isMultiLine() {return false;	}
	
	// No need to do this, the width is recalculated on every call
	@Override
	public void updateWidth() {}

}//class SingleLineGUIObjRenderer
