package base_UI_Objects.renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

import com.jogamp.newt.opengl.GLWindow;

import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_Render_Interface.GL_PrimStyle;
import base_Render_Interface.IGraphicsAppInterface;
import base_Render_Interface.IMeshInterface;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.mesh.ProcessingShape;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PShape;
import processing.event.MouseEvent;
import processing.opengl.PGL;
import processing.opengl.PGraphics3D;

public final class ProcessingRenderer extends processing.core.PApplet implements IGraphicsAppInterface {
    
    private static GUI_AppManager AppMgr;
        
    private final float frate = 120;            //target frame rate - # of playback updates per second
        
    /**
     * map of giant spheres encapsulating entire 3D scene - allows for different ones for each 3D window
     */
    private HashMap<Integer, PShape> bgrndSphereAra;    
    /**
     * map of background colors for every window
     */
    private HashMap<Integer, int[]> bgrndColorAra;
    
        
    ////////////////////////
    // code
    
    ///////////////////////////////////
    /// inits
    ///////////////////////////////////

    /**
     * needs main to run project - do not modify this code in any way
     * needs to be called by instancing GUI_AppManager class
     * @param _appMgr
     * @param passedArgs
     */
    public final static void _invokedMain(GUI_AppManager _appMgr, String[] passedArgs) {    
        String[] appletArgs = new String[]{"base_UI_Objects.renderer.ProcessingRenderer" };
        AppMgr = _appMgr;
        if (passedArgs != null) {processing.core.PApplet.main(processing.core.PApplet.concat(appletArgs, passedArgs)); } else {processing.core.PApplet.main(appletArgs);            }
        
    }//main    

    /**
     * Initialize render interface implementation.
     */
    @Override
    public final void initRenderInterface() {
        bgrndSphereAra = new HashMap<Integer, PShape>();
        bgrndColorAra = new HashMap<Integer, int[]>();
    }
    
    //processing being run in eclipse uses settings for variable size dimensions
    @Override
    public final void settings(){    
        AppMgr.setIGraphicsAppInterface(this);
        int[] desDims = AppMgr.getIdealAppWindowDims();
        size(desDims[0], desDims[1],P3D);    
        //allow user to set smoothing
        AppMgr.setSmoothing();
    }
    
    /**
     * Set the background painted color for specified window idx
     * @param idx idx to set color for
     * @param r
     * @param g
     * @param b
     * @param alpha
     */    
    @Override
    public final void setRenderBackground(int idx, int r, int g, int b, int alpha) {
        bgrndColorAra.put(idx, new int[] {r,g,b,alpha});
    }
    
    /**
     * Load a background "skybox" sphere using texture from filename
     * @param idx The idx where to put the image in the hashmap
     * @param filename Texture to use for background skybox sphere
     */
    @Override
    public final void loadBkgndSphere(int idx, String filename) {
        //save current sphere detail
        int sPrevDet = getSphereDetail();
        setSphereDetail(100);
        PImage bgrndTex = loadImage(filename);
        PShape bgrndSphere = createShape(PConstants.SPHERE, 10000);
        bgrndSphere.setTexture(bgrndTex);
        bgrndSphere.rotate(MyMathUtils.HALF_PI_F,-1,0,0);
        bgrndSphere.setStroke(false);    
        bgrndSphereAra.put(idx, bgrndSphere);
        setRenderBackground(idx, 255, 255, 255, 255);        
        shape(bgrndSphere);    
        //reset detail
        setSphereDetail(sPrevDet);
    }
    
    @Override
    public final void setup() {
        colorMode(RGB, 255, 255, 255, 255);
        //setup default stroke ends.  ROUND is very slow, SQUARE  makes points invisible    
        strokeCap(PROJECT);
        textSize(AppMgr.getTextSize());
        textureMode(NORMAL);            
        setDefaultRectMode();
        sphereDetail(4);
        //Set up application
        AppMgr.setupApp(width, height);

        //needs to be the last thing called in setup, to avoid timeout 5000ms issue
        frameRate(frate);
    }//setup()
    
    private void setDefaultRectMode() {    rectMode(CORNER);}
    
    /**
     * Draw the specified window's background color
     * @param idx the idx of the background to draw
     */
    @Override
    public final void drawRenderBackground(int idx) {
        int[] bGroundAra = bgrndColorAra.get(idx);
        if (bGroundAra == null) {
            bGroundAra = getClr(gui_White, 255);
        }
        super.background(bGroundAra[0],bGroundAra[1],bGroundAra[2],bGroundAra[3]);
    }
    /**
     * Set loaded background sphere as skybox
     * @param idx the idx of the skybox to draw
     */
    @Override
    public final void drawBkgndSphere(int idx) {
        PShape shape = bgrndSphereAra.get(idx);
        drawRenderBackground(idx);
        if(shape==null) {
            AppMgr.msgObj.dispErrorMessage("ProcessingRenderer","drawBkgndSphere","ERROR! No background sphere specified for idx :"+idx);
            return;
        }
        shape(shape);    
    }
        
    /**
     * Return the underlying GL Window for this JOGL 
     * @return
     */
    @Override
    public GLWindow getGLWindow() {
        return (GLWindow)getSurface().getNative();
    }
    ///////////////////////////////////////////
    // draw routines
    protected int pushPopAllDepth = 0, pushPopJustStyleDepth = 0;
    /**
     * Retrieve current push matrix/style depth - for debugging purposes.
     * @return
     */
    @Override
    public final int getCurrentPushMatDepth() {return pushPopAllDepth;}
    /**
     * Retrieve current push style only depth - for debugging purposes.
     * @return
     */
    @Override
    public final int getCurrentPushStyleDepth() {return pushPopJustStyleDepth;}
    
    /**
     * push matrix, and style (if available) - must be paired with pop matrix/style calls
     */
    @Override
    public final int pushMatState() {    super.pushMatrix();super.pushStyle();return ++pushPopAllDepth;}
    /**
     * pop style (if supported) and matrix - must be called after equivalent pushes
     */
    @Override
    public final int popMatState() {    super.popStyle();super.popMatrix();    return --pushPopAllDepth;}
    
    /**
     * push current style/color params onto "style stack" (save current settings)
     */    
    @Override
    public int pushJustStyleState() {    super.pushStyle();return ++pushPopJustStyleDepth;}
    /**
     * pop current style/color params from "style stack" (restore/overwrite with last saved settings)
     */
    @Override
    public int popJustStyleState(){        super.popStyle();return --pushPopJustStyleDepth;}
    

    /**
     * main draw loop - override if handling draw differently
     */
    @Override
    public final void draw(){
        //returns whether actually drawn or not
        if(!AppMgr.mainSimAndDrawLoop()) {return;}
    }//draw    
    
    /**
     * Sets window title. AppMgr constructs window title
     * @param windowTitle string to display in the window titlebar
     */
    @Override
    public final void setWindowTitle(String winTitle) {
        //display window title
        surface.setTitle(winTitle);        
    }    
    
