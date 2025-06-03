package base_UI_Objects.windowUI.uiObjs.renderer.base;

import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.ornaments.GUI_NoPrefixObj;
import base_UI_Objects.windowUI.uiObjs.ornaments.GUI_PrefixObj;
import base_UI_Objects.windowUI.uiObjs.ornaments.base.Base_GUIPrefixObj;

public abstract class Base_GUIObjRenderer {
	/**
	 * Interface to drawing/graphics engine
	 */
	protected static IRenderInterface ri;
	
	/**
	 * Object to either manage and display or not show an ornamental box in front of a UI element
	 */
	protected final Base_GUIPrefixObj _ornament;
	/**
	 * Text color value for main UI object label/text. 
	 * For text rendering this is governed by fill (not stroke)
	 */
	protected int[] textClr = new int[] {0,0,0,255};
	/**
	 * Fill color value for main UI object
	 */
	protected int[] fillClr = new int[] {0,0,0,255};	
	/**
	 * Stroke color value for main UI object
	 */
	protected int[] strkClr = new int[] {0,0,0,255};
	/**
	 * Highlight fill color when selected (for bounding box)
	 */
	protected int[] hlFillClr = new int[] {220, 255, 255, 255};
	/**
	 * Highlight stroke color when selected (for bounding box edge)
	 */
	protected int[] hlStrkClr = new int[] {150, 150, 150,255};

	/**
	 * x,y coords of top left corner for clickable region
	 */
	protected myPointf start;
	/**
	 * x,y coords of bottom right corner for clickable region
	 */
	protected myPointf end;
	
	/**
	 * The allowable width of the printable area. Single line UI objects will be this wide, 
	 * while multi line will be some fraction of this wide.
	 */
	protected final float menuWidth;

	/**
	 * Owning object consuming this renderer
	 */
	protected Base_GUIObj owner;
	
	/**
	 * The type of this renderer, for debug purposes
	 */
	private final String rendererType;
	
	/**
	 * 
	 * @param _ri render interface
	 * @param _owner Gui object that owns this renderer
	 * @param _off offset for ornament
	 * @param _menuWidth the allowable width of the printable area. Single line UI objects will be this wide, 
	 * 						while multi line will be some fraction of this wide.
	 * @param _strkClr stroke color
	 * @param _fillClr fill color
	 * @param _textClr text color - specified in draw using setFill
	 * @param buildPrefix whether to build prefix ornament
	 * @param matchLabelColor whether prefix ornament should match label color
	 * @param _rendererType whether single or multi line renderer
	 */
	public Base_GUIObjRenderer (
			IRenderInterface _ri,
			Base_GUIObj _owner,
			double[] _off,
			float _menuWidth,
			int[] _strkClr,
			int[] _fillClr,  
			int[] _textClr,  
			boolean buildPrefix, 
			boolean matchLabelColor,
			String _rendererType) {
		ri=_ri;	
		owner = _owner;
		menuWidth = _menuWidth;
		// stroke color, fill color, text color of text
		strkClr = _strkClr;
		fillClr = _fillClr;
		textClr = _textClr;
		//build prefix ornament to display
		if (buildPrefix && (_off != null)) {
			int[] prefixClr = (matchLabelColor ? textClr : MyMathUtils.randomIntClrAra());
			System.arraycopy(prefixClr, 0, hlStrkClr, 0, hlStrkClr.length);
			for(int i=0;i<3;++i ) {
				hlStrkClr[i] = prefixClr[i];
				hlFillClr[i] = (prefixClr[i] + 1024)/5;	
			}
			_ornament = new GUI_PrefixObj(_off, prefixClr);
		} else {
			_ornament = new GUI_NoPrefixObj();
		}
		rendererType = _rendererType;
	}
	
	/**
	 * 
	 * @param _ri render interface
	 * @param _owner Gui object that owns this renderer
	 * @param _off offset for ornament
	 * @param _menuWidth the allowable width of the printable area. Single line UI objects will be this wide, 
	 * 						while multi line will be some fraction of this wide.
	 * @param _strkClr stroke color
	 * @param _fillClr fill color
	 * @param buildPrefix whether to build prefix ornament
	 * @param matchLabelColor whether prefix ornament should match label color
	 * @param _rendererType whether single or multi line renderer
	 */
	public Base_GUIObjRenderer (
			IRenderInterface _ri,
			Base_GUIObj _owner,
			double[] _off,
			float _menuWidth,
			int[] _strkClr,
			int[] _fillClr,  
			boolean buildPrefix, 
			boolean matchLabelColor,
			String _rendererType) {
		this(_ri, _owner,_off, _menuWidth, _strkClr, _fillClr, _fillClr, buildPrefix, matchLabelColor, _rendererType);
	}
	
