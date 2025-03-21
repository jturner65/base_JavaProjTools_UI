package base_UI_Objects.windowUI.uiObjs.renderer.base;

import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.ornaments.GUI_NoPrefixObj;
import base_UI_Objects.windowUI.uiObjs.base.ornaments.GUI_PrefixObj;
import base_UI_Objects.windowUI.uiObjs.base.ornaments.base.Base_GUIPrefixObj;

public abstract class Base_GUIObjRenderer {
	/**
	 * Interface to drawing/graphics engine
	 */
	protected static IRenderInterface ri;
	
	/**
	 * Object to either manage and display or not show an ornamental box in front of a UI element
	 */
	private final Base_GUIPrefixObj _ornament;
	/**
	 * Fill color value for main UI object
	 */
	protected int[] fillClr = new int[] {0,0,0,255};
	
	/**
	 * Stroke color value for main UI object
	 */
	protected int[] strkClr = new int[] {0,0,0,255};
	/**
	 * x,y coords of top left corner for clickable region
	 */
	protected final myPointf start;
	/**
	 * x,y coords of bottom right corner for clickable region
	 */
	protected final myPointf end;
	/**
	 * Used to draw this UI object encapsulated by a border representing the
	 * click region this UI element will respond to, for debug
	 */
	private int _animCount = 0;
	private boolean _cyanStroke = false;
	
	/**
	 * Owning object consuming this renderer
	 */
	protected Base_GUIObj owner;
	
	/**
	 * @param _ri render interface
	 * @param _start the upper left corner of the hot spot for this object
	 * @param _end the lower right corner of the hot spot for this object
	 * @param _off offset before text
	 * @param strkClr stroke color of text
	 * @param fillClr fill color around text
	 * @param buildPrefix whether to build a prefix ornament or not
	 * @param matchLabelColor whether the built prefix ornament should match the object's color
	 */
	public Base_GUIObjRenderer (
			IRenderInterface _ri, Base_GUIObj _owner, myPointf _start, myPointf _end, 
			double[] _off, int[] _strkClr, int[] _fillClr,  
			boolean buildPrefix, 
			boolean matchLabelColor) {
		ri=_ri;	
		owner = _owner;
		// location
		start = _start;
		end = _end;
		// stroke color and fill color of text
		strkClr = _strkClr;
		fillClr = _fillClr;
		//build prefix ornament to display
		if (buildPrefix && (_off != null)) {
			int[] prefixClr = (matchLabelColor ? _fillClr : ri.getRndClr());
			_ornament = new GUI_PrefixObj(_off, prefixClr);
		} else {
			_ornament = new GUI_NoPrefixObj();
		}			
	}
	
	/**
	 * Draw this UI object encapsulated by a border representing the click region this UI element will respond to
	 * @param animTimeMod animation time modifier to enable this object to blink
	 */
	public final void drawDebug() {
		ri.pushMatState();
			ri.setStrokeWt(1.0f);
			++_animCount;
			if(_animCount>20) {_animCount = 0; _cyanStroke = !_cyanStroke;}
			if(_cyanStroke) {ri.setStroke(0, 255, 255,255);} else {	ri.setStroke(255, 0, 255,255);}
			ri.noFill();
			//Draw rectangle around this object denoting active zone
			ri.drawRect(start.x, start.y, end.x - start.x, end.y - start.y);
		ri.popMatState();
		draw();
	}
	
	/**
	 * Draw this UI Object, including any ornamentation if appropriate
	 */
	public final void draw() {
		ri.pushMatState();
			ri.translate(start.x,start.y,0);
			_ornament.drawPrefixObj(ri);
			ri.setFill(fillClr,fillClr[3]);
			ri.setStroke(strkClr,strkClr[3]);	
			//draw specifics for this UI object
			_drawUIData();
		ri.popMatState();
	}//draw

	/**
	 * Draw UI Data String - usually {label}{data value}
	 */
	protected abstract void _drawUIData();
	

}//class Base_GUIObjRenderer