    /**
     * draw a translucent representation of a canvas plane ortho to eye-to-mouse vector
     * @param eyeToMse vector 
     * @param canvas3D bounded points to draw polygon edge of canvas
     * @param color color to paint the canvas - should be translucent (Alpha should be no more than 80), 
     *                 light for dark backgrounds and dark for light backgrounds. 
     */
    @Override
    public final void drawCanvas(myVector eyeToMse, myPointf[] canvas3D, int[] color){
        disableLights();
        pushMatState();
        gl_beginShape(GL_PrimStyle.GL_LINE_LOOP);
        gl_setFill(color, color[3]);
        setNoStroke();
        gl_normal(eyeToMse);
        for(int i =canvas3D.length-1;i>=0;--i){        //build invisible canvas to draw upon
             gl_vertex(canvas3D[i]);
         }
         gl_endShape(true);
         popMatState();
         enableLights();
    }//drawCanvas


    /**
     * set perspective matrix based on frustum for camera
     * @param left left coordinate of the clipping plane
     * @param right right coordinate of the clipping plane
     * @param bottom bottom coordinate of the clipping plane
     * @param top top coordinate of the clipping plane
     * @param near near component of the clipping plane (> 0)
     * @param far far component of the clipping plane (> near)
     */
    @Override
    public final void setFrustum(float left, float right, float bottom, float top, float near, float far) {
        super.frustum(left, right, bottom, top, near, far);
    }
    
    /**
     * set perspective projection matrix for camera
     * @param fovy Vertical FOV
     * @param ar Aspect Ratio 
     * @param zNear Z position of near clipping plane
     * @param zFar Z position of far clipping plane 
     */
    @Override
    public final void setPerspective(float fovy, float ar, float zNear, float zFar) {
        super.perspective(fovy, ar, zNear, zFar);
    }
    
    /**
     * Set orthographic projection matrix for 2D camera
     * @param left left plane of clipping volume
     * @param right right plane of the clipping volume
     * @param bottom bottom plane of the clipping volume
     * @param top top plane of the clipping volume
     */
    @Override
    public final void setOrtho(float left, float right, float bottom, float top) {
        super.ortho(left, right, bottom, top);
    }
    /**
     * set orthographic projection matrix for 3D camera
     * @param left left plane of clipping volume
     * @param right right plane of the clipping volume
     * @param bottom bottom plane of the clipping volume
     * @param top top plane of the clipping volume
     * @param near maximum distance from the origin to the viewer
     * @param far maximum distance from the origin away from the viewer
     */
    @Override
    public final void setOrtho(float left, float right, float bottom, float top, float near, float far) {
        super.ortho(left, right, bottom, top, near, far);
    }
    
    /**
     * Set normal for smooth shading
     * @param x
     * @param y
     * @param z
     */
    @Override
    public final void gl_normal(float x, float y, float z) {super.normal(x,y,z);}                                          // changes normal for smooth shading
    /**
     * Set a vertex for drawing shapes
     * @param P
     */
    @Override
    public final void gl_vertex(float x, float y, float z) {super.vertex(x,y,z);}                                             // vertex for shading or drawing

    /**
     * set fill color by value during shape building
     * @param clr 1st 3 values denot integer color vals
     * @param alpha 
     */
    @Override
    public final void gl_setFill(int r, int g, int b, int alpha) {super.fill(r,g,b,alpha);}
    /**
     * Set to have no fill color for a specified object (within gl_beginShape/gl_endShape)
     */
    @Override
    public final void gl_setNoFill() {super.noFill();}
    /**
     * set stroke color by value during shape building
     * @param clr rgba
     * @param alpha 
     */
    @Override
    public final void gl_setStroke(int r, int g, int b, int alpha) {super.stroke(r,g,b,alpha);}
    /**
     * Set to have no stroke color for a specified object (within gl_beginShape/gl_endShape)
     */
    @Override
    public final void gl_setNoStroke() {super.noStroke();}
    /**
     * Set stroke weight for a specified object (within gl_beginShape/gl_endShape)
     * @param wt
     */
    @Override
    public final void gl_setStrokeWt(float wt) {super.strokeWeight(wt);}
    
    /**
     * type needs to be -1 for blank, otherwise should be specified in PConstants
     * 
     * (from PConstants) - these are allowed elements in glBegin function
          static final int POINTS          = 3;   // vertices
          static final int LINES           = 5;   // beginShape(), createShape()
          static final int LINE_STRIP      = 50;  // beginShape()
          static final int LINE_LOOP       = 51;
          static final int TRIANGLES       = 9;   // vertices
          static final int TRIANGLE_STRIP  = 10;  // vertices
          static final int TRIANGLE_FAN    = 11;  // vertices
          
          static final int QUADS           = 17;  // vertices
          static final int QUAD_STRIP      = 18;  // vertices
          
          static final int POLYGON         = 20;  // 
     * 
     * 
     */
    @Override
    public final void gl_beginShape(GL_PrimStyle primType) {
        switch (primType) {
            case GL_POINTS : {          beginShape(PConstants.POINTS);         return;}
            case GL_LINES : {           beginShape(PConstants.LINES);          return;}
            case GL_LINE_LOOP : {
                //Processing does not support line loop, so treat as polygon
                beginShape(POLYGON);
                return;
            }
            case GL_LINE_STRIP : {
                //Processing does not support line_strip, treat as lines
                beginShape(LINES);
                break;
            }
            case GL_TRIANGLES : {       beginShape(PConstants.TRIANGLES);      return;}
            case GL_TRIANGLE_STRIP : {  beginShape(PConstants.TRIANGLE_STRIP); return;}
            case GL_TRIANGLE_FAN : {    beginShape(PConstants.TRIANGLE_FAN);   return;}
            case GL_QUADS : {           beginShape(PConstants.QUADS);          return;}
            case GL_QUAD_STRIP : {      beginShape(PConstants.QUAD_STRIP);     return;}
            case POINT : {              beginShape(PConstants.POINT);          return;}
            case LINE : {               beginShape(PConstants.LINE);           return;}
            case TRIANGLE : {           beginShape(PConstants.TRIANGLE);       return;}
            case QUAD : {               beginShape(PConstants.QUAD);           return;}
            case RECT : {               beginShape(PConstants.RECT);           return;}
            case ELLIPSE : {            beginShape(PConstants.ELLIPSE);        return;}
            case ARC : {                beginShape(PConstants.ARC);            return;}
            case SPHERE : {             beginShape(PConstants.SPHERE);         return;}
            case BOX : {                beginShape(PConstants.BOX);            return;}
            default : {                 beginShape(PConstants.POLYGON);        return;}        
        };
    }//gl_beginShape
    /**
     * type needs to be -1 for blank, otherwise will be CLOSE, regardless of passed value
     */
    @Override
    public final void gl_endShape(boolean isClosed) {        
        if(isClosed) {            endShape(CLOSE);        }
        else {                endShape();        }
    }
    
    @Override
    public final void drawSphere(float rad) {sphere(rad);}
    //internal value tracking current sphere detail
    private int sphereDtl = 4;

    @Override
    public final void setSphereDetail(int det) {sphereDtl=det;sphereDetail(det);}

    @Override
    public int getSphereDetail() {return sphereDtl;}
    
