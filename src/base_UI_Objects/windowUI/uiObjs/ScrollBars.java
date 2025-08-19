package base_UI_Objects.windowUI.uiObjs;

import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Render_Interface.IGraphicsAppInterface;
import base_UI_Objects.windowUI.base.Base_DispWindow;


public class ScrollBars{
    public IGraphicsAppInterface ri;
    public Base_DispWindow win;
    public static int scrBarCnt = 0;
    public int ID;
    //displacement for scrolling display - x/y location of window, x/y zoom - use scrollbars&zoomVals - displacement is translate, zoom is scale
    public float[] scrollZoomDisp,
    //start x,y, width, height of scroll bar region 
         hScrlDims,    
    //start x,y, width, height of scroll bar region 
         vScrlDims;
    private final float thk = 20, thmult = 1.5f;                //scrollbar dim is 25 pxls wide    
    
    public float[][] arrowDims = new float[][]{{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};
    //hthumb x,y width,height,vthumb x,y width,height
    public float[][] thmbs = new float[][]{{0,0},{0,0}};
    //hthumb min/max, vthumb min/max bounds
    public float[][] thmbBnds = new float[][]{{0,0},{0,0}};
    
    public static final int
        upIDX = 0,
        dnIDX = 1,
        ltIDX = 2,
        rtIDX = 3,
        hThmbIDX = 0,
        vThmbIDX = 1;
    
    public int[][] clrs;            //colors for thumb and up/down/left/right arrows
    
    public ScrollBars(IGraphicsAppInterface _ri,Base_DispWindow _win){
        ri = _ri;
        win = _win;
        ID = scrBarCnt++;
        setSize();
        clrs = new int[6][];
        for(int i=0;i<clrs.length;++i){    clrs[i]=MyMathUtils.randomIntClrAra();}
    }//myScrollBars
    
    public void setSize(){
        float[] winRectDim = win.getRectDims();
        float rectWidth = winRectDim[0]+winRectDim[2],vScrlStartY = winRectDim[1]+(win.getCloseBoxHeight()),
                rectHeight = winRectDim[3]-(vScrlStartY);
        vScrlDims = new float[]{rectWidth - thk,vScrlStartY, thk,  rectHeight-thk};
        hScrlDims = new float[]{winRectDim[0], winRectDim[1]+winRectDim[3] - thk,winRectDim[2]-thk,thk};

        thmbs[hThmbIDX] = new float[]{hScrlDims[0]+thk,hScrlDims[1],thmult*thk,thk};            //location/dims of thumb
        thmbBnds[hThmbIDX] = new float[]{thmbs[hThmbIDX][0],hScrlDims[2]-thmbs[hThmbIDX][2]-thk};        //min/max x val of horiz thumb
        
        thmbs[vThmbIDX] = new float[]{vScrlDims[0],(vScrlDims[1]+thk),thk,thmult*thk};            //location/dims of thumb
        thmbBnds[vThmbIDX] = new float[]{thmbs[vThmbIDX][1],vScrlDims[3]-thmbs[vThmbIDX][3]-thk};        //min/max of y val of vert thumb
        
        //arrow boxes - x,y,widht,height
        arrowDims = new float[][]{
            {vScrlDims[0],vScrlDims[1],thk,thk},            //up
            {vScrlDims[0],vScrlDims[1]+vScrlDims[3]-thk,thk,thk},        //down
            {hScrlDims[0],                    hScrlDims[1],thk,thk},            //left
            {hScrlDims[0]+hScrlDims[2]-thk,hScrlDims[1],thk,thk}};        //right

    }
    public void drawMe(boolean isGlblAppDebug){
        ri.pushMatState();
        ri.setColorValFill(IGraphicsAppInterface.gui_LightGray,255);
        ri.setColorValStroke(IGraphicsAppInterface.gui_Black,255);
        ri.setStrokeWt(1.0f);
        ri.drawRect(vScrlDims);
        ri.drawRect(hScrlDims);
        for(int i =0; i<arrowDims.length;++i){
            ri.setFill(clrs[i],clrs[i][3]);
            ri.drawRect(arrowDims[i]);
        }
        ri.popMatState();
        ri.pushMatState();
        for(int i =0; i<thmbs.length;++i){
            ri.setFill(clrs[i + 4],clrs[i + 4][3]);
            ri.drawRect(thmbs[i]);
        }
        
        ri.popMatState();
    }//drawMe
    
    
    public boolean msePtInRect(int x, int y, float[] r){return ((x > r[0])&&(x <= r[0]+r[2])&&(y > r[1])&&(y <= r[1]+r[3]));}        
    public boolean handleMouseClick(int mouseX, int mouseY, myPoint mouseClickIn3D){
        
        return false;    
    }//handleMouseClick
    public boolean handleMouseDrag(int mouseX, int mouseY,int pmouseX, int pmouseY, myPoint mouseClickIn3D, myVector mseDragInWorld){    
        
        return false;
    }//handleMouseDrag
    public void handleMouseRelease(){
        
        
    }//handleMouseRelease
    
}//myScrollBars