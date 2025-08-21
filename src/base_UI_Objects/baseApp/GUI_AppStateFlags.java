package base_UI_Objects.baseApp;

import java.util.Arrays;
import java.util.List;

import base_Render_Interface.IGraphicsAppInterface;
import base_UI_Objects.GUI_AppManager;
import base_Utils_Objects.tools.flags.Base_BoolFlags;

/**
 * Class to track the application state flags represented
 */
public class GUI_AppStateFlags extends Base_BoolFlags {
    public static GUI_AppManager appMgr;
    
    public static final int
        //debug is specified in base class as idx 0
        valueKeyPressed     = _numBaseFlags,                //key has been pressed
        shiftKeyPressed     = _numBaseFlags + 1,            //shift pressed
        altKeyPressed       = _numBaseFlags + 2,            //alt pressed
        cntlKeyPressed      = _numBaseFlags + 3,            //cntrl pressed
        mouseClicked        = _numBaseFlags + 4,            //mouse left button is held down    
        drawing             = _numBaseFlags + 5,             //currently drawing a trajectory
        modView             = _numBaseFlags + 6;            //shift+mouse click+mouse move being used to modify the view
        
    /**
     * # of control flags being managed
     */
    private static final int _numPrivFlags = _numBaseFlags+7;
    
    /**
     * Display UI indicator for what keys are pressed and what UI-driven actions are occurring
     */
    private static final List<Integer> _stateFlagsToShow = Arrays.asList(
        valueKeyPressed,
        shiftKeyPressed,            //shift pressed
        altKeyPressed,              //alt pressed
        cntlKeyPressed,             //cntrl pressed
        mouseClicked,               //mouse left button is held down    
        drawing,                    //currently drawing
        modView                     //shift+mouse click+mouse move being used to modify the view                    
        );
    private static final int _numStateFlagsToShow = _stateFlagsToShow.size();
    
    /**
     * Flags to clear on mouse button release
     */
    private static final int[] _baseRelFlagsToClear=new int[]{mouseClicked, modView};
    
    /**
     * Distance to translate each state flag in x
     */
    private static float _stateFlagTransX;
    
    private static final String[] _stateFlagDispNames = {"Key","Shift","Alt","Cntl","Click","Draw","View"};
    /**
     * Colors for state flags
     */
    private static final int[][] _stateFlagColors = new int[_numStateFlagsToShow][3];    
    static {
        int idx=0;        
        _stateFlagColors[idx++] = new int[]{0,0,0};
        _stateFlagColors[idx++] = new int[]{255,0,0};
        _stateFlagColors[idx++] = new int[]{0,255,0};
        _stateFlagColors[idx++] = new int[]{0,0,255};
        _stateFlagColors[idx++] = new int[]{255,0,255};
        _stateFlagColors[idx++] = new int[]{255,255,0};
        _stateFlagColors[idx++] = new int[]{0,255,255};
    }
    
    /**
     * Color for flags that are off
     */
    private static final int[] _offColor = new int[] {80,80,80};
    /**
     * multiplier for displacement to display text label for _stateFlagDispNames
     */
    private static final float[] _stateFlagWidth = new float[_numStateFlagsToShow];
    static {
        for(int i=0;i<_stateFlagDispNames.length;++i) {
            _stateFlagWidth[i] = _stateFlagDispNames[i].length() * -3.0f;
        }    
    }
    
    /**
     * What key is currently being pressed, or null if none
     */
    private char _keyPressed;
    
    /**
     * Constructor
     */
    public GUI_AppStateFlags(GUI_AppManager _appMgr) {
        super(_numPrivFlags);
        appMgr = _appMgr;
        _stateFlagTransX = appMgr.getMenuWidth() / (1.0f*_numStateFlagsToShow + 1);
        _keyPressed = 0x00;
    }

    /**
     * Copy constructor
     * @param _otr
     */
    public GUI_AppStateFlags(GUI_AppStateFlags _otr) {        
        super(_otr);
        _keyPressed = _otr._keyPressed;
    }

    @Override
    protected void handleSettingDebug(boolean val) {}
    