    /**
     * draw a 2 d ellipse 
     * @param x,y,x rad, y rad
     */
    @Override
    public final void drawEllipse2D(float x, float y, float xr, float yr) {ellipse(x,y,xr,yr);}

    
    @Override
    public final void drawLine(float x1, float y1, float z1, float x2, float y2, float z2){line(x1,y1,z1,x2,y2,z2 );}
    @Override
    public final void drawLine(myPointf a, myPointf b, int stClr, int endClr){
        gl_beginShape(GL_PrimStyle.GL_LINES);
        gl_setStrokeWt(1.0f);
        gl_setStroke(getClr(stClr, 255), 255);
        gl_vertex(a);
        gl_setStroke(getClr(endClr,255), 255);
        gl_vertex(b);
        gl_endShape();
    }
    @Override
    public final void drawLine(myPointf a, myPointf b, int[] stClr, int[] endClr){
        gl_beginShape(GL_PrimStyle.GL_LINES);
        gl_setStrokeWt(1.0f);
        gl_setStroke(stClr, 255);
        gl_vertex(a);
        gl_setStroke(endClr,255);
        gl_vertex(b);
        gl_endShape();
    }
    
    /**
     * draw a cloud of points with passed color values as an integrated shape
     * @param numPts number of points to draw
     * @param ptIncr incrementer between points, to draw only every 2nd, 3rd or more'th point
     * @param ptClrIntAra 2d array of per point 3-color stroke values
     * @param ptPosX per point x value
     * @param ptPosY per point y value
     * @param ptPosZ per point z value
     */
    @Override
    public final void drawPointCloudWithColors(int numPts, int ptIncr, int[][] ptClrIntAra, float[] ptPosX, float[] ptPosY, float[] ptPosZ) {
        gl_beginShape(GL_PrimStyle.GL_POINTS);
        for(int i=0;i<=numPts-ptIncr;i+=ptIncr) {    
            gl_setStroke(ptClrIntAra[i][0], ptClrIntAra[i][1], ptClrIntAra[i][2], 255);
            gl_vertex(ptPosX[i], ptPosY[i], ptPosZ[i]);
        }
        gl_endShape();
    }//drawPointCloudWithColors    
    
    /**
     * draw a cloud of points with all points having same color value as an integrated shape
     * @param numPts number of points to draw
     * @param ptIncr incrementer between points, to draw only every 2nd, 3rd or more'th point
     * @param ptClrIntAra array of 3-color stroke values for all points
     * @param ptPosX per point x value
     * @param ptPosY per point y value
     * @param ptPosZ per point z value
     */
    @Override
    public final void drawPointCloudWithColor(int numPts, int ptIncr, int[] ptClrIntAra, float[] ptPosX, float[] ptPosY, float[] ptPosZ) {
        gl_beginShape(GL_PrimStyle.GL_POINTS);
        gl_setStroke(ptClrIntAra[0], ptClrIntAra[1], ptClrIntAra[2], 255);
        for(int i=0;i<=numPts-ptIncr;i+=ptIncr) {    
            gl_vertex(ptPosX[i], ptPosY[i], ptPosZ[i]);
        }
        gl_endShape();
    }//drawPointCloudWithColor
    
    /**
     * draw a box centered at origin with passed dimensions, in 3D
     */
    @Override
    public final void drawBox3D(int x, int y, int z) {box(x,y,z);};
    /**
     * draw a rectangle in 2D using the passed values as x,y,w,h
     * @param a 4 element array : x,y,w,h
     */
    @Override
    public final void drawRect(float a, float b, float c, float d){rect(a,b,c,d);}                //rectangle from array of floats : x, y, w, h
    
    /**
     * draw a circle centered at P with specified radius r in plane proscribed by passed axes using n number of points
     * @param P center
     * @param r radius
     * @param I x axis
     * @param J y axis
     * @param n # of points to use
     */
    @Override
    public final void drawCircle3D(myPoint P, double r, myVector I, myVector J, int n) {
        myPoint[] pts = MyMathUtils.buildCircleInscribedPoints(P,r,I,J,n);
        pushMatState();noFill(); drawShapeFromPts(pts);popMatState();
    }
    @Override
    public final void drawCircle3D(myPointf P, float r, myVectorf I, myVectorf J, int n) {
        myPointf[] pts = MyMathUtils.buildCircleInscribedPoints(P,r,I,J,n);
        pushMatState();noFill(); drawShapeFromPts(pts);popMatState();
    } 
    
    /**
     * draw a 6 pointed star centered at ri inscribed in circle radius r
     */
    @Override
    public final void drawStar2D(myPointf p, float r) {
        myPointf[] pts = MyMathUtils.buildCircleInscribedPoints(p,r,myVectorf.FORWARD,myVectorf.RIGHT,6);
        drawTriangle2D(pts[0], pts[2],pts[4]);
        drawTriangle2D(pts[1], pts[3],pts[5]);
    }
    /**
     * draw a triangle at 3 locations in 2D (only uses x,y)
     * @param a
     * @param b
     * @param c
     */
    @Override
    public final void drawTriangle2D(myPointf a, myPointf b, myPointf c) {triangle(a.x,a.y, b.x, b.y, c.x, c.y);}
    /**
     * draw a triangle at 3 locations in 2D (only uses x,y)
     * @param a
     * @param b
     * @param c
     */
    @Override
    public final void drawTriangle2D(myPoint a, myPoint b, myPoint c) {triangle((float)a.x,(float)a.y,(float) b.x, (float)b.y,(float) c.x,(float) c.y);}
    
    
    @Override
    public final void drawCylinder_NoFill(myPoint A, myPoint B, double r, int clr1, int clr2) {
        myPoint[] vertList = AppMgr.buildCylVerts(A, B, r);
        int[] c1 = getClr(clr1, 255);
        int[] c2 = getClr(clr2, 255);
        noFill();
        gl_beginShape(GL_PrimStyle.GL_QUAD_STRIP);
            for(int i=0; i<vertList.length; i+=2) {
                gl_setStroke(c1[0],c1[1],c1[2],255);
                gl_vertex(vertList[i]);
                gl_setStroke(c2[0],c2[1],c2[2],255);
                gl_vertex(vertList[i+1]);}
        gl_endShape();
    }
    @Override
    public final void drawCylinder_NoFill(myPointf A, myPointf B, float r, int clr1, int clr2) {
        myPointf[] vertList = AppMgr.buildCylVerts(A, B, r);
        int[] c1 = getClr(clr1, 255);
        int[] c2 = getClr(clr2, 255);
        noFill();
        gl_beginShape(GL_PrimStyle.GL_QUAD_STRIP);
            for(int i=0; i<vertList.length; i+=2) {
                gl_setStroke(c1[0],c1[1],c1[2],255);
                gl_vertex(vertList[i]); 
                gl_setStroke(c2[0],c2[1],c2[2],255);
                gl_vertex(vertList[i+1]);}
        gl_endShape();
    }

    @Override
    public final void drawCylinder(myPoint A, myPoint B, double r, int clr1, int clr2) {
        myPoint[] vertList = AppMgr.buildCylVerts(A, B, r);
        int[] c1 = getClr(clr1, 255);
        int[] c2 = getClr(clr2, 255);
        gl_beginShape(GL_PrimStyle.GL_QUAD_STRIP);
            for(int i=0; i<vertList.length; i+=2) {
                gl_setFill(c1[0],c1[1],c1[2],255);        
                gl_vertex(vertList[i]); 
                gl_setFill(c2[0],c2[1],c2[2],255);    
                gl_vertex(vertList[i+1]);}
        gl_endShape();
    }
    
