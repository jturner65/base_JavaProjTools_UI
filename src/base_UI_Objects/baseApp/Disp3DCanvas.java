package base_UI_Objects.baseApp;

import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_Render_Interface.IGraphicsAppInterface;
import base_Render_Interface.shape.GL_PrimitiveType;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.base.Base_DispWindow;

/**
 * Class to manage display canvas and reticle for 3d windows
 * @author John Turner
 *
 */
public class Disp3DCanvas {
    /**
     * GL-interface for rendering and screen<->world coords transforms
     */
    private static IGraphicsAppInterface ri;
    
    /**
     * Screen ctr location in world coords
     */        
    private myPoint scrCtrInWorld;
    private myPoint eyeInWorld; 
    private myPoint oldMseLoc;
    private myPoint dfCtr;                                                        //mouse location projected onto current drawing canvas

    private final float canvasDim = 15000,
            canvasDimOvSqrt2 = MyMathUtils.INV_SQRT_2_F * canvasDim;             //canvas dimension for "virtual" 3d        
    private myPoint[] canvas3D;                                                    //3d plane, normal to camera eye, to be used for drawing - need to be in "view space" not in "world space", so that if camera moves they don't change
    private myVector eyeToMse,                                                    //eye to 2d mouse location 
                    eyeToCtr,                                                    //vector from eye to center of cube, to be used to determine which panels of bounding box to show or hide
                    drawSNorm;                                                    //current normal of viewport/screen
        
    private int viewDimW, viewDimH,viewDimW2, viewDimH2;
    
    private int[] mseFillClr;
    
    private GUI_AppManager AppMgr;
    
    /**
     * Screen-space depth in window of screen-space halfway point.
     */
    private float rawCtrDepth;
    
    public Disp3DCanvas(GUI_AppManager _AppMgr, IGraphicsAppInterface _ri, int w, int h) {
        ri = _ri;
        AppMgr = _AppMgr;
        mseFillClr = new int[] {0,0,0,255};
        initCanvasVars();
        setViewDim(w,h);
    }
    
    private void initCanvasVars(){
        canvas3D = new myPoint[4];        //3 points to define canvas
        canvas3D[0]=new myPoint();canvas3D[1]=new myPoint();canvas3D[2]=new myPoint();canvas3D[3]=new myPoint();
        eyeInWorld = new myPoint();        
        scrCtrInWorld = new myPoint();
        eyeInWorld = new myPoint();
        oldMseLoc  = new myPoint();
        dfCtr = new myPoint();                                            //mouse location projected onto current drawing canvas
        eyeToMse = new myVector();        
        eyeToCtr = new myVector();    
        drawSNorm = new myVector();    
    }//initCanvasVars
    
    /**
     * Set the view dimensions and (re)build the canvas
     * @param w
     * @param h
     */
    public void setViewDim(int w, int h) {
        viewDimW = w; viewDimH = h;
        viewDimW2 = viewDimW/2; viewDimH2 = viewDimH/2;    
        //Only changes when viewDims change
        rawCtrDepth = ri.getDepth(viewDimW2, viewDimH2);
        buildCanvas();
    }

    /**
     * find points to define plane normal to camera eye, at set distance from camera, to use drawing canvas     
     */
    public void buildCanvas(){
        // Screen center in world space
        myPoint rawScrCtrInWorld = ri.getWorldLoc(viewDimW2, viewDimH2, rawCtrDepth);
        // Screen norm is from raw center in world to camera, at depth ==0
        drawSNorm = new myVector(rawScrCtrInWorld, ri.getWorldLoc(viewDimW2, viewDimH2, 0))._normalize();
        //build plane using norm - have canvas go through canvas ctr in 3d
        myVector planeTan = myVector._cross(drawSNorm, myVector._normalize(new myVector(drawSNorm.x+1000,drawSNorm.y-1000,drawSNorm.z+10)))._normalize();            //result of vector crossed with normal will be in plane described by normal
        myPoint lastPt = new myPoint(myPoint.ZEROPT, canvasDimOvSqrt2, planeTan);
        planeTan = myVector._rotAroundAxis(planeTan, drawSNorm, MyMathUtils.THREE_QTR_PI);       
        
        for(int i =0;i<canvas3D.length;++i){        
            //build invisible canvas to draw upon
             canvas3D[i].set(myPoint._add(lastPt, canvasDim, planeTan));
             //rotate around center point by 90 degrees to build a square canvas
             planeTan = myVector._rotAroundAxis(planeTan, drawSNorm);
             lastPt = canvas3D[i];
         }

        //normal to canvas through eye moved far behind viewer
        eyeInWorld = ri.getWorldLoc(viewDimW2, viewDimH2,AppMgr._camEyeZ);
        //eyeInWorld =myPoint._add(rawScrCtrInWorld, myPoint._dist( ri.pick(0,0,-1), rawScrCtrInWorld), drawSNorm);                                //location of "eye" in world space
        eyeToCtr.set(eyeInWorld, rawScrCtrInWorld);
        scrCtrInWorld = getPlInterSect(rawScrCtrInWorld, myVector._normalize(eyeToCtr));
        
        myPoint mseLocInWorld = getMseLocInWorld();    
        //unit vector in world coords of "eye" to mouse location
        eyeToMse.set(eyeInWorld, mseLocInWorld);        
        eyeToMse._normalize();
        // record last cycles dfCtr
        oldMseLoc.set(dfCtr);
        dfCtr = getPlInterSect(mseLocInWorld, eyeToMse);
    }//buildCanvas()
    
    public myVector getDrawSNorm() {return drawSNorm;}
    public myVectorf getDrawSNorm_f() {return new myVectorf(drawSNorm);}
    public myPoint[] getCanvasCorners() {return canvas3D;}
    
