package base_UI_Objects.baseApp;

import java.util.Arrays;
import java.util.List;

import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.GUI_AppManager;
import base_Utils_Objects.tools.flags.Base_BoolFlags;

/**
 * Class to track the application state flags represented
 */
public class GUI_AppStateFlags extends Base_BoolFlags {
    public static GUI_AppManager appMgr;
    
    public static final int
        //debug is specified in base class as idx 0
        valueKeyPressed        = _numBaseFlags,
        shiftKeyPressed     = _numBaseFlags + 1,            //shift pressed
        altKeyPressed          = _numBaseFlags + 2,            //alt pressed
        cntlKeyPressed      = _numBaseFlags + 3,            //cntrl pressed
        mouseClicked         = _numBaseFlags + 4,            //mouse left button is held down    
        drawing                = _numBaseFlags + 5,             //currently drawing a trajectory
        modView                 = _numBaseFlags + 6;            //shift+mouse click+mouse move being used to modify the view
        
    /**
     * # of control flags being managed
     */
    private static final int numPrivFlags = _numBaseFlags+7;
    
    /**
     * Display UI indicator that varius mod keys are pressed
     */
    private static final List<Integer> _stateFlagsToShow = Arrays.asList( 
        shiftKeyPressed,            //shift pressed
        altKeyPressed,                //alt pressed
        cntlKeyPressed,                //cntrl pressed
        mouseClicked,                //mouse left button is held down    
        drawing,                     //currently drawing
        modView                         //shift+mouse click+mouse move being used to modify the view                    
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
    
    private static final String[] _stateFlagDispNames = {"Shift","Alt","Cntl","Click", "Draw","View"};
    /**
     * Colors for state flags
     */
    private static int[][] _stateFlagColors = new int[_numStateFlagsToShow][3];    
    static {
        _stateFlagColors[0] = new int[]{255,0,0};
        _stateFlagColors[1] = new int[]{0,255,0};
        _stateFlagColors[2] = new int[]{0,0,255};
        _stateFlagColors[3] = new int[]{255,0,255};
        _stateFlagColors[4] = new int[]{255,255,0};
        _stateFlagColors[5] = new int[]{0,255,255};
    }
    
    /**
     * multiplier for displacement to display text label for _stateFlagDispNames
     */
    private static final float[] _stateFlagWidthMult = new float[]{-3.0f,-3.0f,-3.0f,-3.2f,-3.5f,-2.5f};
    private static float[] _stateFlagWidth = new float[_stateFlagWidthMult.length];
    static {
        for(int i=0;i<_stateFlagDispNames.length;++i) {
            _stateFlagWidth[i] = _stateFlagDispNames[i].length() * _stateFlagWidthMult[i];
        }    
    }
    /**
     * Constructor
     */
    public GUI_AppStateFlags(GUI_AppManager _appMgr) {
        super(numPrivFlags);
        appMgr = _appMgr;
        _stateFlagTransX = appMgr.getMenuWidth() / (1.0f*_numStateFlagsToShow+ 1);
    }

    /**
     * Copy constructor
     * @param _otr
     */
    public GUI_AppStateFlags(Base_BoolFlags _otr) {        super(_otr);}

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
    public final void setValueKeyPressed(boolean val){setFlag(valueKeyPressed,val);}
    
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
    private void endAltKey(){            setAltPressed(false);
        for(int i=0; i<appMgr.numDispWins; ++i){appMgr.getDispWindow(i).endAltKey();}
    }
    private void endCntlKey(){            setCntlPressed(false);for(int i=0; i<appMgr.numDispWins; ++i){appMgr.getDispWindow(i).endCntlKey();}}
    private void endValueKeyPressed() {    setValueKeyPressed(false);            
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
    private void _myMouseClicked(int mouseX, int mouseY, int mseBtn){     for(int i=0; i<appMgr.numDispWins; ++i){if (appMgr.getDispWindow(i).handleMouseClick(mouseX, mouseY,mseBtn)){return;}}}
    
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
    public final void mouseDragged(int mouseX, int mouseY, int pmouseX, int pmouseY, myVector drag, boolean isLeftClick, boolean isRightClick){    
        if(isLeftClick){                    _myMouseDragged(mouseX, mouseY, pmouseX, pmouseY,drag,0);}
        else if (isRightClick) {            _myMouseDragged(mouseX, mouseY, pmouseX, pmouseY,drag,1);}
    }//mouseDragged()
    private void _myMouseDragged(int mouseX, int mouseY, int pmouseX, int pmouseY, myVector drag, int mseBtn){    for(int i=0; i<appMgr.numDispWins; ++i){if (appMgr.getDispWindow(i).handleMouseDrag(mouseX, mouseY, pmouseX, pmouseY,drag,mseBtn)) {return;}}}
    
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
    
    @Override
    protected void handleFlagSet_Indiv(int idx, boolean val, boolean oldval) {
        switch(idx){
            case valueKeyPressed     : { break;}//anything special for valueKeyPressed  
            case altKeyPressed         : { break;}//anything special for altKeyPressed     
            case shiftKeyPressed     : { break;}//anything special for shiftKeyPressed     
            case cntlKeyPressed        : { break;}
            case mouseClicked         : { break;}//anything special for mouseClicked         
            case modView             : { break;}//anything special for modView         
            case drawing            : { break;}
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
    private void dispBoolStFlag(IRenderInterface ri, int[] clrAra, int idx, float yOff){
        if(getFlag(_stateFlagsToShow.get(idx))){
            ri.setFill( _stateFlagColors[idx], 255); 
            ri.setStroke( _stateFlagColors[idx], 255);
        } else {
            ri.setColorValFill(IRenderInterface.gui_DarkGray,255); 
            ri.noStroke();    
        }
        ri.drawSphere(5);
        //text(""+txt,-xOff,yOff*.8f);    
        ri.showText(""+_stateFlagDispNames[idx], _stateFlagWidth[idx],yOff*.8f);    
    }    
        
    /**
     * draw state booleans at top of screen and their state
     */
    public final void drawSideBarStateLights(IRenderInterface ri, float yOff){ //_numStateFlagsToShow
        ri.translate(1.5f*_stateFlagTransX, yOff);        
        for(int idx =0; idx<_numStateFlagsToShow; ++idx){
            dispBoolStFlag(ri, _stateFlagColors[idx], idx, yOff);            
            ri.translate(_stateFlagTransX,0);
        }
    }
    

}//class GUI_AppStateFlags