    @Override
    public final void drawCylinder(myPointf A, myPointf B, float r, int clr1, int clr2) {
        myPointf[] vertList = AppMgr.buildCylVerts(A, B, r);
        int[] c1 = getClr(clr1, 255);
        int[] c2 = getClr(clr2, 255);
        gl_beginShape(GL_PrimStyle.GL_QUAD_STRIP);
        for(int i=0; i<vertList.length; i+=2) {
            gl_setFill(c1[0],c1[1],c1[2],255);        
            gl_vertex(vertList[i]); 
            gl_setFill(c2[0],c2[1],c2[2],255);        
            gl_vertex(vertList[i+1]);}
        gl_endShape();
    }
    
    //////////////////////////////////
    // end draw routines
    
////////////////////////
// transformations
    
    @Override
    public final void translate(float x, float y){super.translate(x,y);}
    @Override
    public final void translate(float x, float y, float z){super.translate(x,y,z);}
    
    /**
     * this will translate the passed box dimensions to keep them on the screen
     * using ri as start point and rectDims[2] and rectDims[3] as width and height
     * @param P starting point
     * @param rectDims box dimensions 
     */
    @Override
    public final void transToStayOnScreen(myPointf P, float[] rectDims) {
        float xLocSt = P.x + rectDims[0], xLocEnd = xLocSt + rectDims[2];
        float yLocSt = P.y + rectDims[1], yLocEnd = yLocSt + rectDims[3];
        float transX = 0.0f, transY = 0.0f;
        if (xLocSt < 0) {    transX = -1.0f * xLocSt;    } else if (xLocEnd > width) {transX = width - xLocEnd - 20;}
        if (yLocSt < 0) {    transY = -1.0f * yLocSt;    } else if (yLocEnd > height) {transY = height - yLocEnd - 20;}
        super.translate(transX,transY);        
    }

    @Override
    public final void rotate(float thet, float x, float y, float z) {super.rotate(thet, x, y, z);}

    @Override
    public final void scale(float x) {super.scale(x);}
    @Override
    public final void scale(float x,float y) {super.scale(x, y);}
    @Override
    public final void scale(float x,float y,float z) {super.scale(x,y,z);}

    

    ////////////////////////
    // end transformations
    
//////////////////////////////////////////////////////
/// user interaction
//////////////////////////////////////////////////////    
    /**
     * called by papplet super
     */
    @Override
    public final void keyPressed(){
        if(key==CODED) {    AppMgr.checkAndSetSACKeys(keyCode);        } 
        else {                AppMgr.sendKeyPressToWindows(key,keyCode);    }
    }    
    /**
     * called by papplet super
     */
    @Override
    public final void keyReleased(){        AppMgr.checkKeyReleased(key==CODED, keyCode);}    
    /**
     * called by papplet super
     */
    @Override
    public final void mouseMoved(){        AppMgr.mouseMoved(mouseX, mouseY);}
    /**
     * called by papplet super
     */
    @Override
    public final void mousePressed() {    AppMgr.mousePressed(mouseX, mouseY, (mouseButton == LEFT), (mouseButton == RIGHT));}        
    /**
     * called by papplet super
     */
    @Override
    public final void mouseDragged(){        AppMgr.mouseDragged(mouseX, mouseY, pmouseX, pmouseY,(mouseButton == LEFT), (mouseButton == RIGHT));    }
    /**
     * called by papplet super
     */
    @Override
    public final void mouseWheel(MouseEvent event) {
        //ticks is how much the wheel has moved one way or the other
        int ticks = event.getCount();        
        AppMgr.mouseWheel(ticks);    
    }
    /**
     * called by papplet super
     */
    @Override
    public final void mouseReleased(){    AppMgr.mouseReleased();    }
        
    ///////////////////////
    // display directives
    /**
     * opengl hint directive to not check for depth - use this to display text on screen
     */
    @Override
    public final void setBeginNoDepthTest() {hint(PConstants.DISABLE_DEPTH_TEST);}
    /**
     * opengl hint directive to start checking depth again
     */
    @Override
    public final void setEndNoDepthTest() {    hint(PConstants.ENABLE_DEPTH_TEST);}

    /**
     * disable lights in scene
     */
    @Override
    public final void disableLights() { noLights();}
    /**
     * enable lights in scene
     */
    @Override
    public final void enableLights(){ lights();}    

    @Override
    public final void bezier(myPoint A, myPoint B, myPoint C, myPoint D) {bezier((float)A.x,(float)A.y,(float)A.z,(float)B.x,(float)B.y,(float)B.z,(float)C.x,(float)C.y,(float)C.z,(float)D.x,(float)D.y,(float)D.z);} // draws a cubic Bezier curve with control points A, B, C, D
    @Override
    public final myPoint bezierPoint(myPoint[] C, float t) {return new myPoint(bezierPoint((float)C[0].x,(float)C[1].x,(float)C[2].x,(float)C[3].x,(float)t),bezierPoint((float)C[0].y,(float)C[1].y,(float)C[2].y,(float)C[3].y,(float)t),bezierPoint((float)C[0].z,(float)C[1].z,(float)C[2].z,(float)C[3].z,(float)t)); }
    @Override
    public final myVector bezierTangent(myPoint[] C, float t) {return new myVector(bezierTangent((float)C[0].x,(float)C[1].x,(float)C[2].x,(float)C[3].x,(float)t),bezierTangent((float)C[0].y,(float)C[1].y,(float)C[2].y,(float)C[3].y,(float)t),bezierTangent((float)C[0].z,(float)C[1].z,(float)C[2].z,(float)C[3].z,(float)t)); }
    
    /**
     * Set a vertex's UV texture coordinates
     * @param P myPointf for vertex
     * @param u float u texture coordinate (0-1)
     * @param v float v texture coordinate (0-1)
     */
    @Override
    public final void vTextured(myPointf P, float u, float v) {vertex(P.x,P.y,P.z,u,v);}
    /**
     * Set a vertex's UV texture coordinates
     * @param P myPoint for vertex
     * @param u double u texture coordinate (0-1)
     * @param v double v texture coordinate (0-1)
     */
    @Override
    public final void vTextured(myPoint P, double u, double v) {vertex((float)P.x,(float)P.y,(float)P.z,(float)u,(float)v);}                      
    
    /////////////
    // show functions 
    
    /////////////
    // text
    @Override
    public final void showText(String txt, float x, float y) {                text(txt,x,y);}
    @Override
    public final void showText(String txt, float x, float y, float z ) {    text(txt,x,y,z);}    
    @Override
    public final void showTextAtPt(myPoint P, String s) {text(s, (float)P.x, (float)P.y, (float)P.z); } // prints string s in 3D at P
    @Override
    public final void showTextAtPt(myPoint P, String s, myVector D) {text(s, (float)(P.x+D.x), (float)(P.y+D.y), (float)(P.z+D.z));  } // prints string s in 3D at P+D    
    @Override
    public final void showTextAtPt(myPointf P, String s) {text(s, P.x, P.y, P.z); } // prints string s in 3D at P    
    @Override
    public final void showTextAtPt(myPointf P, String s, myVectorf D) {text(s, (P.x+D.x), (P.y+D.y),(P.z+D.z));  } // prints string s in 3D at P+D
    /**
     * display an array of text at a location on screen. Color needs to have been specified before calling.
     * @param x initial x location
     * @param y initial y location
     * @param txtAra string array to display
     */
    @Override
    public final void showTextAra(float x, float y, String[] txtAra){
        for (String txt : txtAra) {
            showText(txt, x, y, x);
            y+=AppMgr.getCloseTextHeightOffset();
        }
    }    
    /**
     * display an array of text at a location on screen
     * @param x initial x location
     * @param y initial y location
     * @param tclr text color
     * @param txtAra string array to display
     */
    @Override
    public final void showTextAra(float x, float y, int tclr, String[] txtAra){
        setColorValFill(tclr, 255);setColorValStroke(tclr, 255);
        showTextAra(x, y, txtAra);
    }    

