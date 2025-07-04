package base_UI_Objects.windowUI.uiObjs.renderer.base;

import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.uiObjs.base.Base_GUIObj;
import base_UI_Objects.windowUI.uiObjs.base.GUIObj_Params;
import base_UI_Objects.windowUI.uiObjs.renderer.ornaments.GUI_NoPrefixObj;
import base_UI_Objects.windowUI.uiObjs.renderer.ornaments.GUI_PrefixObj;
import base_UI_Objects.windowUI.uiObjs.renderer.ornaments.base.Base_GUIPrefixObj;

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
     * Base stroke(idx 0) and fill(idx 1) colors, short cut object for drawing bounding box
     */
    protected int[][] rectStrkFillColor;

    /**
     * Highlight stroke(idx 0) and fill(idx 1) colors (for when selected), short cut object for drawing bounding box
     */
    protected int[][] hlRectStrkFillColor;
    
    /**
     * Color of clicked button
     */
    protected static final int[] clickedColor = {150,160,170,255};
    /**
     * Colors of highlit and shadowed edges
     */
    protected static final int[][] edgeColors = new int[][] {{255,255,255,255}, {55,55,55,255}};
    
    /**
     * x,y coords of top left corner for clickable region
     */
    protected myPointf start = new myPointf();
    /**
     * x,y coords of bottom right corner for clickable region
     */
    protected myPointf end = new myPointf();

    /**
     * Owning object consuming this renderer
     */
    protected Base_GUIObj owner;
    
    /**
     * The type of this renderer, for debug purposes
     */
    private final String rendererType;
    /**
     * Catch-all shortcut for text Height value
     */
    protected final float txtHeight;
    protected final float halfTxtHeight;
    
    /**
     * The configuration flags that govern how this renderer will be constructed
     */
    private GUIObjRenderer_Flags cfgFlags;

    /**
     * Width of button edge line
     */
    protected final float lineWidth = 2.0f;
    
    /**
     * Y location of top edge of 3d button/border
     */
    protected final float yTop3DEdgeOffset;
    /**
     * Y location of top edge of 3d button/border
     */
    protected final float yBottom3DEdgeOffset;
    
    /**
     * Length of the longest line of text this object might produce
     */
    protected float longestTextLine;
    
    /**
     * 
     * @param _ri render interface
     * @param _owner Gui object that owns this renderer
     * @param _off offset for ornament
     * @param _menuWidth the allowable width of the printable area. Single line UI objects will be this wide, 
     *                         while multi line will be some fraction of this wide.
     * @param _argObj GUIObjParams that describe colors, render format and other components of the owning gui object
     * @param _rendererType whether single or multi line renderer
     */
    public Base_GUIObjRenderer (
            IRenderInterface _ri,
            Base_GUIObj _owner,
            double[] _off, GUIObj_Params _argObj,
            String _rendererType) {
        ri=_ri;    
        owner = _owner;
         cfgFlags = _argObj.getRenderCreationFormatFlags();

         //_clrs array of stroke, fill and possibly text colors. If only 2 elements, text is idx 0 (stroke)
         int[][] _clrs = _argObj.getStrkFillTextColors();
        // stroke color, fill color, text color for label
        rectStrkFillColor = new int[2][4];
        strkClr = new int[4];
        System.arraycopy(_clrs[0], 0, strkClr, 0, _clrs[0].length);
        System.arraycopy(strkClr, 0, rectStrkFillColor[0], 0, strkClr.length);
        fillClr = new int[4];
        System.arraycopy(_clrs[1], 0, fillClr, 0, _clrs[1].length);
        System.arraycopy(fillClr, 0, rectStrkFillColor[1], 0, fillClr.length);
        textClr = new int[4];
        int textClrIDX = _clrs.length == 3 ? 2 : 0;
        System.arraycopy(_clrs[textClrIDX], 0, textClr, 0, _clrs[textClrIDX].length);
        // height to be size of largest character
        txtHeight = (float) _off[1];
        halfTxtHeight = .65f*txtHeight;
        
        // highlight colors
        hlRectStrkFillColor = ri.getRndMatchedStrkFillClrs();
        //make fill alpha a bit lighter
        hlRectStrkFillColor[1][3] = 150;
        //build prefix ornament to display
        if (getHasOrnament()) {            _ornament = new GUI_PrefixObj(_off, hlRectStrkFillColor[0]);} 
        else {                            _ornament = new GUI_NoPrefixObj();}        
        
        rendererType = _rendererType;
         // lines need the -txtHeight because the UI data has been translated an extra txtHeight already from base class
        yTop3DEdgeOffset = lineWidth;
        yBottom3DEdgeOffset = -lineWidth+1.0f;
    }//ctor
    
    public boolean getIsMultiLine() {return cfgFlags.getIsMultiLine();}
    public boolean getIsOneObjPerLine() {return cfgFlags.getIsOneObjPerLine();}
    public boolean getForceStartNewLine() {return cfgFlags.getForceStartNewLine();}
    public boolean getIsCentered() {return cfgFlags.getIsCentered();}
    public boolean getHasOutline() {return cfgFlags.getHasOutline();}
    public boolean getHasOrnament() {return cfgFlags.getHasOrnament();}
    public boolean getOrnmntClrMatch() {return cfgFlags.getOrnmntClrMatch();}
    
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
    protected final void _drawRectangle(float _strkWt, int[][] _clrs, float[] xywh) {
        ri.pushMatState();
            ri.setStrokeWt(_strkWt);
            ri.setStroke(_clrs[0], _clrs[0][3]);
            ri.setFill(_clrs[1], _clrs[1][3]);
            ri.drawRect(xywh);
        ri.popMatState();
    }

    /**
     * Draw a highlight box around this object representing the click region this UI element will respond to
     */
    public final void drawHighlight() {
        _drawRectangle(strkWt, hlRectStrkFillColor, _getRectDims());
    }//drawHighlight
    
    /**
     * Get x,y, w,h format of hotspot rectangle dims
     * @return
     */
    private float[] _getRectDims() {        return new float[] {start.x, start.y, end.x - start.x, end.y - start.y};    }

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
    public final void drawDebug(boolean isClicked) {        
        ri.pushMatState();
            ri.setStrokeWt(1.0f);
            _animCount += _animMod;
            _animMod = (_animCount <= 0 ? _animSpeed : (_animCount >= 255 ? -_animSpeed : _animMod));
            // Draw rectangle around object with changing color
            ri.setStrokeWt(strkWt);
            ri.setStroke(_animCount, 255-_animCount, 255, 255);
            ri.setNoFill();
            ri.drawRect(_getRectDims());
            ri.drawLine(start.x, start.y,0, end.x, end.y, 0);
            ri.drawLine(start.x, end.y,0, end.x, start.y, 0);
            ri.setStroke(255,0,0, 255);            
            float avgY = start.y + _getCenterY();
            ri.drawLine(start.x, avgY, 0, end.x, avgY, 0);
        ri.popMatState();
        draw(isClicked);
    }//drawDebug
    
    /**
     * Draw this UI Object, including any ornamentation if appropriate
     */
    public final void draw(boolean isClicked) {
        ri.pushMatState();
            if(isClicked || getHasOutline()) {
                // if this is clicked or it is made with an outline (like a button), draw the surrounding rectangle representing the hotspot
                int[][] clrs =  getRectStrkFillClr(isClicked);
                _drawRectangle(strkWt, clrs, _getRectDims());
            }
            ri.translate(start.x,start.y,0);
            _ornament.drawPrefixObj(ri);
            ri.setStrokeWt(1.0f);
            // text is colored by fill specification
            ri.setFill(textClr,textClr[3]);
            ri.setStroke(strkClr,strkClr[3]);
            //draw specifics for this UI object
            if(getHasOutline()) {        _drawButtonEdges(isClicked);}
            ri.translate(0.0f, getStartTextYCentered(), 0.0f);
            if (getIsCentered()) {       _drawUIDataCentered();} 
            else {                       _drawUIData();}            
        ri.popMatState();
    }//draw
    
       
    /**
     * Draw button edges to look like it is 3d
     * @param isClicked
     */
    protected void _drawButtonEdges(boolean isClicked) {
        ri.pushMatState();
            int[] topClr, btmClr;
            if(!isClicked) {        topClr = edgeColors[0]; btmClr = edgeColors[1];}
            else {                  topClr = edgeColors[1]; btmClr = edgeColors[0];}            
            float[] dims = getHotSpotDims();        
            // draw 3d button edges         
            ri.setStrokeWt(lineWidth);            
            //top/left
            ri.setStroke(topClr, topClr[3]);
            ri.drawLine(lineWidth, yTop3DEdgeOffset, 0, dims[0]-lineWidth, yTop3DEdgeOffset, 0); // top line
            ri.drawLine(lineWidth, yTop3DEdgeOffset, 0, lineWidth, yBottom3DEdgeOffset+dims[1], 0);       
            //bottom/right
            ri.setStroke(btmClr, btmClr[3]);
            ri.drawLine(lineWidth, yBottom3DEdgeOffset+dims[1], 0, dims[0]-lineWidth, yBottom3DEdgeOffset+dims[1], 0);//bottom line
            ri.drawLine(dims[0]-lineWidth+1.0f, yTop3DEdgeOffset, 0, dims[0]-lineWidth+1.0f, yBottom3DEdgeOffset+dims[1], 0);//right side line
        ri.popMatState();
    }//_drawButton
    
    /**
     * Get the stroke and fill colors to use for a rectangle around the UI object
     * @return
     */
    protected abstract int[][] getRectStrkFillClr(boolean isClicked);
    
    /**
     * Draw UI Data String - usually {label}{data value}
     */
    protected abstract void _drawUIData();
    
    /**
     * Draw UI Data String centered within hotspot - usually {label}{data value}
     */
    protected abstract void _drawUIDataCentered();    
    
    /**
     * Get center point in x
     * @return
     */
    protected final float _getCenterX() {return (end.x - start.x)/2.0f;}
    
    /**
     * Get center point in y
     * @return
     */
    protected final float _getCenterY() {return (end.y - start.y)/2.0f;}
       
    /**
     * Where to start the text in y for text to be centered in the hotspot. We add 
     * text height and then subtract half-heights because the text is aligned to the bottom of a location, and we want center alignment
     * @return
     */
    public final float getStartTextYCentered() { return _getCenterY() + txtHeight - (getNumTextLines() * halfTxtHeight);}    
    
    /**
     * Return the max width feasible for this UI object's text (based on possible values + label length if any)
     * @return
     */
    public abstract float getMaxTextWidth();
    /**
     * Return the # of text lines the owning object will need to render
     * @return
     */
    public abstract int getNumTextLines();

    /**
     * Get upper left corner coordinates of hotspot for the gui object this renderer draws
     * @return
     */
    public final myPointf getStart() {return start;}    
    /**
     * Set upper left corner coordinates of hotspot for the gui object this renderer draws
     */
    public final void setStart(myPointf _start) {start.set(_start); _ornament.setYCenter(_getCenterY());}
    
    /**
     * Get lower right corner coordinates of hotspot for the gui object this renderer draws
     * @return
     */
    public final myPointf getEnd() {return end;}

    /**
     * Set lower right corner coordinates of hotspot for the gui object this renderer draws
     */
    public final void setEnd(myPointf _end) {end.set(_end);_ornament.setYCenter(_getCenterY());}
    
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