	/**
	 * Verify passed coordinates are within this object's modifiable zone. If true then this object will be modified by UI actions
	 * @param _clkx
	 * @param _clky
	 * @return whether passed coords are within this object's modifiable zone
	 */
	public final boolean checkIn(float _clkx, float _clky){return (_clkx >= start.x)&&(_clkx <= end.x)&&(_clky >= start.y)&&(_clky <= end.y);}

	/**
	 * Draw a highlight box around this object representing the click region this UI element will respond to
	 */
	public final void drawHighlight() {
		ri.pushMatState();
			ri.setStrokeWt(1.0f);
			ri.setFill(hlFillClr, hlFillClr[3]);
			ri.setStroke(hlStrkClr, hlStrkClr[3]);
			//Draw rectangle around this object denoting active zone
			_drawRectangle();
		ri.popMatState();
	}//drawHighlight

	/**
	 * Used to draw this UI object encapsulated by a border representing the
	 * click region this UI element will respond to, for debug
	 */
	private int _animCount = 0;
	private final int _animSpeed = 10;
	private int _animMod = _animSpeed;
	/**
	 * Draw this UI object encapsulated by a border representing the click region this UI element will respond to
	 */
	public final void drawDebug() {		
		ri.pushMatState();
			ri.setStrokeWt(1.0f);
			_animCount += _animMod;
			_animMod = (_animCount <= 0 ? _animSpeed : (_animCount >= 255 ? -_animSpeed : _animMod));
			ri.setStroke(_animCount, 255-_animCount, 255,255);
			ri.noFill();
			//Draw rectangle around this object denoting active zone
			_drawRectangle();
			ri.drawLine(start.x, start.y,0, end.x, end.y, 0);
			ri.drawLine(start.x, end.y,0, end.x, start.y, 0);			
		ri.popMatState();
		draw();
	}//drawDebug
	
	/**
	 * Draw rectangle for object - debug, button, etc
	 */
	protected void _drawRectangle() {		ri.drawRect(start.x, start.y, end.x - start.x, end.y - start.y);	}
	
	/**
	 * Draw this UI Object, including any ornamentation if appropriate
	 */
	public final void draw() {
		ri.pushMatState();
			ri.translate(start.x,start.y,0);
			_ornament.drawPrefixObj(ri);
			//Text is colored by fill
			ri.setFill(textClr,textClr[3]);
			ri.setStroke(strkClr,strkClr[3]);	
			//draw specifics for this UI object
			_drawUIData();
		ri.popMatState();
	}//draw

	/**
	 * Draw UI Data String - usually {label}{data value}
	 */
	protected abstract void _drawUIData();
	
	/**
	 * Recalculate the lower right location of the hotspot for the owning UI object
	 * @param newStartPoint new upper left point
	 * @param lineHeight the height of a single line of text
	 * @param menuStart the x coord of the start of the menu region
	 * @param menuWidth the possible display with for the object
	 * @return the next object's new start location
	 */
	public abstract myPointf reCalcHotSpot(myPointf newStart, float lineHeight, float menuStartX, float menuWidth);
	
	/**
	 * Return the maximum width of the owning UI object in the display.
	 * @return
	 */
	public abstract float getMaxWidth();
	/**
	 * Whether the gui object this renderer manages is multi-line or single line
	 * @return
	 */
	public abstract boolean isMultiLine();
	/**
	 * Upper left corner of hotspot for the gui object this renderer draws
	 * @return
	 */
	public final myPointf getStart() {return start;}
	/**
	 * Lower right corner of hotspot for the gui object this renderer draws
	 * @return
	 */
	public final myPointf getEnd() {return end;}
	
	public final String getHotBoxLocString() {
		return  rendererType+ " Rendered : Upper Left crnr click zone : ["+ start.x +","+start.y+"]| Lower Right crnr click zone : ["+ end.x +","+end.y+"]";
	}
	

}//class Base_GUIObjRenderer