    /**
     * show array displayed at specific point on screens
     * @param P
     * @param rad
     * @param det
     * @param clrs
     * @param txtAra
     */
    @Override
    public final void showTextAra(myPointf P, float rad, int det, int[] clrs, String[] txtAra) {//only call with set fclr and sclr - idx0 == fill, idx 1 == strk, idx2 == txtClr
        pushMatState(); 
            setColorValFill(clrs[0],255); 
            setColorValStroke(clrs[1],255);
            drawSphere(P, rad, det);
            translate(P.x,P.y,P.z); 
            showTextAra(1.2f * rad, clrs[2], txtAra);
        popMatState();
    } // render sphere of radius r and center P)
    
    /**
     * draw a box at a point containing an array of text
     * @param P
     * @param rad
     * @param det
     * @param clrs
     * @param txtAra
     * @param rectDims
     */
    @Override
    public final void showBoxTextAra(myPointf P, float rad, int det, int[] clrs, String[] txtAra, float[] rectDims) {
        pushMatState();          
            setColorValFill(clrs[0],255); 
            setColorValStroke(clrs[1],255);
            translate(P.x,P.y,P.z);
            drawSphere(myPointf.ZEROPT, rad, det);            
            
            pushMatState();  
            //make sure box doesn't extend off screen
                transToStayOnScreen(P,rectDims);
                setColorValFill(IGraphicsAppInterface.gui_White,150);
                setColorValStroke(IGraphicsAppInterface.gui_Black,255);
                setStrokeWt(2.5f);
                drawRect(rectDims);
                translate(rectDims[0],0,0);
                showTextAra(1.2f * rad, clrs[2], txtAra);
             popMatState();
         popMatState();
    } // render sphere of radius r and center P)
    
    /////////////
    // centered text    

    ////////////////////
    // showing centered text    
    /**
     * display text centered at x,y location
     * @param txt
     * @param ctrX x location of center
     * @param y
     */
    @Override
    public final void showCenteredText(String txt, float ctrX, float y) {
        // find width of text, subtract 1/2 from x value
        float xMod = textWidth(txt) *.5f;
        text(txt,ctrX-xMod,y);
    }
    
    /**
     * display text centered at x,y,z location
     * @param txt
     * @param x
     * @param y
     * @param z
     */
    @Override
    public final void showCenteredText(String txt, float ctrX, float y, float z ) {
        // find width of text, subtract 1/2 from x value
        float xMod = textWidth(txt) *.5f;
        text(txt,ctrX-xMod,y,z);        
    }
    
    /**
     * display an array of text centered at a location on screen. Color needs to have been specified before calling.
     * @param ctrX x location of center
     * @param initY initial y location
      * @param txtAra string array to display
     */    
    @Override
    public final void showCenteredTextAra(float ctrX, float initY, String[] txtAra) {
        float y = initY;
        for (String txt : txtAra) {
            showCenteredText(txt, ctrX, y, initY);
            y+=AppMgr.getCloseTextHeightOffset();
        }    
    }
    
    /**
     * display an array of text centered at a location on screen
     * @param ctrX x location of center
     * @param initY initial y location
     * @param tclr text color
      * @param txtAra string array to display
     */
    @Override
    public final void showCenteredTextAra(float ctrX, float initY, int tclr, String[] txtAra) {
        setColorValFill(tclr, 255);setColorValStroke(tclr, 255);
        showCenteredTextAra(ctrX, initY, txtAra);        
    }
    
    /**
     * return the size, in pixels, of the passed text string, accounting for the currently set font dimensions
     * @param txt the text string to be measured
     * @return the size in pixels
     */
    @Override
    public float getTextWidth(String txt) {        return super.textWidth(txt);    }
    /**
     * Set the current text font size
     * @param size
     */
    @Override
    public final void setTextSize(float fontSize) {super.textSize(fontSize);}

    ///////////
    // end text    
    
    @Override
    public final void setNoFill() {noFill();}
    
    @Override
    public final void setNoStroke(){noStroke();}
    
    private void checkClrInts(int fclr, int sclr) {
        if(fclr > -1){setColorValFill(fclr,255); } else if(fclr <= -2) {noFill();}        
        if(sclr > -1){setColorValStroke(sclr,255);} else if(sclr <= -2) {noStroke();}
    }
    
    private void checkClrIntArrays(int[] fclr, int[] sclr) {
        if(fclr!= null){setFill(fclr,255);}
        if(sclr!= null){setStroke(sclr,255);}
    }    
    
    ///////////
    // points
    
    @Override
    public final void drawSphere(myPoint P, double rad, int det) {
        pushMatState(); 
        sphereDetail(det);
        translate(P.x,P.y,P.z); 
        sphere((float) rad); 
        popMatState();
    }
    
    ////////////////////
    // showing double points as spheres or circles

    
    /**
     * show a point as a sphere, using double point as center
     * @param P point for center
     * @param r radius
     * @param det sphere detail
     * @param fclr fill color index
     * @param sclr scale color index
     */
    @Override
    public final void showPtAsSphere(myPoint P, double r, int det, int fclr, int sclr) {
        pushMatState();
        checkClrInts(fclr, sclr);
        drawSphere(P, r, det);
        popMatState();
    }
    /**
     * show a point as a sphere, using double point as center
     * @param P point for center
     * @param r radius
     * @param det sphere detail
     * @param fclr fill color array
     * @param sclr scale color array
     */
    @Override
    public final void showPtAsSphere(myPoint P, double r, int det, int[] fclr, int[] sclr) {
        pushMatState(); 
        checkClrIntArrays(fclr, sclr);
        drawSphere(P, r, det);
        popMatState();
    };
    
    /**
     * show a point as a flat circle, using double point as center
     * @param P point for center
     * @param r radius
     * @param fclr fill color index
     * @param sclr scale color index
     */
    @Override
    public final void showPtAsCircle(myPoint P, double r, int fclr, int sclr) {
        pushMatState(); 
        checkClrInts(fclr, sclr);
        drawEllipse2D(P,(float)r);                
        popMatState();
    }
    /**
     * show a point as a flat circle, using double point as center
     * @param P point for center
     * @param r radius
     * @param det sphere detail
     * @param fclr fill color array
     * @param sclr scale color array
     */
    @Override
    public final void showPtAsCircle(myPoint P, double r, int[] fclr, int[] sclr) {
        pushMatState(); 
        checkClrIntArrays(fclr, sclr);
        drawEllipse2D(P,(float)r);                        
        popMatState();
    }
    /**
     * show a point either as a sphere or as a circle, with text
     * @param P
     * @param r
     * @param s
     * @param D
     * @param clr
     * @param flat
     */
    @Override
    public final void showPtWithText(myPoint P, double r, String s, myVector D, int clr, boolean flat){
        if(flat) {            showPtAsCircle(P,r, clr, clr);} 
        else {            showPtAsSphere(P,r,5, gui_Black, gui_Black);        }
        pushStyle();setColorValFill(clr,255);showTextAtPt(P,s,D);popStyle();
    }