    /**
     * check if shift, alt, or control are pressed
     * @param keyCode
     */
    public final void checkAndSetSACKeys(int keyCode) {
        if(!shiftIsPressed()){setShiftPressed(keyCode  == 16);} //16 == KeyEvent.VK_SHIFT
        if(!cntlIsPressed()){setCntlPressed(keyCode  == 17);}//17 == KeyEvent.VK_CONTROL            
        if(!altIsPressed()){setAltPressed(keyCode  == 18);}//18 == KeyEvent.VK_ALT
    }
    
    public final boolean shiftIsPressed() {return getFlag(shiftKeyPressed);}
    public final void setShiftPressed(boolean val) {setFlag(shiftKeyPressed,val);}

    public final boolean altIsPressed() {return getFlag(altKeyPressed);}
    public final void setAltPressed(boolean val) {setFlag(altKeyPressed,val);}
    
    public final boolean cntlIsPressed() {return getFlag(cntlKeyPressed);}
    public final void setCntlPressed(boolean val) {setFlag(cntlKeyPressed,val);}
    
    public final boolean valueKeyIsPressed() {return getFlag(valueKeyPressed);}
    public final void setValueKeyPressed(char key, boolean val){
        _keyPressed = val ? key: 0x00;       
        setFlag(valueKeyPressed,val);
    }
    
    public final boolean mouseIsClicked() {return getFlag(mouseClicked);}
    public final void setMouseClicked(boolean val) {setFlag(mouseClicked,val);}
    
    public final boolean IsModView() {return getFlag(modView);}
    public final void setModView(boolean val) {setFlag(modView,val);}    
    
    public final boolean IsDrawing() {return getFlag(drawing);}
    public final void setIsDrawing(boolean val) {setFlag(drawing,val);}

    
    public final void checkKeyReleased(boolean keyIsCoded, int keyCode) {
        if(valueKeyIsPressed()) {endValueKeyPressed();}
        if(keyIsCoded) {
            if(shiftIsPressed() && (keyCode == 16)){endShiftKey();}
            if(cntlIsPressed() && (keyCode == 17)){endCntlKey();}
            if(altIsPressed() && (keyCode == 18)){endAltKey();}
        }
    }
    //modview tied to shift key
    private static int[] shiftKeyFlags = new int []{shiftKeyPressed, modView}; 
    
    private void endShiftKey(){             
        setAllFlagsToFalse(shiftKeyFlags);
        for(int i=0; i<appMgr.numDispWins; ++i){appMgr.getDispWindow(i).endShiftKey();}
    }
    private void endAltKey(){               setAltPressed(false);for(int i=0; i<appMgr.numDispWins; ++i){appMgr.getDispWindow(i).endAltKey();}}
    private void endCntlKey(){              setCntlPressed(false);for(int i=0; i<appMgr.numDispWins; ++i){appMgr.getDispWindow(i).endCntlKey();}}
    
    private void endValueKeyPressed() {     
        setValueKeyPressed((char) 0x00, false);            
        for(int i=0; i<appMgr.numDispWins; ++i){appMgr.getDispWindow(i).endValueKeyPress();}
    }
        
    /**
     * Mouse button pressed
     * @param mouseX
     * @param mouseY
     * @param isLeftClick
     * @param isRightClick
     */
    public final void mousePressed(int mouseX, int mouseY,boolean isLeftClick, boolean isRightClick) {
        setFlag(mouseClicked, true);
        if(isLeftClick){                    _myMouseClicked(mouseX, mouseY,0);} 
        else if (isRightClick) {            _myMouseClicked(mouseX, mouseY,1);}
    }// mousePressed        
    private void _myMouseClicked(int mouseX, int mouseY, int mseBtn){     
        for(int i=0; i<appMgr.numDispWins; ++i){if (appMgr.getDispWindow(i).handleMouseClick(mouseX, mouseY,mseBtn)){return;}}
    }
    
