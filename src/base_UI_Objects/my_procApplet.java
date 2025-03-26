package base_UI_Objects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import base_Render_Interface.IRenderInterface;
import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;

import com.jogamp.newt.opengl.GLWindow;

import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.event.MouseEvent;
import processing.opengl.PGL;
import processing.opengl.PGraphics3D;

public final class my_procApplet extends processing.core.PApplet implements IRenderInterface {
	
	public static GUI_AppManager AppMgr;
		
	public final float frate = 120;			//target frame rate - # of playback updates per second
	
	//animation control variables	
	public final float maxAnimCntr = MyMathUtils.PI_F*1000.0f, baseAnimSpd = 1.0f;
	
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
		String[] appletArgs = new String[] { "base_UI_Objects.my_procApplet" };
		AppMgr = _appMgr;
		if (passedArgs != null) {processing.core.PApplet.main(processing.core.PApplet.concat(appletArgs, passedArgs)); } else {processing.core.PApplet.main(appletArgs);		    }
	    
	}//main	

	/**
	 * Initialize render interface implementation.
	 */
	@Override
	public void initRenderInterface() {
		bgrndSphereAra = new HashMap<Integer, PShape>();
		bgrndColorAra = new HashMap<Integer, int[]>();
	}
	
	//processing being run in eclipse uses settings for variable size dimensions
	@Override
	public void settings(){	
		AppMgr.setIRenderInterface(this);
		int[] desDims = AppMgr.getIdealAppWindowDims();
		size(desDims[0], desDims[1],P3D);	
		//allow user to set smoothing
		AppMgr.setSmoothing();
	}
	
	/**
	 * Set the background painted color for specified window idx
	 * @param winIdx window idx to set color for
	 * @param r
	 * @param g
	 * @param b
	 * @param alpha
	 */	
	@Override
	public final void setRenderBackground(int winIdx, int r, int g, int b, int alpha) {
		bgrndColorAra.put(winIdx, new int[] {r,g,b,alpha});
	}
	
	/**
	 * Load a background "skybox" sphere using texture from filename
	 * @param filename Texture to use for background skybox sphere
	 */
	@Override
	public void loadBkgndSphere(int winIdx, String filename) {
		//save current sphere detail
		int sPrevDet = getSphereDetail();
		setSphereDetail(100);
		PImage bgrndTex = loadImage(filename);
		PShape bgrndSphere = createShape(PConstants.SPHERE, 10000);
		bgrndSphere.setTexture(bgrndTex);
		bgrndSphere.rotate(MyMathUtils.HALF_PI_F,-1,0,0);
		bgrndSphere.setStroke(false);	
		bgrndSphereAra.put(winIdx, bgrndSphere);
		setRenderBackground(winIdx,getClr(gui_White, 255), 255);		
		shape(bgrndSphere);	
		//reset detail
		setSphereDetail(sPrevDet);
	}
	/**
	 * Set loaded background sphere as skybox
	 * @param winIdx the idx of the skybox to draw
	 */
	@Override
	public void drawBkgndSphere(int winIdx) {
		PShape shape = bgrndSphereAra.get(winIdx);
		drawRenderBackground(winIdx);
		if(shape==null) {
			AppMgr.msgObj.dispErrorMessage("my_procApplet","drawBkgndSphere","ERROR! No background sphere specified for window idx :"+winIdx);
			return;
		}
		shape(shape);	
	}
	
	
	@Override
	public void setup() {
		colorMode(RGB, 255, 255, 255, 255);
		//setup default stroke ends.  ROUND is very slow, SQUARE  makes points invisible	
		strokeCap(PROJECT);
		textSize(AppMgr.getTextSize());
		textureMode(NORMAL);			
		rectMode(CORNER);	
		sphereDetail(4);
		//Set up application
		AppMgr.setupApp(width, height);

		//needs to be the last thing called in setup, to avoid timeout 5000ms issue
		frameRate(frate);
	}//setup()
	
	/**
	 * Draw the specified window's background color
	 * @param winIdx the window whose background to draw
	 */
	@Override
	public void drawRenderBackground(int winIdx) {
		int[] bGroundAra = bgrndColorAra.get(winIdx);
		if (bGroundAra == null) {
			bGroundAra = getClr(gui_White, 255);
		}
		super.background(bGroundAra[0],bGroundAra[1],bGroundAra[2],bGroundAra[3]);
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
	protected static int pushPopAllDepth = 0, pushPopJustStyleDepth = 0;
	/**
	 * push matrix, and style (if available) - must be paired with pop matrix/style calls
	 */
	@Override
	public final int pushMatState() {	super.pushMatrix();super.pushStyle();return ++pushPopAllDepth;}
	/**
	 * pop style (if supported) and matrix - must be called after equivalent pushes
	 */
	@Override
	public final int popMatState() {	super.popStyle();super.popMatrix();	return --pushPopAllDepth;}
	
	/**
	 * push current style/color params onto "style stack" (save current settings)
	 */	
	@Override
	public int pushJustStyleState() {	super.pushStyle();return ++pushPopJustStyleDepth;}
	/**
	 * pop current style/color params from "style stack" (restore/overwrite with last saved settings)
	 */
	@Override
	public int popJustStyleState(){		super.popStyle();return --pushPopJustStyleDepth;}
	

	/**
	 * main draw loop - override if handling draw differently
	 */
	@Override
	public void draw(){
		//returns whether actually drawn or not
		if(!AppMgr.mainSimAndDrawLoop()) {return;}
		//TODO find better mechanism for saving screenshot
		if (AppMgr.doSaveAnim()) {	savePic();}
	}//draw	
	
	/**
	 * Builds and sets window title
	 */
	@Override
	public void setWindowTitle(String applicationTitle, String windowName) {
		//build window title
		surface.setTitle(applicationTitle + " : " + (int)(frameRate) + " fps|cyc curFocusWin : " + windowName);		
	}		
	
	/**
	 * draw a translucent representation of a canvas plane ortho to eye-to-mouse vector
	 * @param eyeToMse vector 
	 * @param canvas3D bounded points to draw polygon edge of canvas
	 * @param color color to paint the canvas - should be translucent (Alpha should be no more than 80), 
	 * 				light for dark backgrounds and dark for light backgrounds. 
	 */
	@Override
	public void drawCanvas(myVector eyeToMse, myPointf[] canvas3D, int[] color){
		disableLights();
		pushMatState();
		gl_beginShape(GL_PrimStyle.GL_LINE_LOOP);
		gl_setFill(color, color[3]);
		setNoStroke();
		gl_normal(eyeToMse);
        for(int i =canvas3D.length-1;i>=0;--i){		//build invisible canvas to draw upon
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
	public void setFrustum(float left, float right, float bottom, float top, float near, float far) {
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
	public void setPerspective(float fovy, float ar, float zNear, float zFar) {
		super.perspective(fovy, ar, zNear, zFar);
	}
	
	/**
	 * set orthographic projection matrix for camera (2d or 3d)
	 * @param left left plane of clipping volume
	 * @param right right plane of the clipping volume
	 * @param bottom bottom plane of the clipping volume
	 * @param top top plane of the clipping volume
	 * @param near maximum distance from the origin to the viewer
	 * @param far maximum distance from the origin away from the viewer
	 */
	@Override
	public void setOrtho(float left, float right, float bottom, float top) {
		super.ortho(left, right, bottom, top);
	}
	@Override
	public void setOrtho(float left, float right, float bottom, float top, float near, float far) {
		super.ortho(left, right, bottom, top, near, far);
	}
	
	
	@Override
	public void gl_normal(float x, float y, float z) {super.normal(x,y,z);}                                          // changes normal for smooth shading
	@Override
	public void gl_vertex(float x, float y, float z) {super.vertex(x,y,z);}                                             // vertex for shading or drawing

	/**
	 * set fill color by value during shape building
	 * @param clr 1st 3 values denot integer color vals
	 * @param alpha 
	 */
	@Override
	public void gl_setFill(int r, int g, int b, int alpha) {super.fill(r,g,b,alpha);}

	/**
	 * set stroke color by value during shape building
	 * @param clr rgba
	 * @param alpha 
	 */
	@Override
	public void gl_setStroke(int r, int g, int b, int alpha) {super.stroke(r,g,b,alpha);}	
	
	
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
  
  DONOT SUPPORT QUAD PRIMS - have been deprecated/Removed from opengl
  static final int QUADS           = 17;  // vertices
  static final int QUAD_STRIP      = 18;  // vertices
  
  static final int POLYGON         = 20;  // 
	 * 
	 * 
	 */
	@Override
	public void gl_beginShape(GL_PrimStyle primType) {
		switch (primType) {
			case GL_POINTS : {
				beginShape(POINTS);
				return;
			}
			case GL_LINES : {
				beginShape(LINES);
				return;
			}
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
			case GL_TRIANGLES : {
				beginShape(TRIANGLES);
				break;
			}
			case GL_TRIANGLE_STRIP : {
				beginShape(TRIANGLE_STRIP);
				break;
			}
			case GL_TRIANGLE_FAN : {
				beginShape(TRIANGLE_FAN);
				break;
			}
			default : {
				beginShape(POLYGON);	
				return;
			}		
		};
	}//gl_beginShape
	/**
	 * type needs to be -1 for blank, otherwise will be CLOSE, regardless of passed value
	 */
	@Override
	public void gl_endShape(boolean isClosed) {		
		if(isClosed) {			endShape(CLOSE);		}
		else {				endShape();		}
	}
	
	@Override
	public void drawSphere(float rad) {sphere(rad);}
	private int sphereDtl = 4;

	@Override
	public void setSphereDetail(int det) {sphereDtl=det;sphereDetail(det);}

	@Override
	public int getSphereDetail() {return sphereDtl;}
	
	/**
	 * draw a 2 d ellipse 
	 * @param x,y,x rad, y rad
	 */
	@Override
	public void drawEllipse2D(float x, float y, float xr, float yr) {ellipse(x,y,xr,yr);}

	
	@Override
	public void drawLine(float x1, float y1, float z1, float x2, float y2, float z2){line(x1,y1,z1,x2,y2,z2 );}
	@Override
	public void drawLine(myPointf a, myPointf b, int stClr, int endClr){
		gl_beginShape(GL_PrimStyle.GL_LINES);
		setStrokeWt(1.0f);
		setColorValStroke(stClr, 255);
		gl_vertex(a);
		setColorValStroke(endClr,255);
		gl_vertex(b);
		gl_endShape();
	}
	@Override
	public void drawLine(myPointf a, myPointf b, int[] stClr, int[] endClr){
		gl_beginShape(GL_PrimStyle.GL_LINES);
		setStrokeWt(1.0f);
		setStroke(stClr, 255);
		gl_vertex(a);
		setStroke(endClr,255);
		gl_vertex(b);
		gl_endShape();
	}
	
	/**
	 * draw a cloud of points with passed color values as an integrated shape
	 * @param numPts number of points to draw
	 * @param ptIncr incrementer between points, to draw only every 2nd, 3rd or more'th point
	 * @param h_part_clr_int 2d array of per point 3-color stroke values
	 * @param h_part_pos_x per point x value
	 * @param h_part_pos_y per point y value
	 * @param h_part_pos_z per point z value
	 */
	@Override
	public void drawPointCloudWithColors(int numPts, int ptIncr, int[][] h_part_clr_int, float[] h_part_pos_x, float[] h_part_pos_y, float[] h_part_pos_z) {
		gl_beginShape(GL_PrimStyle.GL_POINTS);
		for(int i=0;i<=numPts-ptIncr;i+=ptIncr) {	
			setStroke(h_part_clr_int[i][0], h_part_clr_int[i][1], h_part_clr_int[i][2], 255);
			gl_vertex(h_part_pos_x[i], h_part_pos_y[i], h_part_pos_z[i]);
		}
		gl_endShape();
	}//drawPointCloudWithColors	
	
	/**
	 * draw a cloud of points with all points having same color value as an integrated shape
	 * @param numPts number of points to draw
	 * @param ptIncr incrementer between points, to draw only every 2nd, 3rd or more'th point
	 * @param h_part_clr_int array of 3-color stroke values for all points
	 * @param h_part_pos_x per point x value
	 * @param h_part_pos_y per point y value
	 * @param h_part_pos_z per point z value
	 */
	@Override
	public void drawPointCloudWithColor(int numPts, int ptIncr, int[] h_part_clr_int, float[] h_part_pos_x, float[] h_part_pos_y, float[] h_part_pos_z) {
		gl_beginShape(GL_PrimStyle.GL_POINTS);
		setStroke(h_part_clr_int[0], h_part_clr_int[1], h_part_clr_int[2], 255);
		for(int i=0;i<=numPts-ptIncr;i+=ptIncr) {	
			gl_vertex(h_part_pos_x[i], h_part_pos_y[i], h_part_pos_z[i]);
		}
		gl_endShape();
	}//drawPointCloudWithColors	
	
	/**
	 * draw a box centered at origin with passed dimensions, in 3D
	 */
	@Override
	public void drawBox3D(int x, int y, int z) {box(x,y,z);};
	/**
	 * draw a rectangle in 2D using the passed values as x,y,w,h
	 * @param a 4 element array : x,y,w,h
	 */
	@Override
	public void drawRect(float a, float b, float c, float d){rect(a,b,c,d);}				//rectangle from array of floats : x, y, w, h
	
	/**
	 * draw a circle centered at P with specified radius r in plane proscribed by passed axes using n number of points
	 * @param P center
	 * @param r radius
	 * @param I x axis
	 * @param J y axis
	 * @param n # of points to use
	 */
	@Override
	public void drawCircle3D(myPoint P, double r, myVector I, myVector J, int n) {
		myPoint[] pts = AppMgr.buildCircleInscribedPoints(P,r,I,J,n);
		pushMatState();noFill(); drawShapeFromPts(pts);popMatState();
	}
	@Override
	public void drawCircle3D(myPointf P, float r, myVectorf I, myVectorf J, int n) {
		myPointf[] pts = AppMgr.buildCircleInscribedPoints(P,r,I,J,n);
		pushMatState();noFill(); drawShapeFromPts(pts);popMatState();
	} 
	
	/**
	 * draw a 6 pointed star centered at p inscribed in circle radius r
	 */
	@Override
	public void drawStar2D(myPointf p, float r) {
		myPointf[] pts = AppMgr.buildCircleInscribedPoints(p,r,myVectorf.FORWARD,myVectorf.RIGHT,6);
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
	public void drawTriangle2D(myPointf a, myPointf b, myPointf c) {triangle(a.x,a.y, b.x, b.y, c.x, c.y);}
	/**
	 * draw a triangle at 3 locations in 2D (only uses x,y)
	 * @param a
	 * @param b
	 * @param c
	 */
	@Override
	public void drawTriangle2D(myPoint a, myPoint b, myPoint c) {triangle((float)a.x,(float)a.y,(float) b.x, (float)b.y,(float) c.x,(float) c.y);}
	
	
	@Override
	public void drawCylinder_NoFill(myPoint A, myPoint B, double r, int clr1, int clr2) {
		myPoint[] vertList = AppMgr.buildCylVerts(A, B, r);
		int[] c1 = getClr(clr1, 255);
		int[] c2 = getClr(clr2, 255);
		noFill();
		beginShape(QUAD_STRIP);
			for(int i=0; i<vertList.length; i+=2) {
				gl_setStroke(c1[0],c1[1],c1[2],255);
				gl_vertex(vertList[i]);
				gl_setStroke(c2[0],c2[1],c2[2],255);
				gl_vertex(vertList[i+1]);}
		gl_endShape();
	}
	@Override
	public void drawCylinder_NoFill(myPointf A, myPointf B, float r, int clr1, int clr2) {
		myPointf[] vertList = AppMgr.buildCylVerts(A, B, r);
		int[] c1 = getClr(clr1, 255);
		int[] c2 = getClr(clr2, 255);
		noFill();
		beginShape(QUAD_STRIP);
			for(int i=0; i<vertList.length; i+=2) {
				gl_setStroke(c1[0],c1[1],c1[2],255);
				gl_vertex(vertList[i]); 
				gl_setStroke(c2[0],c2[1],c2[2],255);
				gl_vertex(vertList[i+1]);}
		gl_endShape();
	}

	@Override
	public void drawCylinder(myPoint A, myPoint B, double r, int clr1, int clr2) {
		myPoint[] vertList = AppMgr.buildCylVerts(A, B, r);
		int[] c1 = getClr(clr1, 255);
		int[] c2 = getClr(clr2, 255);
		beginShape(QUAD_STRIP);
			for(int i=0; i<vertList.length; i+=2) {
				gl_setFill(c1[0],c1[1],c1[2],255);		
				gl_vertex(vertList[i]); 
				gl_setFill(c2[0],c2[1],c2[2],255);	
				gl_vertex(vertList[i+1]);}
		gl_endShape();
	}
	
	@Override
	public void drawCylinder(myPointf A, myPointf B, float r, int clr1, int clr2) {
		myPointf[] vertList = AppMgr.buildCylVerts(A, B, r);
		int[] c1 = getClr(clr1, 255);
		int[] c2 = getClr(clr2, 255);
		beginShape(QUAD_STRIP);
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
	public void translate(float x, float y){super.translate(x,y);}
	@Override
	public void translate(float x, float y, float z){super.translate(x,y,z);}
	
	/**
	 * this will translate the passed box dimensions to keep them on the screen
	 * using p as start point and rectDims[2] and rectDims[3] as width and height
	 * @param P starting point
	 * @param rectDims box dimensions 
	 */
	@Override
	public void transToStayOnScreen(myPointf P, float[] rectDims) {
		float xLocSt = P.x + rectDims[0], xLocEnd = xLocSt + rectDims[2];
		float yLocSt = P.y + rectDims[1], yLocEnd = yLocSt + rectDims[3];
		float transX = 0.0f, transY = 0.0f;
		if (xLocSt < 0) {	transX = -1.0f * xLocSt;	} else if (xLocEnd > width) {transX = width - xLocEnd - 20;}
		if (yLocSt < 0) {	transY = -1.0f * yLocSt;	} else if (yLocEnd > height) {transY = height - yLocEnd - 20;}
		super.translate(transX,transY);		
	}

	@Override
	public void rotate(float thet, float x, float y, float z) {super.rotate(thet, x, y, z);}

	@Override
	public void scale(float x) {super.scale(x);}
	@Override
	public void scale(float x,float y) {super.scale(x, y);}
	@Override
	public void scale(float x,float y,float z) {super.scale(x,y,z);}

	

	////////////////////////
	// end transformations
	
//////////////////////////////////////////////////////
/// user interaction
//////////////////////////////////////////////////////	
	/**
	 * called by papplet super
	 */
	@Override
	public void keyPressed(){
		if(key==CODED) {	AppMgr.checkAndSetSACKeys(keyCode);		} 
		else {				AppMgr.sendKeyPressToWindows(key,keyCode);	}
	}	
	/**
	 * called by papplet super
	 */
	@Override
	public void keyReleased(){		AppMgr.checkKeyReleased(key==CODED, keyCode);}	
	/**
	 * called by papplet super
	 */
	@Override
	public void mouseMoved(){		AppMgr.mouseMoved(mouseX, mouseY);}
	/**
	 * called by papplet super
	 */
	@Override
	public void mousePressed() {	AppMgr.mousePressed(mouseX, mouseY, (mouseButton == LEFT), (mouseButton == RIGHT));}		
	/**
	 * called by papplet super
	 */
	@Override
	public void mouseDragged(){		AppMgr.mouseDragged(mouseX, mouseY, pmouseX, pmouseY,(mouseButton == LEFT), (mouseButton == RIGHT));	}
	/**
	 * called by papplet super
	 */
	@Override
	public void mouseWheel(MouseEvent event) {
		//ticks is how much the wheel has moved one way or the other
		int ticks = event.getCount();		
		AppMgr.mouseWheel(ticks);	
	}
	/**
	 * called by papplet super
	 */
	@Override
	public void mouseReleased(){	AppMgr.mouseReleased();	}
		
	///////////////////////
	// display directives
	/**
	 * opengl hint directive to not check for depth - use this to display text on screen
	 */
	@Override
	public void setBeginNoDepthTest() {hint(PConstants.DISABLE_DEPTH_TEST);}
	/**
	 * opengl hint directive to start checking depth again
	 */
	@Override
	public void setEndNoDepthTest() {	hint(PConstants.ENABLE_DEPTH_TEST);}

	/**
	 * disable lights in scene
	 */
	@Override
	public void disableLights() { noLights();}
	/**
	 * enable lights in scene
	 */
	@Override
	public void enableLights(){ lights();}	

	@Override
	public void bezier(myPoint A, myPoint B, myPoint C, myPoint D) {bezier((float)A.x,(float)A.y,(float)A.z,(float)B.x,(float)B.y,(float)B.z,(float)C.x,(float)C.y,(float)C.z,(float)D.x,(float)D.y,(float)D.z);} // draws a cubic Bezier curve with control points A, B, C, D
	@Override
	public final myPoint bezierPoint(myPoint[] C, float t) {return new myPoint(bezierPoint((float)C[0].x,(float)C[1].x,(float)C[2].x,(float)C[3].x,(float)t),bezierPoint((float)C[0].y,(float)C[1].y,(float)C[2].y,(float)C[3].y,(float)t),bezierPoint((float)C[0].z,(float)C[1].z,(float)C[2].z,(float)C[3].z,(float)t)); }
	@Override
	public final myVector bezierTangent(myPoint[] C, float t) {return new myVector(bezierTangent((float)C[0].x,(float)C[1].x,(float)C[2].x,(float)C[3].x,(float)t),bezierTangent((float)C[0].y,(float)C[1].y,(float)C[2].y,(float)C[3].y,(float)t),bezierTangent((float)C[0].z,(float)C[1].z,(float)C[2].z,(float)C[3].z,(float)t)); }
	
	/**
	 * vertex with texture coordinates
	 * @param P vertex location
	 * @param u,v txtr coords
	 */
	public void vTextured(myPointf P, float u, float v) {vertex(P.x,P.y,P.z,u,v);}; 
	public void vTextured(myPoint P, double u, double v) {vertex((float)P.x,(float)P.y,(float)P.z,(float)u,(float)v);};                         
	
	/////////////
	// show functions 
	
	/////////////
	// text
	@Override
	public void showText(String txt, float x, float y) {				text(txt,x,y);}
	@Override
	public void showText(String txt, float x, float y, float z ) {	text(txt,x,y,z);}
	
	/**
	 * return the size, in pixels, of the passed text string, accounting for the currently set font dimensions
	 * @param txt the text string to be measured
	 * @return the size in pixels
	 */
	@Override
	public float textWidth(String txt) {		return super.textWidth(txt);	}
	
	@Override
	public void textSize(float fontSize) {super.textSize(fontSize);}

	///////////
	// end text	
	
	@Override
	public void setNoFill() {noFill();}
	
	@Override
	public void setNoStroke(){noStroke();}
	
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
	public void drawSphere(myPoint P, double rad, int det) {
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
	public void showPtAsSphere(myPoint P, double r, int det, int fclr, int sclr) {
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
	public void showPtAsSphere(myPoint P, double r, int det, int[] fclr, int[] sclr) {
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
	public void showPtAsCircle(myPoint P, double r, int fclr, int sclr) {
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
	public void showPtAsCircle(myPoint P, double r, int[] fclr, int[] sclr) {
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
	public void showPtWithText(myPoint P, double r, String s, myVector D, int clr, boolean flat){
		if(flat) {			showPtAsCircle(P,r, clr, clr);} 
		else {			showPtAsSphere(P,r,5, gui_Black, gui_Black);		}
		pushStyle();setColorValFill(clr,255);showTextAtPt(P,s,D);popStyle();
	}

	@Override
	public void showVec( myPoint ctr, double len, myVector v){drawLine(ctr.x,ctr.y,ctr.z,ctr.x+(v.x)*len,ctr.y+(v.y)*len,ctr.z+(v.z)*len);}
	
	@Override
	public void showTextAtPt(myPoint P, String s) {text(s, (float)P.x, (float)P.y, (float)P.z); } // prints string s in 3D at P
	
	@Override
	public void showTextAtPt(myPoint P, String s, myVector D) {text(s, (float)(P.x+D.x), (float)(P.y+D.y), (float)(P.z+D.z));  } // prints string s in 3D at P+D
	
	public void show(myPoint P, double rad, int fclr, int sclr, int tclr, String txt) {
		pushMatState(); 
		checkClrInts(fclr, sclr);
		sphereDetail(5);
		translate((float)P.x,(float)P.y,(float)P.z); 
		setColorValFill(tclr,255);setColorValStroke(tclr,255);
		AppMgr.showOffsetText(1.2f * (float)rad,tclr, txt);
		popMatState();} // render sphere of radius r and center P)
	
	public void show(myPoint P, double r, int fclr, int sclr) {
		pushMatState(); 
		checkClrInts(fclr, sclr);
		sphereDetail(5);
		translate((float)P.x,(float)P.y,(float)P.z); 
		sphere((float)r); 
		popMatState();} // render sphere of radius r and center P)
	
	public void drawShapeFromPts(myPoint[] ara) {
		gl_beginShape(); 
		for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} 
		gl_endShape(true);
	}                     
	public void drawShapeFromPts(myPoint[] ara, myVector norm) {
		gl_beginShape();
		gl_normal(norm); 
		for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} 
		gl_endShape(true);
	}   
	
	///////////
	// end double points
	///////////
	// float points (pointf)
	
	@Override
	public void drawSphere(myPointf P, float rad, int det) {
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
	public void showPtAsSphere(myPointf P, float r,int det, int fclr, int sclr) {
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
	public void showPtAsSphere(myPointf P, float r, int det, int[] fclr, int[] sclr){
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
	public void showPtAsCircle(myPointf P, float r, int fclr, int sclr) {		
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
	public void showPtAsCircle(myPointf P, float r, int[] fclr, int[] sclr) {		
		pushMatState(); 
		checkClrIntArrays(fclr, sclr);
		drawEllipse2D(P,(float)r);					
		popMatState();
	} // render sphere of radius r and center P)

	@Override
	public void showPtWithText(myPointf P, float r, String s, myVectorf D, int clr, boolean flat){
		if(flat) {			showPtAsCircle(P,r, clr, clr);} 
		else {			showPtAsSphere(P,r,5, gui_Black, gui_Black);		}
		pushStyle();setColorValFill(clr,255);showTextAtPt(P,s,D);popStyle();
	}
	
	@Override
	public void showVec( myPointf ctr, float len, myVectorf v){line(ctr.x,ctr.y,ctr.z,ctr.x+(v.x)*len,ctr.y+(v.y)*len,ctr.z+(v.z)*len);}
	
	@Override
	public void showTextAtPt(myPointf P, String s) {text(s, P.x, P.y, P.z); } // prints string s in 3D at P
	
	@Override
	public void showTextAtPt(myPointf P, String s, myVectorf D) {text(s, (P.x+D.x), (P.y+D.y),(P.z+D.z));  } // prints string s in 3D at P+D
	
	/////////////
	// show functions using color idxs 
	/**
	 * display an array of text at a location on screen
	 * @param d initial y location
	 * @param tclr text color
 	 * @param txtAra string array to display
	 */
	@Override
	public void showTextAra(float d, String[] txtAra){
		float y = d;
		for (String txt : txtAra) {
			showText(txt, d, y, d);
			y+=10;
		}
	}	
	/**
	 * display an array of text at a location on screen
	 * @param d initial y location
	 * @param tclr text color
 	 * @param txtAra string array to display
	 */
	@Override
	public void showTextAra(float d, int tclr, String[] txtAra){
		setColorValFill(tclr, 255);setColorValStroke(tclr, 255);
		showTextAra(d, txtAra);
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
	public void showTextAra(myPointf P, float rad, int det, int[] clrs, String[] txtAra) {//only call with set fclr and sclr - idx0 == fill, idx 1 == strk, idx2 == txtClr
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
	public void showBoxTextAra(myPointf P, float rad, int det, int[] clrs, String[] txtAra, float[] rectDims) {
		pushMatState();  		
			setColorValFill(clrs[0],255); 
			setColorValStroke(clrs[1],255);
			translate(P.x,P.y,P.z);
			drawSphere(myPointf.ZEROPT, rad, det);			
			
			pushMatState();  
			//make sure box doesn't extend off screen
				transToStayOnScreen(P,rectDims);
				setColorValFill(IRenderInterface.gui_White,150);
				setColorValStroke(IRenderInterface.gui_Black,255);
				setStrokeWt(2.5f);
				drawRect(rectDims);
				translate(rectDims[0],0,0);
				showTextAra(1.2f * rad, clrs[2], txtAra);
			 popMatState();
		 popMatState();
	} // render sphere of radius r and center P)
	
	public void drawShapeFromPts(myPointf[] ara) {
		gl_beginShape(); 
		for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} 
		gl_endShape(true);
	}                     
	public void drawShapeFromPts(myPointf[] ara, myVectorf norm) {
		gl_beginShape();
		gl_normal(norm); 
		for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} 
		gl_endShape(true);
	}                     
	
	public void showNoClose(myPoint[] ara) {gl_beginShape(); for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} gl_endShape();};                     
	public void showNoClose(myPointf[] ara) {gl_beginShape(); for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} gl_endShape();};   
	
	///end show functions
	
	
	////////////////////////
	// splines
	/**
	 * implementation of catumull rom - array needs to be at least 4 points, if not, then reuses first and last points as extra cntl points  
	 * @param pts
	 */
	@Override
	public void catmullRom2D(myPointf[] ara) {
		if(ara.length < 4){
			if(ara.length == 0){return;}
			gl_beginShape(); curveVertex2D(ara[0]);for(int i=0;i<ara.length;++i){curveVertex2D(ara[i]);} curveVertex2D(ara[ara.length-1]);gl_endShape();
			return;}		
		gl_beginShape(); for(int i=0;i<ara.length;++i){curveVertex2D(ara[i]);} gl_endShape();
	}
	/**
	 * implementation of catumull rom - array needs to be at least 4 points, if not, then reuses first and last points as extra cntl points  
	 * @param pts
	 */
	@Override
	public void catmullRom2D(myPoint[] ara) {
		if(ara.length < 4){
			if(ara.length == 0){return;}
			gl_beginShape(); curveVertex2D(ara[0]);for(int i=0;i<ara.length;++i){curveVertex2D(ara[i]);} curveVertex2D(ara[ara.length-1]);gl_endShape();
			return;}		
		gl_beginShape(); for(int i=0;i<ara.length;++i){curveVertex2D(ara[i]);} gl_endShape();		
	}
	protected final void curveVertex2D(myPoint P) {curveVertex((float)P.x,(float)P.y);};                                           // curveVertex for shading or drawing
	protected final void curveVertex2D(myPointf P) {curveVertex(P.x,P.y);};                                           // curveVertex for shading or drawing
	
	/**
	 * implementation of catumull rom - array needs to be at least 4 points, if not, then reuses first and last points as extra cntl points  
	 * @param pts
	 */
	@Override
	public void catmullRom3D(myPointf[] ara) {
		if(ara.length < 4){
			if(ara.length == 0){return;}
			gl_beginShape(); curveVertex3D(ara[0]);for(int i=0;i<ara.length;++i){curveVertex3D(ara[i]);} curveVertex3D(ara[ara.length-1]);gl_endShape();
			return;}		
		gl_beginShape(); for(int i=0;i<ara.length;++i){curveVertex3D(ara[i]);} gl_endShape();
	}
	/**
	 * implementation of catumull rom - array needs to be at least 4 points, if not, then reuses first and last points as extra cntl points  
	 * @param pts
	 */
	@Override
	public void catmullRom3D(myPoint[] ara) {
		if(ara.length < 4){
			if(ara.length == 0){return;}
			gl_beginShape(); curveVertex3D(ara[0]);for(int i=0;i<ara.length;++i){curveVertex3D(ara[i]);} curveVertex3D(ara[ara.length-1]);gl_endShape();
			return;}		
		gl_beginShape(); for(int i=0;i<ara.length;++i){curveVertex3D(ara[i]);} gl_endShape();		
	}
	
	protected final void curveVertex3D(myPoint P) {curveVertex((float)P.x,(float)P.y,(float)P.z);};                                           // curveVertex for shading or drawing
	protected final void curveVertex3D(myPointf P) {curveVertex(P.x,P.y,P.z);};                                           // curveVertex for shading or drawing


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
	 * @param smthLvl 0 == no smoothing,  	int: either 2, 3, 4, or 8 depending on the renderer
	 */
	@Override
	public void setSmoothing(int smthLvl) {
		if (smthLvl == 0) {	noSmooth();	}
		else {			smooth(smthLvl);}
	}
	
	/**
	 * set initial window location
	 * @param x
	 * @param y
	 */
	@Override
	public void setLocation(int x, int y) {
		surface.setLocation(x, y);		
	}
	/**
	 * set camera to passed 9-element values - should be called from window!
	 * @param camVals
	 */
	@Override
	public void setCameraWinVals(float[] camVals) {		camera(camVals[0],camVals[1],camVals[2],camVals[3],camVals[4],camVals[5],camVals[6],camVals[7],camVals[8]);}
	/**
	 * used to handle camera location/motion
	 */
	@Override
	public void setCamOrient(float rx, float ry){rotateX(rx);rotateY(ry); rotateX(MyMathUtils.HALF_PI_F);		}//sets the rx, ry, pi/2 orientation of the camera eye	
	/**
	 * used to draw text on screen without changing mode - reverses camera orientation setting
	 */
	@Override
	public void unSetCamOrient(float rx, float ry){rotateX(-MyMathUtils.HALF_PI_F); rotateY(-ry);   rotateX(-rx); }//reverses the rx,ry,pi/2 orientation of the camera eye - paints on screen and is unaffected by camera movement

	/**
	 * return x screen value for 3d point
	 * @param x
	 * @param y
	 * @param z
	 */
	@Override
	public float getSceenX(float x, float y, float z) {		return screenX(x,y,z);	};
	/**
	 * return y screen value for 3d point
	 * @param x
	 * @param y
	 * @param z
	 */
	@Override
	public float getSceenY(float x, float y, float z) {		return screenY(x,y,z);	};
	/**
	 * return screen value of z (Z-buffer) for 3d point
	 * @param x
	 * @param y
	 * @param z
	 */
	@Override
	public float getSceenZ(float x, float y, float z) {		return screenZ(x,y,z);	};
	
	/**
	 * return target frame rate
	 * @return
	 */
	@Override
	public final float getFrameRate() {return frameRate;}
	@Override
	public final myPoint getMouse_Raw() {return new myPoint(mouseX, mouseY,0);}                                          			// current mouse location
	@Override
	public final myVector getMouseDrag() {return new myVector(mouseX-pmouseX,mouseY-pmouseY,0);};                     			// vector representing recent mouse displacement
	
	@Override
	public final myPointf getMouse_Raw_f() {return new myPointf(mouseX, mouseY,0);}                                          			// current mouse location
	@Override
	public final myVectorf getMouseDrag_f() {return new myVectorf(mouseX-pmouseX,mouseY-pmouseY,0);};                     			// vector representing recent mouse displacement

	@Override
	public final int[] getMouse_Raw_Int() {return new int[] {mouseX, mouseY};}                                          			// current mouse location
	@Override
	public final int[] getMouseDrag_Int() {return new int[] {mouseX-pmouseX,mouseY-pmouseY};};              			// vector representing recent mouse displacement

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
		int newY = height - y;		pgl.readPixels(x, newY - 1, 1, 1, PGL.DEPTH_COMPONENT, PGL.FLOAT, depthBuffer);
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
		int newY = height - y;
		if(depthValue == -1){depthValue = getDepth(x, y); }	
		//get 3d transform matrices
		PGraphics3D p3d = (PGraphics3D)g;
		// build normalized x,y prenormalized depth homogeneous screen point
		float[] normalized = new float[] {
				(x/(width * 0.5f)) - 1.0f, 
				(newY/(height* 0.5f)) - 1.0f, 
				depthValue * 2.0f - 1.0f, 
				1.0f};
		
		// Get projection matrix
		PMatrix3D modelViewProjInv = p3d.projection.get(),
				modelView = p3d.modelview.get(); 
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
	 * @param x
	 * @param y
	 * @param depthValue
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
	public void setFill(int r, int g, int b, int alpha){fill(r,g,b,alpha);}
	@Override
	public void setStroke(int r, int g, int b, int alpha){stroke(r,g,b,alpha);}
	/**
	 * set stroke weight
	 */
	@Override
	public void setStrokeWt(float stW) {	strokeWeight(stW);}
	@Override
	public void setColorValFill(int colorVal, int alpha){
		if(colorVal == gui_TransBlack) {
			fill(0x00010100);//	have to use hex so that alpha val is not lost    TODO not taking care of alpha here
		} else {
			setFill(getClr(colorVal, alpha), alpha);
		}	
	}//setcolorValFill
	@Override
	public void setColorValStroke(int colorVal, int alpha){
		setStroke(getClr(colorVal, alpha), alpha);		
	}//setcolorValStroke	
	@Override
	public void setColorValFillAmb(int colorVal, int alpha){
		if(colorVal == gui_TransBlack) {
			fill(0x00010100);//	have to use hex so that alpha val is not lost    
			ambient(0,0,0);
		} else {
			int[] fillClr = getClr(colorVal, alpha);
			setFill(fillClr, alpha);
			ambient(fillClr[0],fillClr[1],fillClr[2]);
		}		
	}//setcolorValFill

	/**
	 * any instancing-class-specific colors - colorVal set to be higher than IRenderInterface.gui_OffWhite
	 * @param colorVal
	 * @param alpha
	 * @return
	 */
	@Override
	public int[] getClr_Custom(int colorVal, int alpha) {return AppMgr.getClr_Custom(colorVal, alpha); }		
	@Override
	public final int[] getRndClr(int alpha){
		return new int[]{ThreadLocalRandom.current().nextInt(256),
					ThreadLocalRandom.current().nextInt(256),
					ThreadLocalRandom.current().nextInt(256),alpha};	}
	@Override
	public final int[] getRndClrBright(int alpha){
		return new int[]{ThreadLocalRandom.current().nextInt(50,256),
				ThreadLocalRandom.current().nextInt(25,256),
				ThreadLocalRandom.current().nextInt(80,256),alpha};	}
	
	@Override
	public final int getRndClrIndex(){return ThreadLocalRandom.current().nextInt(0,IRenderInterface.gui_nextColorIDX);}		//return a random color flag value from IRenderInterface
		
	@Override
	public final Integer[] getClrMorph(int[] a, int[] b, double t){
		if(t==0){return new Integer[]{a[0],a[1],a[2],a[3]};} else if(t==1){return new Integer[]{b[0],b[1],b[2],b[3]};}
		return new Integer[]{(int)(((1.0f-t)*a[0])+t*b[0]),(int)(((1.0f-t)*a[1])+t*b[1]),(int)(((1.0f-t)*a[2])+t*b[2]),(int)(((1.0f-t)*a[3])+t*b[3])};
	}

	//save screenshot
	public void savePic(){			
		String picName = AppMgr.getAnimPicName(); 
		if(null==picName) {return;}
		save(picName);
	}


}//my_procApplet