    @Override
    public final void showVec( myPoint ctr, double len, myVector v){drawLine(ctr.x,ctr.y,ctr.z,ctr.x+(v.x)*len,ctr.y+(v.y)*len,ctr.z+(v.z)*len);}

    /**
     * Draw a shape from the passed myPoint ara
     * @param ara array of myPoints
     */
    @Override
    public final void drawShapeFromPts(myPoint[] ara) {
        gl_beginShape(GL_PrimStyle.GL_LINE_LOOP); 
        for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} 
        gl_endShape(true);
    }                     
    /**
     * Draw a shape from passed myPoint array with given normal
     * @param ara array of myPoints
     * @param norm surface normal for resultant shape
     */
    @Override
    public final void drawShapeFromPts(myPoint[] ara, myVector norm) {
        gl_beginShape(GL_PrimStyle.GL_LINE_LOOP);
        gl_normal(norm); 
        for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} 
        gl_endShape(true);
    }
    /**
     * Draw a shape from the given myPoint array of points with the given myVector array
     * of normals per point.  NOTE : point array size and normal array size is not checked.
     * 
     * @param ara array of myPoints
     * @param normAra array of per-point myVector surface normals for shape.
     * SIZE IS NOT VERIFIED - this must be at least as many normals
     * as there are points for shape
     */
    @Override
    public final void drawShapeFromPts(myPoint[] ara, myVector[] normAra) {
        gl_beginShape(GL_PrimStyle.GL_LINE_LOOP); 
        for(int i=0;i<ara.length;++i){gl_normal(normAra[i]);gl_vertex(ara[i]);} 
        gl_endShape(true);
    }
        
    ///////////
    // end double points
    ///////////
    // float points (pointf)
    
    @Override
    public final void drawSphere(myPointf P, float rad, int det) {
        pushMatState(); 
        sphereDetail(det);
        translate(P.x,P.y,P.z); 
        sphere(rad); 
        popMatState();
    }    
    ////////////////////
    // showing float points as spheres or circles    
    /**
     * show a point as a sphere, using float point as center
     * @param P point for center
     * @param r radius
     * @param det sphere detail
     * @param fclr fill color index
     * @param sclr scale color index
     */
    @Override
    public final void showPtAsSphere(myPointf P, float r,int det, int fclr, int sclr) {
        pushMatState(); 
        checkClrInts(fclr, sclr);
        drawSphere(P,(float)r, det);
        popMatState();        
    }    
    /**
     * show a point as a sphere, using float point as center
     * @param P point for center
     * @param r radius
     * @param det sphere detail
     * @param fclr fill color array
     * @param sclr scale color array
     */
    @Override
    public final void showPtAsSphere(myPointf P, float r, int det, int[] fclr, int[] sclr){
        pushMatState(); 
        checkClrIntArrays(fclr, sclr);
        drawSphere(P,(float)r, det);
        popMatState();
    } // render sphere of radius r and center P)
    /**
     * show a point as a flat circle, using float point as center
     * @param P point for center
     * @param r radius
     * @param fclr fill color index
     * @param sclr scale color index
     */
    @Override
    public final void showPtAsCircle(myPointf P, float r, int fclr, int sclr) {        
        pushMatState(); 
        checkClrInts(fclr, sclr);
        drawEllipse2D(P,(float)r);        
        popMatState();        
    }
    /**
     * show a point as a flat circle, using float point as center
     * @param P point for center
     * @param r radius
     * @param det sphere detail
     * @param fclr fill color array
     * @param sclr scale color array
     */
    @Override
    public final void showPtAsCircle(myPointf P, float r, int[] fclr, int[] sclr) {        
        pushMatState(); 
        checkClrIntArrays(fclr, sclr);
        drawEllipse2D(P,(float)r);                    
        popMatState();
    } // render sphere of radius r and center P)

    @Override
    public final void showPtWithText(myPointf P, float r, String s, myVectorf D, int clr, boolean flat){
        if(flat) {            showPtAsCircle(P,r, clr, clr);} 
        else {            showPtAsSphere(P,r,5, gui_Black, gui_Black);        }
        pushStyle();setColorValFill(clr,255);showTextAtPt(P,s,D);popStyle();
    }
    
    @Override
    public final void showVec( myPointf ctr, float len, myVectorf v){line(ctr.x,ctr.y,ctr.z,ctr.x+(v.x)*len,ctr.y+(v.y)*len,ctr.z+(v.z)*len);}
    
    /**
     * Draw a shape from the passed myPointf ara
     * @param ara array of myPointfs
     */
    @Override
    public final void drawShapeFromPts(myPointf[] ara) {
        gl_beginShape(GL_PrimStyle.GL_LINE_LOOP); 
        for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} 
        gl_endShape(true);
    }
    /**
     * Draw a shape from passed myPointf array with given normal
     * @param ara array of myPointfs
     * @param norm myVectorf surface normal for resultant shape
     */
    @Override
    public final void drawShapeFromPts(myPointf[] ara, myVectorf norm) {
        gl_beginShape(GL_PrimStyle.GL_LINE_LOOP);
        gl_normal(norm); 
        for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} 
        gl_endShape(true);
    } 
    /**
     * Draw a shape from the given myPointf array of points with the given myVectorf array
     * of normals per point.  NOTE : point array size and normal array size is not checked.
     * 
     * @param ara array of myPointfs
     * @param normAra array of per-point myVectorf surface normals for shape.
     * SIZE IS NOT VERIFIED - this must be at least as many normals
     * as there are points for shape
     */
    @Override
    public final void drawShapeFromPts(myPointf[] ara, myVectorf[] normAra) {
        gl_beginShape(GL_PrimStyle.GL_LINE_LOOP);
        for(int i=0;i<ara.length;++i){gl_normal(normAra[i]);gl_vertex(ara[i]);} 
        gl_endShape(true);
    }  
    
    
    ///end show functions
    
    
    ////////////////////////
    // splines
    /**
     * implementation of catumull rom - array needs to be at least 4 points, if not, then reuses first and last points as extra cntl points  
     * @param pts
     */
    @Override
    public final void catmullRom2D(myPointf[] ara) {
        if(ara.length < 4){
            if(ara.length == 0){return;}
            gl_beginShape(); curveVertex2D(ara[0]);for(int i=0;i<ara.length;++i){curveVertex2D(ara[i]);} curveVertex2D(ara[ara.length-1]);gl_endShape();
            return;}        
        gl_beginShape(); for(int i=0;i<ara.length;++i){curveVertex2D(ara[i]);} gl_endShape();
    }
    protected final void curveVertex2D(myPointf P) {curveVertex(P.x,P.y);};                                           // curveVertex for shading or drawing

    
    /**
     * implementation of catumull rom - array needs to be at least 4 points, if not, then reuses first and last points as extra cntl points  
     * @param pts
     */
    @Override
    public final void catmullRom2D(myPoint[] ara) {
        if(ara.length < 4){
            if(ara.length == 0){return;}
            gl_beginShape(); curveVertex2D(ara[0]);for(int i=0;i<ara.length;++i){curveVertex2D(ara[i]);} curveVertex2D(ara[ara.length-1]);gl_endShape();
            return;}        
        gl_beginShape(); for(int i=0;i<ara.length;++i){curveVertex2D(ara[i]);} gl_endShape();        
    }
    protected final void curveVertex2D(myPoint P) {curveVertex((float)P.x,(float)P.y);};                                           // curveVertex for shading or drawing
    
    /**
     * implementation of catumull rom - array needs to be at least 4 points, if not, then reuses first and last points as extra cntl points  
     * @param pts
     */
    @Override
    public final void catmullRom3D(myPointf[] ara) {
        if(ara.length < 4){
            if(ara.length == 0){return;}
            gl_beginShape(); curveVertex3D(ara[0]);for(int i=0;i<ara.length;++i){curveVertex3D(ara[i]);} curveVertex3D(ara[ara.length-1]);gl_endShape();
            return;}        
        gl_beginShape(); for(int i=0;i<ara.length;++i){curveVertex3D(ara[i]);} gl_endShape();
    }
    protected final void curveVertex3D(myPointf P) {curveVertex(P.x,P.y,P.z);};                                           // curveVertex for shading or drawing

    /**
     * implementation of catumull rom - array needs to be at least 4 points, if not, then reuses first and last points as extra cntl points  
     * @param pts
     */
    @Override
    public final void catmullRom3D(myPoint[] ara) {
        if(ara.length < 4){
            if(ara.length == 0){return;}
            gl_beginShape(); curveVertex3D(ara[0]);for(int i=0;i<ara.length;++i){curveVertex3D(ara[i]);} curveVertex3D(ara[ara.length-1]);gl_endShape();
            return;}        
        gl_beginShape(); for(int i=0;i<ara.length;++i){curveVertex3D(ara[i]);} gl_endShape();        
    }    
    protected final void curveVertex3D(myPoint P) {curveVertex((float)P.x,(float)P.y,(float)P.z);};                                           // curveVertex for shading or drawing


    ///////////////////////////////////
    // getters/setters
    //////////////////////////////////
    
    /**
     * returns application window width in pxls
     * @return
     */
    @Override
    public final int getWidth() {return width;}
    /**
     * returns application window height in pxls
     * @return
     */
    @Override
    public final int getHeight() {return height;}    
    
    /**
     * set smoothing level based on renderer
     * @param smthLvl 0 == no smoothing,      int: either 2, 3, 4, or 8 depending on the renderer
     */
    @Override
    public final void setSmoothing(int smthLvl) {
        if (smthLvl == 0) {    noSmooth();    }
        else {            smooth(smthLvl);}
    }
    
    /**
     * set initial window location
     * @param x
     * @param y
     */
    @Override
    public final void setLocation(int x, int y) {
        surface.setLocation(x, y);        
    }
    /**
     * set camera to passed 9-element values - should be called from window!
     * @param camVals
     */
    @Override
    public final void setCameraWinVals(float[] camVals) {        camera(camVals[0],camVals[1],camVals[2],camVals[3],camVals[4],camVals[5],camVals[6],camVals[7],camVals[8]);}
    /**
     * used to handle camera location/motion - the final rotateX(MyMathUtils.HALF_PI_F) is to the scene with z up
     */
    @Override
    public final void setCamOrient(float rx, float ry){rotateX(rx);rotateY(ry); rotateX(MyMathUtils.HALF_PI_F);}    
    /**
     * used to draw text on screen without changing mode - reverses camera orientation setting
     * the initial rotateX(-MyMathUtils.HALF_PI_F) remove the z-up orientation
     */
    @Override
    public final void unSetCamOrient(float rx, float ry){rotateX(-MyMathUtils.HALF_PI_F); rotateY(-ry);rotateX(-rx);}

    /**
     * return x screen value for 3d point
     * @param x
     * @param y
     * @param z
     */
    @Override
    public float getSceenX(float x, float y, float z) {        return screenX(x,y,z);    };
    /**
     * return y screen value for 3d point
     * @param x
     * @param y
     * @param z
     */
    @Override
    public float getSceenY(float x, float y, float z) {        return screenY(x,y,z);    };
    /**
     * return screen value of z (Z-buffer) for 3d point
     * @param x
     * @param y
     * @param z
     */
    @Override
    public float getSceenZ(float x, float y, float z) {        return screenZ(x,y,z);    };
    
    /**
     * return target frame rate
     * @return
     */
    @Override
    public final float getFrameRate() {return frameRate;}
    @Override
    public final myPoint getMouse_Raw() {return new myPoint(mouseX, mouseY,0);}                                                      // current mouse location
    @Override
    public final myVector getMouseDrag() {return new myVector(mouseX-pmouseX,mouseY-pmouseY,0);};                                 // vector representing recent mouse displacement
    
    @Override
    public final myPointf getMouse_Raw_f() {return new myPointf(mouseX, mouseY,0);}                                                      // current mouse location
    @Override
    public final myVectorf getMouseDrag_f() {return new myVectorf(mouseX-pmouseX,mouseY-pmouseY,0);};                                 // vector representing recent mouse displacement

    @Override
    public final int[] getMouse_Raw_Int() {return new int[] {mouseX, mouseY};}                                                      // current mouse location
    @Override
    public final int[] getMouseDrag_Int() {return new int[] {mouseX-pmouseX,mouseY-pmouseY};};                          // vector representing recent mouse displacement

    /**
     * get depth at specified screen dim location
     * @param x
     * @param y
     * @return
     */
    @Override
    public float getDepth(int x, int y) {
        PGL pgl = beginPGL();
        FloatBuffer depthBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        int newY = height - y;        pgl.readPixels(x, newY - 1, 1, 1, PGL.DEPTH_COMPONENT, PGL.FLOAT, depthBuffer);
        float depthValue = depthBuffer.get(0);
        endPGL();
        return depthValue;
    }
    
    /**
     * Get world location as float array from screen location + depth
     * @param x
     * @param y
     * @param depthValue
     * @return 4 element array. Point is idxs 0,1,2 normalized by idx 3
     */
    private float[] _getWorldLocFromScreenLoc(int x, int y, float depthValue){
        float height = getHeight();
        float width = getWidth();
        int newY = (int) (height - y);
        if(depthValue == -1){depthValue = getDepth(x, y); }    

        // build normalized x,y prenormalized depth homogeneous screen point
        float[] normalized = new float[] {
                (x/(width * 0.5f)) - 1.0f, 
                (newY/(height* 0.5f)) - 1.0f, 
                depthValue * 2.0f - 1.0f, 
                1.0f};

        //get 3d transform matrices
        PGraphics3D p3d = (PGraphics3D)g;
        // Get projection matrix
        var modelViewProjInv = p3d.projection.get();
        var modelView = p3d.modelview.get(); 
        // multiply projection by model view
        modelViewProjInv.apply( modelView );
        modelViewProjInv.invert();      
      
        //destination
        float[] unprojected = new float[4];      
        modelViewProjInv.mult(normalized, unprojected);
        return unprojected;    
    }//_getWorldLocFromScreenLoc
    
    
    /**
     * determine world location as myPoint based on mouse click and passed depth
     * @param x mouse x
     * @param y mouse y
     * @param depthValue depth in world 
     * @return
     */
    @Override
    public myPoint getWorldLoc(int x, int y, float depthValue){ 
        //destination
        float[] unprojected = _getWorldLocFromScreenLoc(x,y,depthValue);
        //normalize by projected depth
        return new myPoint(unprojected[0]/unprojected[3], unprojected[1]/unprojected[3], unprojected[2]/unprojected[3]);
    }
    
    /**
     * determine world location as myPointf based on mouse click and passed depth
     * @param x
     * @param y
     * @param depthValue
     * @return
     */
    @Override
    public myPointf getWorldLoc_f(int x, int y, float depthValue){
        //destination
        float[] unprojected = _getWorldLocFromScreenLoc(x,y,depthValue);
        return new myPointf(unprojected[0]/unprojected[3], unprojected[1]/unprojected[3], unprojected[2]/unprojected[3]);
    }
    
    @Override
    public final myPoint getScrLocOf3dWrldPt(myPoint pt){return new myPoint(screenX((float)pt.x,(float)pt.y,(float)pt.z),screenY((float)pt.x,(float)pt.y,(float)pt.z),screenZ((float)pt.x,(float)pt.y,(float)pt.z));}
    
    /////////////////////        
    ///color utils
    /////////////////////

    @Override
    public final void setFill(int r, int g, int b, int alpha){fill(r,g,b,alpha);}
    @Override
    public final void setStroke(int r, int g, int b, int alpha){stroke(r,g,b,alpha);}
    /**
     * set stroke weight
     */
    @Override
    public final void setStrokeWt(float stW) {    strokeWeight(stW);}
    @Override
    public final void setColorValFill(int colorVal, int alpha){
        if(colorVal == gui_TransBlack) {
            fill(0x00010100);//    have to use hex so that alpha val is not lost    TODO not taking care of alpha here
        } else {
            setFill(getClr(colorVal, alpha), alpha);
        }    
    }//setcolorValFill
    @Override
    public final void setColorValStroke(int colorVal, int alpha){
        setStroke(getClr(colorVal, alpha), alpha);        
    }//setcolorValStroke    