    /**
     * Handle mouse being dragged from old position to new position
     * @param mouseX current mouse x
     * @param mouseY current mouse y
     * @param pmouseX previous mouse x 
     * @param pmouseY previous mouse y
     * @param drag dragged vector
     * @param isLeftClick
     * @param isRightClick
     */
    public final void mouseDragged(int mouseX, int mouseY, int pmouseX, int pmouseY, boolean isLeftClick, boolean isRightClick){    
        if(isLeftClick){                    _myMouseDragged(mouseX, mouseY, pmouseX, pmouseY, 0);}
        else if (isRightClick) {            _myMouseDragged(mouseX, mouseY, pmouseX, pmouseY, 1);}
    }//mouseDragged()
    private void _myMouseDragged(int mouseX, int mouseY, int pmouseX, int pmouseY, int mseBtn){    
        for(int i=0; i<appMgr.numDispWins; ++i){if (appMgr.getDispWindow(i).handleMouseDrag(mouseX, mouseY, pmouseX, pmouseY, mseBtn)) {return;}}
    }
    
    /**
     * Handle mouse wheel
     * @param ticks amount of wheel moves
     */
    public final void mouseWheel(int ticks) {
        for(int i=0; i<appMgr.numDispWins; ++i){if (appMgr.getDispWindow(i).handleMouseWheel(ticks, (getFlag(shiftKeyPressed)) ? 50.0f * GUI_AppManager.mouseWhlSens : 10.0f*GUI_AppManager.mouseWhlSens)) {return;}}        
    }

    /**
     * Handle mouse button release
     */
    public final void mouseReleased(){
        setAllFlagsToFalse(_baseRelFlagsToClear);
        for(int i=0; i<appMgr.numDispWins; ++i){appMgr.getDispWindow(i).handleMouseRelease();}
        setFlag(drawing, false);
    }//mouseReleased
    
    /**
     * Any special handling for the flags this implementation handles
     */
    @Override
    protected void handleFlagSet_Indiv(int idx, boolean val, boolean oldval) {
        switch(idx){
            case valueKeyPressed   : { break;}
            case altKeyPressed     : { break;}
            case shiftKeyPressed   : { break;}
            case cntlKeyPressed    : { break;}
            case mouseClicked      : { break;}
            case modView           : { break;}
            case drawing           : { break;}
        }                
    }
    
    /**
     * display state flag indicator at top of window
     * @param txt
     * @param clrAra
     * @param state
     * @param stMult
     * @param yOff
     */
    private void dispBoolStFlag(IGraphicsAppInterface ri, String label, float width, int[] clrAra, int idx, float yOff){
        ri.setFill(clrAra, 255); 
        ri.setStroke(clrAra, 255);
        ri.drawSphere(5);
        //text(""+txt,-xOff,yOff*.8f);    
        ri.showText(label, width,yOff*.8f);    
    }    
   
    /**
     * Draw state booleans at top of screen and their state
     * @param ri
     * @param animTimeMod
     * @param yOff
     */
    public final void drawSideBarStateLights(IGraphicsAppInterface ri, float animTimeMod, float yOff){ //_numStateFlagsToShow
        ri.translate(1.5f*_stateFlagTransX, yOff);
        //For 'key' flag - special handling to display key pressed
        int[] clrToUse;
        String label;
        float width;
        if(getFlag(_stateFlagsToShow.get(0))) {
            clrToUse = _stateFlagColors[0];
            label = _stateFlagDispNames[0]+":(`"+_keyPressed+"`)";
            width = label.length() * -3.0f;
        } else {
            clrToUse = _offColor;
            label = _stateFlagDispNames[0];
            width = _stateFlagWidth[0];
        }
        dispBoolStFlag(ri, label, width, clrToUse, 0, yOff);
        ri.translate(_stateFlagTransX,0);        
        for(int idx = 1; idx<_numStateFlagsToShow; ++idx){
            dispBoolStFlag(ri, _stateFlagDispNames[idx], _stateFlagWidth[idx], getFlag(_stateFlagsToShow.get(idx)) ? _stateFlagColors[idx] : _offColor, idx, yOff);
            ri.translate(_stateFlagTransX,0);
        }
    }
    

}//class GUI_AppStateFlags