    public myVector getEyeToMse() {return eyeToMse;}
    public myVectorf getEyeToMse_f() {return new myVectorf(eyeToMse.x,eyeToMse.y,eyeToMse.z);}

    /**
     * find pt in canvas drawing plane that corresponds with point and camera eye normal
     * @param pt point to find intersection of
     * @param unitT camera eye normal
     * @return
     */
    public myPoint getPlInterSect(myPoint pt, myVector unitT){
         // return intersection point in canvas plane
        return MyMathUtils.intersectPlane(pt, unitT, canvas3D[0],canvas3D[1],canvas3D[2]);        
    }//getPlInterSect    

    /**
     * Mouse location in world at given depth
     * @return
     */
    public myPoint getMseLocInWorld() {
        float ctrDepth = ri.getSceenZ((float)scrCtrInWorld.x, (float)scrCtrInWorld.y, (float)scrCtrInWorld.z);
        int[] mse = ri.getMouse_Raw_Int();
        return ri.getWorldLoc(mse[0],mse[1],ctrDepth);        
    }

    /**
     * Retrieve the mouse location projected onto the cavnas as a 3d point
     * @return
     */
    public myPoint getMseLoc(){return new myPoint(dfCtr);    }
    /**
     * Retrieve the mouse location projected onto the cavnas as a 3d point
     * @return
     */
    public myPointf getMseLoc_f(){return new myPointf(dfCtr.x,dfCtr.y,dfCtr.z);    }
    /**
     * Retrieve the old mouse location projected onto the cavnas as a 3d point
     * @return
     */
    public myPoint getOldMseLoc(){return new myPoint(oldMseLoc);    }  
    /**
     * Retrieve the old mouse location projected onto the cavnas as a 3d point
     * @return
     */
    public myPointf getOldMseLoc_f(){return new myPointf(oldMseLoc.x, oldMseLoc.y, oldMseLoc.z);    }    
    /**
     * 
     * @return
     */
    public myVector getMseDragVec(){return new myVector(oldMseLoc,dfCtr);}
    
    /**
     * 
     * @return
     */
    public myVectorf getMseDragVec_f(){return new myVectorf(oldMseLoc,dfCtr);}
    
    /**
     * relative to passed origin
     * @param glbTrans
     * @return
     */
    public myPoint getMseLoc(myPoint glbTrans){return myPoint._sub(dfCtr, glbTrans);    }
    /**
     * move by passed translation
     * @param glbTrans
     * @return
     */
    public myPointf getTransMseLoc(myPointf glbTrans){return myPointf._add(dfCtr, glbTrans);    }
    /**
     * dist from mouse to passed location
     * @param glbTrans
     * @return
     */
    public float getMseDist(myPointf glbTrans){return new myVectorf(dfCtr, glbTrans).magn;    }
    /**
     * 
     * @param glbTrans
     * @return
     */
    public myPoint getOldMseLoc(myPoint glbTrans){return myPoint._sub(oldMseLoc, glbTrans);    }
    /**
     * 
     * @return
     */
    public myPoint getEyeInWorld() {return eyeInWorld;}
    
    //get normalized ray from eye loc to mouse loc
    public myVectorf getEyeToMouseRay_f() {
        myVectorf ray = new myVectorf(eyeInWorld, dfCtr);
        return ray._normalize();
    }
    
    /**
     * draw a translucent representation of a canvas plane ortho to eye-to-mouse vector
     * @param color color to paint the canvas - should be translucent (Alpha should be no more than 80), 
     *                 light for dark backgrounds and dark for light backgrounds. 
     */
    public final void drawCanvas(int[] color) {
        myPointf[] bndedCanvas3D = AppMgr.buildPlaneBoxBounds(canvas3D);
        ri.disableLights();
        ri.pushMatState();
        ri.gl_beginShape(GL_PrimitiveType.GL_LINE_LOOP);
        ri.gl_setFill(color, color[3]);
        ri.setNoStroke();
        ri.gl_normal(eyeToMse);
        for(int i = bndedCanvas3D.length-1;i>=0;--i){             ri.gl_vertex(bndedCanvas3D[i]);}
        ri.gl_endShape(true);
        ri.popMatState();
        ri.enableLights();
    }

    private final void drawText(Base_DispWindow win, String str, float x, float y, float z){
        ri.pushMatState();
            ri.setFill(mseFillClr,mseFillClr[3]);
            win.unSetCamOrient();
            ri.translate(x,y,z);
            ri.showText(str,0,0,0);        
        ri.popMatState();    
    }//drawText    

    public void drawMseEdge(Base_DispWindow win, boolean projOnBox){//draw mouse sphere and edge normal to cam eye through mouse sphere 
        ri.pushMatState();
            ri.setStrokeWt(1f);
            ri.setStroke(255, 0,255, 255);
            //draw line through mouse point and eye location in world    
            ri.drawLine(eyeInWorld, dfCtr);
            ri.translate(dfCtr);
            //project mouse point on bounding box walls if appropriate
            if(projOnBox){AppMgr.drawProjOnBox(dfCtr);}
            AppMgr.drawRGBAxesWithEnds(10000,1f, myPointf.ZEROPT, 100);//
            //draw center point
            ri.showPtAsSphere(myPointf.ZEROPT,3.0f, 5, IGraphicsAppInterface.gui_Black, IGraphicsAppInterface.gui_Black);
            drawText(win, ""+dfCtr+ "|fr:"+ri.getFrameRate(),4.0f, 15.0f, 4.0f);
        ri.popMatState();            
    }//drawMseEdge        
}//class Disp3DCanvas


