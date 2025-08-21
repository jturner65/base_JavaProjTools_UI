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
     * Owning application manager
     */
    private GUI_AppManager AppMgr;

    /**
     * GL-interface for rendering and screen<->world coords transforms
     */
    private static IGraphicsAppInterface ri;
    
    /**
     * Screen ctr location in world coords
     */        
    private myPoint scrCtrInWorld;
    /**
     * Eye location in the world
     */
    private myPoint eyeInWorld; 
    /**
     * Previous mse location on canvas
     */
    private myPoint oldMseInWorldOnCanvas;
    /**
     * Current mouse location projected onto current canvas
     */
    private myPoint mseInWorldOnCanvas;                                                        //mouse location projected onto current drawing canvas

    private final float canvasDim = 15000,
            canvasDimOvSqrt2 = MyMathUtils.INV_SQRT_2_F * canvasDim;             //canvas dimension for "virtual" 3d        
    private myPoint[] canvas3D;                                                    //3d plane, normal to camera eye, to be used for drawing - need to be in "view space" not in "world space", so that if camera moves they don't change
    private myVector eyeToMse,                                                    //eye to 2d mouse location 
                    eyeToCtr,                                                    //vector from eye to center of cube, to be used to determine which panels of bounding box to show or hide
                    drawSNorm;                                                    //current normal of viewport/screen
        
    private int viewDimW, viewDimH,viewDimW2, viewDimH2;
    
    private int[] mseFillClr;
     
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
        oldMseInWorldOnCanvas  = new myPoint();
        mseInWorldOnCanvas = new myPoint();                                            //mouse location projected onto current drawing canvas
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
        eyeInWorld = ri.getWorldLoc(viewDimW2, viewDimH2, AppMgr._camEyeZ);
        //eyeInWorld =myPoint._add(rawScrCtrInWorld, myPoint._dist( ri.pick(0,0,-1), rawScrCtrInWorld), drawSNorm);                                //location of "eye" in world space
        eyeToCtr.set(eyeInWorld, rawScrCtrInWorld);
        scrCtrInWorld = getPtIntersectWithCanvas(rawScrCtrInWorld, myVector._normalize(eyeToCtr));
        // record last cycle's mouseLocOnCanvas
        oldMseInWorldOnCanvas.set(mseInWorldOnCanvas);
        
        //get mouse loc in world at scrCtrInWorld depth
        myPoint mseLocInWorld = _getMseLocInWorldAtScreenCtr();
        // small difference between these two.
        mseInWorldOnCanvas = getPtIntersectWithCanvas(mseLocInWorld, eyeToMse);
        //unit vector in world coords of "eye" to mouse location on canvas
        eyeToMse.set(eyeInWorld, mseLocInWorld);        
        eyeToMse._normalize();
        
    }//buildCanvas()

    /**
     * find pt in canvas drawing plane that corresponds with point and camera eye normal
     * @param pt point to find intersection of
     * @param unitT camera eye normal
     * @return
     */
    public myPoint getPtIntersectWithCanvas(myPoint pt, myVector unitT){
         // return intersection point in canvas plane
        return MyMathUtils.intersectPlane(pt, unitT, canvas3D[0],canvas3D[1],canvas3D[2]);        
    }//getPlInterSect    

    /**
     * Mouse location in world at scrCtrInWorld depth
     * @return
     */
    private myPoint _getMseLocInWorldAtScreenCtr() {
        float ctrDepth = ri.getSceenZ((float)scrCtrInWorld.x, (float)scrCtrInWorld.y, (float)scrCtrInWorld.z);
        int[] mse = ri.getMouse_Raw_Int();
        return ri.getWorldLoc(mse[0],mse[1],ctrDepth);        
    }
    
    
    
    /**
     * Retrieve a double-based point of the eye location in world space
     * @return
     */
    public myPoint getEyeInWorld() {return new myPoint(eyeInWorld);}
    
    /**
     * Retrieve a float-based point of the eye location in world space
     * @return
     */
    public myPointf getEyeInWorld_f() {return new myPointf(eyeInWorld.x, eyeInWorld.y, eyeInWorld.z);}
    
    /**
     * Retrieve the double-based vector for the normal to the viewport/canvas 
     * @return
     */
    public myVector getDrawSNorm() {return new myVector(drawSNorm);}
    /**
     * Retrieve the float-based vector for the normal to the viewport/canvas 
     * @return
     */
    public myVectorf getDrawSNorm_f() {return new myVectorf(drawSNorm);}
    /**
     * Retrieve the array of 4 corners of the canvas, which is parallel to the viewport at some z distance
     * @return
     */
    public myPoint[] getCanvasCorners() {
        return new myPoint[] {
                new myPoint(canvas3D[0]),
                new myPoint(canvas3D[1]),
                new myPoint(canvas3D[2]),
                new myPoint(canvas3D[3]),
        };
    }
    /**
     * Retrieve double-based normalized vector from eye to mouse on canvas in world space
     * @return
     */
    public myVector getEyeToMse() {return new myVector(eyeToMse);}
    /**
     * Retrieve float-based normalized vector from eye to mouse on canvas in world space
     * @return
     */
    public myVectorf getEyeToMse_f() {return new myVectorf(eyeToMse.x,eyeToMse.y,eyeToMse.z);}
    /**
     * Retrieve the mouse location projected onto the cavnas as a 3d point
     * @return
     */
    public myPoint getMseLoc(){return new myPoint(mseInWorldOnCanvas);    }
    /**
     * Retrieve the mouse location projected onto the cavnas as a 3d point
     * @return
     */
    public myPointf getMseLoc_f(){return new myPointf(mseInWorldOnCanvas.x,mseInWorldOnCanvas.y,mseInWorldOnCanvas.z);    }
    /**
     * Retrieve a double-based point of the old mouse location projected onto the cavnas as a 3d point
     * @return
     */
    public myPoint getOldMseLoc(){return new myPoint(oldMseInWorldOnCanvas);    }  
    /**
     * Retrieve a float-based point of the old mouse location projected onto the cavnas as a 3d point
     * @return
     */
    public myPointf getOldMseLoc_f(){return new myPointf(oldMseInWorldOnCanvas.x, oldMseInWorldOnCanvas.y, oldMseInWorldOnCanvas.z);    }    
    /**
     * Retrieve a double-based vector of the mouse drag from old location to current location, on canvas in world space
     * @return
     */
    public myVector getMseDragVec(){return new myVector(oldMseInWorldOnCanvas,mseInWorldOnCanvas);}
    /**
     * Retrieve a float-based vector of the mouse drag from old location to current location, on canvas in world space
     * @return
     */
    public myVectorf getMseDragVec_f(){return new myVectorf(oldMseInWorldOnCanvas,mseInWorldOnCanvas);}    
    /**
     * Retrieve a double-based point of mouse location on drawable canvas relative to passed origin
     * @param origin
     * @return
     */
    public myPoint getMseLocRelToOrigin(myPoint origin){return myPoint._sub(mseInWorldOnCanvas, origin);    }
    /**
     * Retrieve a float-based point of mouse location on drawable canvas relative to passed origin
     * @param origin
     * @return
     */
    public myPointf getMseLocRelToOrigin_f(myPointf origin){
        return new myPointf(mseInWorldOnCanvas.x-origin.x,mseInWorldOnCanvas.y-origin.y,mseInWorldOnCanvas.z-origin.z);    
    }
    
    /**
     * Retrieve a double-based point of mouse location transformed by passed translation
     * @param glbTrans
     * @return
     */
    public myPoint getTransMseLoc(myPoint glbTrans){return myPoint._add(mseInWorldOnCanvas, glbTrans);    }    
    /**
     * Retrieve a float-based point of mouse location transformed by passed translation
     * @param glbTrans
     * @return
     */
    public myPointf getTransMseLoc_f(myPointf glbTrans){return myPointf._add(mseInWorldOnCanvas, glbTrans);    }
    /**
     * Retrieve a double-based point of the last frame's mouse location transformed by passed translation
     * @param glbTrans
     * @return
     */
    public myPoint getOldMseLocRelToOrigin(myPoint origin){return myPoint._sub(oldMseInWorldOnCanvas, origin);    }
    /**
     * Retrieve a float-based point of the last frame's mouse location transformed by passed translation
     * @param glbTrans
     * @return
     */
    public myPointf getOldMseLocRelToOrigin_f(myPointf origin){
        return new myPointf(oldMseInWorldOnCanvas.x-origin.x,oldMseInWorldOnCanvas.y-origin.y,oldMseInWorldOnCanvas.z-origin.z);    
    }

    /**
     * Calc dist from mouse to passed location
     * @param glbTrans
     * @return
     */
    public double getMseDist(myPoint glbTrans){return new myVector(mseInWorldOnCanvas, glbTrans).magn;    }
    /**
     * Calc dist from mouse to passed location
     * @param glbTrans
     * @return
     */
    public float getMseDist(myPointf glbTrans){return new myVectorf(mseInWorldOnCanvas, glbTrans).magn;    }
    
    
    /**
     * draw a translucent representation of a canvas plane ortho to eye-to-mouse vector
     * @param color color to paint the canvas - should be translucent (Alpha should be no more than 80), 
     *                 light for dark backgrounds and dark for light backgrounds. 
     */
    public final void drawCanvas(int[] color) {
        myPointf[] bndedCanvas3D = AppMgr.buildPlaneBoxBounds(drawSNorm, canvas3D);
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
            ri.drawLine(eyeInWorld, mseInWorldOnCanvas);
            ri.translate(mseInWorldOnCanvas);
            //project mouse point on bounding box walls if appropriate
            if(projOnBox){AppMgr.drawProjOnBox(mseInWorldOnCanvas);}
            AppMgr.drawRGBAxesWithEnds(10000,1f, myPointf.ZEROPT, 100);//
            //draw center point
            ri.showPtAsSphere(myPointf.ZEROPT,3.0f, 5, IGraphicsAppInterface.gui_Black, IGraphicsAppInterface.gui_Black);
            drawText(win, ""+mseInWorldOnCanvas+ "|fr:"+ri.getFrameRate(),4.0f, 15.0f, 4.0f);
        ri.popMatState();            
    }//drawMseEdge        
}//class Disp3DCanvas