//    @Override
//    public final void setColorValFillAmb(int colorVal, int alpha){
//        if(colorVal == gui_TransBlack) {
//            fill(0x00010100);//    have to use hex so that alpha val is not lost    
//            ambient(0,0,0);
//        } else {
//            int[] fillClr = getClr(colorVal, alpha);
//            setFill(fillClr, alpha);
//            ambient(fillClr[0],fillClr[1],fillClr[2]);
//        }        
//    }//setcolorValFill

    
    @Override
    public final Integer[] getClrMorph(int[] a, int[] b, double t){
        if(t==0){return new Integer[]{a[0],a[1],a[2],a[3]};} else if(t==1){return new Integer[]{b[0],b[1],b[2],b[3]};}
        return new Integer[]{(int)(((1.0f-t)*a[0])+t*b[0]),(int)(((1.0f-t)*a[1])+t*b[1]),(int)(((1.0f-t)*a[2])+t*b[2]),(int)(((1.0f-t)*a[3])+t*b[3])};
    }
    /**
     * Create a mesh shape
     * @return
     */  
    @Override
    public IMeshInterface createBaseMesh() {
        return new ProcessingShape(this, PShape.GEOMETRY);
    }//createBaseMesh
    /**
     * Create a mesh shape
     * @return
     */
    @Override
    public IMeshInterface createBaseMesh(GL_PrimStyle meshType) {
        switch (meshType) {
            case GL_POINTS :        { return new ProcessingShape(this,PConstants.POINTS);        }
            case GL_LINES :         { return new ProcessingShape(this,PConstants.LINES);         }
            case GL_LINE_LOOP :     { return new ProcessingShape(this,PConstants.LINE_LOOP);     }           
            case GL_LINE_STRIP :    { return new ProcessingShape(this,PConstants.LINE_STRIP);    }           
            case GL_TRIANGLES :     { return new ProcessingShape(this,PConstants.TRIANGLES);     }
            case GL_TRIANGLE_STRIP :{ return new ProcessingShape(this,PConstants.TRIANGLE_STRIP);}
            case GL_TRIANGLE_FAN :  { return new ProcessingShape(this,PConstants.TRIANGLE_FAN);  }
            case GL_QUADS :         { return new ProcessingShape(this,PConstants.QUADS);         }
            case GL_QUAD_STRIP :    { return new ProcessingShape(this,PConstants.QUAD_STRIP);    }
            case POINT :            { return new ProcessingShape(this,PConstants.POINT);         }
            case LINE :             { return new ProcessingShape(this,PConstants.LINE);          }
            case TRIANGLE :         { return new ProcessingShape(this,PConstants.TRIANGLE);      }
            case QUAD :             { return new ProcessingShape(this,PConstants.QUAD);          }
            case RECT :             { return new ProcessingShape(this,PConstants.RECT);          }
            case ELLIPSE :          { return new ProcessingShape(this,PConstants.ELLIPSE);       }
            case ARC :              { return new ProcessingShape(this,PConstants.ARC);           }
            case SPHERE :           { return new ProcessingShape(this,PConstants.SPHERE);        }
            case BOX :              { return new ProcessingShape(this,PConstants.BOX);           }
            default :               { return new ProcessingShape(this,PConstants.POLYGON);       }        
        }
    }//createBaseMesh
    
    /**
     * Create a mesh shape
     * @return
     */
    @Override
    public IMeshInterface createBaseMesh(GL_PrimStyle meshType, float...args) {
        switch (meshType) {
            case GL_POINTS :        { return new ProcessingShape(this,PConstants.POINTS, args);        }
            case GL_LINES :         { return new ProcessingShape(this,PConstants.LINES, args);         }
            case GL_LINE_LOOP :     { return new ProcessingShape(this,PConstants.LINE_LOOP, args);     }           
            case GL_LINE_STRIP :    { return new ProcessingShape(this,PConstants.LINE_STRIP, args);    }           
            case GL_TRIANGLES :     { return new ProcessingShape(this,PConstants.TRIANGLES, args);     }
            case GL_TRIANGLE_STRIP :{ return new ProcessingShape(this,PConstants.TRIANGLE_STRIP, args);}
            case GL_TRIANGLE_FAN :  { return new ProcessingShape(this,PConstants.TRIANGLE_FAN, args);  }
            case GL_QUADS :         { return new ProcessingShape(this,PConstants.QUADS, args);         }
            case GL_QUAD_STRIP :    { return new ProcessingShape(this,PConstants.QUAD_STRIP, args);    }
            case POINT :            { return new ProcessingShape(this,PConstants.POINT, args);         }
            case LINE :             { return new ProcessingShape(this,PConstants.LINE, args);          }
            case TRIANGLE :         { return new ProcessingShape(this,PConstants.TRIANGLE, args);      }
            case QUAD :             { return new ProcessingShape(this,PConstants.QUAD, args);          }
            case RECT :             { return new ProcessingShape(this,PConstants.RECT, args);          }
            case ELLIPSE :          { return new ProcessingShape(this,PConstants.ELLIPSE, args);       }
            case ARC :              { return new ProcessingShape(this,PConstants.ARC, args);           }
            case SPHERE :           { return new ProcessingShape(this,PConstants.SPHERE, args);        }
            case BOX :              { return new ProcessingShape(this,PConstants.BOX, args);           }
            default :               { return new ProcessingShape(this,PConstants.POLYGON, args);       }        
        }        
    }//createBaseMesh
    
    /**
     * Create a mesh shape intended to be the parent of a group of shapes/meshes
     * @return
     */  
    @Override
    public IMeshInterface createBaseGroupMesh() { return new ProcessingShape(this, PConstants.GROUP);}

}//ProcessingRenderer
