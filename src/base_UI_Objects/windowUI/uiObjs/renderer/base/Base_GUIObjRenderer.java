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
	 * Stroke weight for main UI object
	 */
	protected float strkWt = 1.0f;
	/**
	 * Stroke color value for main UI object
	 */
	protected int[] strkClr = new int[] {0,0,0,255};
	
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
	 * Base stroke(idx 0) and fill(idx 1) colors, short cut object for drawing rectangles
	 */
	protected int[][] rectStrkFillColor;

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
	protected myPointf start = new myPointf();
	/**
	 * x,y coords of bottom right corner for clickable region
	 */
	protected myPointf end = new myPointf();
	
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
	 * Catch-all y-offset corresponding to a previs
	 */
	protected final float yOffset;
	
	/**
	 * Flags structure to monitor/manage configurable behavior. No child class should access these directly
	 */
	private int[] rndrConfigFlags;
	private static final int
		isMultiLineIDX = 0,			// Should be multiline
		centerTextIDX = 1,			// Text should be centered 
		hasOutlineIDX = 2,			// An outline around the object should be rendered
		hasOrnamentIDX = 3,			// Should have ornament
		ornmntClrMatchIDX = 4;		// Ornament color should match label color
	private static final int numConfigFlags = 5;
	
	
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
	 * @param _rendererType whether single or multi line renderer
	 */
	public Base_GUIObjRenderer (
			IRenderInterface _ri,
			Base_GUIObj _owner,
			double[] _off,
			float _menuWidth,
			int[][] _clrs,
			boolean[] _guiFormatBoolVals,
			String _rendererType) {
		ri=_ri;	
		owner = _owner;
		menuWidth = _menuWidth;
		// stroke color, fill color, text color for label
		rectStrkFillColor = new int[2][4];
		strkClr = new int[4];
		System.arraycopy(_clrs[0], 0, strkClr, 0, _clrs[0].length);
		System.arraycopy(strkClr, 0, rectStrkFillColor[0], 0, strkClr.length);
		fillClr = new int[4];
		System.arraycopy(_clrs[1], 0, fillClr, 0, _clrs[1].length);
		System.arraycopy(fillClr, 0, rectStrkFillColor[1], 0, fillClr.length);
		textClr = new int[4];
		int textClrIDX = _clrs.length == 3 ? 2 : 1;
		System.arraycopy(_clrs[textClrIDX], 0, textClr, 0, _clrs[textClrIDX].length);
		//build prefix ornament to display
		yOffset = 0.75f * (float) _off[1];
		initConfigFlags();
		int numToInit = (_guiFormatBoolVals.length < numConfigFlags ? _guiFormatBoolVals.length : numConfigFlags);
		for(int i =0; i<numToInit;++i){ 	setConfigFlags(i,_guiFormatBoolVals[i]);	}	
		
		if (getHasOrnament()) {
			int[] prefixClr = (getOrnmntClrMatch() ? textClr : MyMathUtils.randomIntClrAra());
			for(int i=0;i<3;++i ) {
				hlStrkClr[i] = prefixClr[i];
				// make fill color 75% brighter
				hlFillClr[i] = (prefixClr[i] + 1024)/5;	
			}
			_ornament = new GUI_PrefixObj(_off, prefixClr);
		} else {
			_ornament = new GUI_NoPrefixObj();
		}
		rendererType = _rendererType;
	}//ctor
	
	private void initConfigFlags(){			rndrConfigFlags = new int[1 + numConfigFlags/32]; for(int i = 0; i<numConfigFlags; ++i){setConfigFlags(i,false);}	}
	private boolean getConfigFlags(int idx){	int bitLoc = 1<<(idx%32);return (rndrConfigFlags[idx/32] & bitLoc) == bitLoc;}	
	private void setConfigFlags(int idx, boolean val){
		int flIDX = idx/32, mask = 1<<(idx%32);
		rndrConfigFlags[flIDX] = (val ?  rndrConfigFlags[flIDX] | mask : rndrConfigFlags[flIDX] & ~mask);
		switch (idx) {//special actions for each flag
		case isMultiLineIDX			:{break;}
		case centerTextIDX			:{break;}
		case hasOutlineIDX			:{break;}
		case hasOrnamentIDX			:{break;}
		case ornmntClrMatchIDX 		:{break;}
		}
	}//setConfigFlags
	
	protected void setIsMultiLine(boolean isMultiLine) {setConfigFlags(isMultiLineIDX, isMultiLine);}
	public boolean getIsMultiLine() {return getConfigFlags(isMultiLineIDX);}
	
	protected void setIsCentered(boolean isCentered) {setConfigFlags(centerTextIDX, isCentered);}
	public boolean getIsCentered() {return getConfigFlags(centerTextIDX);}
	
	protected void setHasOutline(boolean hasOutline) {setConfigFlags(hasOutlineIDX, hasOutline);}
	public boolean getHasOutline() {return getConfigFlags(hasOutlineIDX);}
	
	protected void setHasOrnament(boolean hasOrnament) {setConfigFlags(hasOrnamentIDX, hasOrnament);}
	public boolean getHasOrnament() {return getConfigFlags(hasOrnamentIDX);}
	
	protected void setOrnmntClrMatch(boolean ornClr) {setConfigFlags(ornmntClrMatchIDX, ornClr);}
	public boolean getOrnmntClrMatch() {return getConfigFlags(ornmntClrMatchIDX);}
	
	/**
	 * Verify passed coordinates are within this object's modifiable zone. If true then this object will be modified by UI actions
	 * @param _clkx
	 * @param _clky
	 * @return whether passed coords are within this object's modifiable zone
	 */
	public final boolean checkIn(float _clkx, float _clky){return (_clkx >= start.x)&&(_clkx <= end.x)&&(_clky >= start.y)&&(_clky <= end.y);}
	
	/**
	 * Draw hotspot rectangle around object
	 * @param strkWt
	 * @param _strkClr
	 * @param _fillClr
	 * @param xywh
	 */
	protected final void _drawRectangle(float _strkWt, int[] _strkClr, int[] _fillClr, float[] xywh) {
		ri.pushMatState();
			ri.setStrokeWt(_strkWt);
			ri.setStroke(_strkClr, _strkClr[3]);
			ri.setFill(_fillClr, _fillClr[3]);
			ri.drawRect(xywh);
		ri.popMatState();
	}

	/**
	 * Draw a highlight box around this object representing the click region this UI element will respond to
	 */
	public final void drawHighlight() {
		_drawRectangle(strkWt, hlStrkClr, hlFillClr, _getRectDims());
	}//drawHighlight
	
	/**
	 * Get x,y, w,h format of hotspot rectangle dims
	 * @return
	 */
	private float[] _getRectDims() {		return new float[] {start.x, start.y, end.x - start.x, end.y - start.y};	}

	/**
	 * Used to draw this UI object encapsulated by a border representing the
	 * click region this UI element will respond to, for debug
	 */
	private int _animCount = 0;
	private final int _animSpeed = 10;
	private int _animMod = _animSpeed;
	// color to reproduce no fill
	private int[] _noFillClr = new int[] {0,0,0,0};
	/**
	 * Draw this UI object encapsulated by a border representing the click region this UI element will respond to
	 */
	public final void drawDebug(boolean isClicked) {		
		ri.pushMatState();
			ri.setStrokeWt(1.0f);
			_animCount += _animMod;
			_animMod = (_animCount <= 0 ? _animSpeed : (_animCount >= 255 ? -_animSpeed : _animMod));
			// Draw rectangle around object with changing color
			_drawRectangle(strkWt, new int[] {_animCount, 255-_animCount, 255,255}, _noFillClr, _getRectDims());
			ri.drawLine(start.x, start.y,0, end.x, end.y, 0);
			ri.drawLine(start.x, end.y,0, end.x, start.y, 0);			
		ri.popMatState();
		draw(isClicked);
	}//drawDebug
	
	/**
	 * Draw this UI Object, including any ornamentation if appropriate
	 */
	public final void draw(boolean isClicked) {
		if(isClicked) {drawHighlight();}
		ri.pushMatState();
			if(isClicked || getHasOutline()) {
				int[][] clrs =  getRectStrkFillClr(isClicked);
				_drawRectangle(strkWt, clrs[0], clrs[1], _getRectDims());
			}
			ri.translate(start.x,start.y+yOffset,0);
			_ornament.drawPrefixObj(ri);
			ri.setStrokeWt(1.0f);
			// text is colored by fill specification
			ri.setFill(textClr,textClr[3]);
			ri.setStroke(strkClr,strkClr[3]);
			//draw specifics for this UI object
			_drawUIData(isClicked);
		ri.popMatState();
	}//draw
	
	/**
	 * Get the stroke and fill colors to use for a rectangle around the UI object
	 * @return
	 */
	protected abstract int[][] getRectStrkFillClr(boolean isClicked);
	
	/**
	 * Draw UI Data String - usually {label}{data value}
	 */
	protected abstract void _drawUIData(boolean isClicked);
	
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
	 * Get upper left corner coordinates of hotspot for the gui object this renderer draws
	 * @return
	 */
	public final myPointf getStart() {return start;}	
	/**
	 * Set upper left corner coordinates of hotspot for the gui object this renderer draws
	 */
	public final void setStart(myPointf _start) {start.set(_start);}
	
	/**
	 * Get lower right corner coordinates of hotspot for the gui object this renderer draws
	 * @return
	 */
	public final myPointf getEnd() {return end;}

	/**
	 * Set lower right corner coordinates of hotspot for the gui object this renderer draws
	 */
	public final void setEnd(myPointf _end) {end.set(_end);} 
	
	/**
	 * Get the center point of the hotspot
	 * @return
	 */
	public final myPointf getCenter() {return myPointf._average(start, end);}
	
	/**
	 * Get the width and height of the hotspot for the gui object this renderer draws
	 * @return
	 */
	public final float[] getHotSpotDims() {return new float[] {end.x-start.x, end.y-start.y};}

	/**
	 * Update the renderer based on new/modified state of the UI object
	 */
	public abstract void updateFromObject();
	
	public final String getHotBoxLocString() {
		return  rendererType+ " Rendered : Upper Left crnr click zone : ["+ start.x +","+start.y+"]| Lower Right crnr click zone : ["+ end.x +","+end.y+"]";
	}
	

}//class Base_GUIObjRenderer
