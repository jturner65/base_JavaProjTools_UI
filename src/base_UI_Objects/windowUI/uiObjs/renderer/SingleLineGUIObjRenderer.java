package base_UI_Objects.windowUI.uiObjs.renderer;

import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;
import base_UI_Objects.windowUI.uiObjs.renderer.base.Base_GUIObjRenderer;

/**
 * 
 */
public class SingleLineGUIObjRenderer extends Base_GUIObjRenderer {

	/**
	 * Build a single-line renderer
	 * @param _ri render interface
	 * @param _owner Gui object that owns this renderer
	 * @param _off offset for ornament
	 * @param _menuWidth the allowable width of the printable area. Single line UI objects will be this wide, 
	 * 						while multi line will be some fraction of this wide.
	 * @param _argObj GUIObjParams that describe colors, render format and other components of the owning gui object
	 */
	public SingleLineGUIObjRenderer(IRenderInterface _ri, Base_GUIObj _owner, double[] _offset, float _menuWidth, GUIObj_Params _argObj) {
		super(_ri, _owner, _offset, _menuWidth, _argObj, "Single Line");
	}

	@Override
	protected void _drawUIData(boolean isClicked) {			ri.showText(owner.getUIDispAsSingleLine(), 0,0);}
	@Override
	protected void _drawUIDataCentered(boolean isClicked) {	ri.showCenteredText(owner.getUIDispAsSingleLine(), _getCenterX(), 0);}
		
	/**
	 * Return the max width feasible for this UI object's text (based on possible values + label length if any)
	 * @return
	 */
	@Override
	public final float getMaxTextWidth() {
		return ri.getTextWidth(owner.getLabel() + owner.getValueAsString()) + this._ornament.getWidth();
	}
	/**
	 * Return the # of text lines the owning object will need to render
	 * @return
	 */
	@Override
	public final int getNumTextLines() {		return 1;	}
	
	/**
	 * Get the stroke and fill colors to use for a rectangle around the UI object. 
	 * @return
	 */
	@Override
	protected final int[][] getRectStrkFillClr(boolean isClicked){ 
		if(isClicked) {			return hlRectStrkFillColor;		}
		return rectStrkFillColor;
	}

	/**
	 * Update renderer when the state/label values change in the underlying UI object
	 */
	@Override	
	public final void updateFromObject() {}
}//class SingleLineGUIObjRenderer
