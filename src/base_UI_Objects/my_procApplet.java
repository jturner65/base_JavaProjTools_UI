package base_UI_Objects;


import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayDeque;

import base_JavaProjTools_IRender.base_Render_Interface.IRenderInterface;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;
import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;

import processing.core.PConstants;
import processing.core.PMatrix3D;
import processing.event.MouseEvent;
import processing.opengl.PGL;
import processing.opengl.PGraphics3D;

public final class my_procApplet extends processing.core.PApplet implements IRenderInterface {
	
	public static GUI_AppManager AppMgr;
	
	protected int glblStartSimFrameTime,			//begin of draw
		glblLastSimFrameTime,					//begin of last draw
		glblStartProgTime;					//start of program	
	
	//data being printed to console - show on screen
	public ArrayDeque<String> consoleStrings;		
	//count of draw cycles
	public int drawCount;
	//how long a message should last before it is popped from the console strings deque (how many frames)
	protected final int cnslStrDecay = 10;			
	
	public final float frate = 120;			//frame rate - # of playback updates per second
	
	//animation control variables	
	public final float maxAnimCntr = PI*1000.0f, baseAnimSpd = 1.0f;
	//9 element array holding camera loc, target, and orientation
	public float[] camVals;		
	
	//replace old displayWidth, displayHeight variables being deprecated in processing
	protected static int _displayWidth, _displayHeight;
	////////////////////////
	// code
	
	///////////////////////////////////
	/// inits
	///////////////////////////////////

	//needs main to run project - do not modify this code in any way 
	//needs to be called by instancing class
	public final static void _invokedMain(GUI_AppManager _appMgr, String[] passedArgs) {	
		String[] appletArgs = new String[] { "base_UI_Objects.my_procApplet" };
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		_displayWidth = gd.getDisplayMode().getWidth();
		_displayHeight = gd.getDisplayMode().getHeight();
		AppMgr = _appMgr;
		if (passedArgs != null) {processing.core.PApplet.main(processing.core.PApplet.concat(appletArgs, passedArgs)); } else {processing.core.PApplet.main(appletArgs);		    }
	    
	}//main	

	//processing being run in eclipse uses settings for variable size dimensions
	public void settings(){	
		AppMgr.setIRenderInterface(this);
		int[] desDims = getIdealAppWindowDims();
		size(desDims[0], desDims[1],P3D);	
		//allow user to set smoothing
		AppMgr.setSmoothing();
		//noSmooth();
	}	
	
	/**
	 * this will manage very large displays, while scaling window to smaller displays
	 * the goal is to preserve a reasonably close to 16:10 ratio window with big/widescreen displays
	 * @return int[] { desired application window width, desired application window height}
	 */
	protected final int[] getIdealAppWindowDims() {		
		int winSizeCntrl = AppMgr.setAppWindowDimRestrictions();		
		switch(winSizeCntrl) {
			case 0 : {//don't care about window dimensions
				return new int[] {(int)(_displayWidth*.95f), (int)(_displayHeight*.92f)};
			}
			case 1 : {//make screen manageable for wide screen monitors
				float displayRatio = _displayWidth/(1.0f*_displayHeight);
				float newWidth = (displayRatio > maxWinRatio) ?  _displayWidth * maxWinRatio/displayRatio : _displayWidth;
				return new int[] {(int)(newWidth*.95f), (int)(_displayHeight*.92f)};
			}
			default :{//unsupported winSizeCntrl setting >= 2
				System.out.println("Unsupported value from setAppWindowDimRestrictions(). Defaulting to 0.");
				return new int[] {(int)(_displayWidth*.95f), (int)(_displayHeight*.92f)};
			}			
		}
	}//getIdealAppWindowDims
	
	
	@Override
	public void setup() {
		colorMode(RGB, 255, 255, 255, 255);
//		frameRate(frate);
		AppMgr.setup_indiv();
		initVisOnce();						//always first
		//call this in first draw loop?
		AppMgr.initOnce();		
		//needs to be the last thing called in setup, to avoid timeout 5000ms issue
		frameRate(frate);
	}//setup()
	
	//public int getNumThreadsAvailable() {return Runtime.getRuntime().availableProcessors();}
		//1 time initialization of visualization things that won't change
	public void initVisOnce(){
		//setup default stroke ends.  ROUND is very slow, SQUARE  makes points invisible	
		strokeCap(PROJECT);
		textSize(txtSz);
		textureMode(NORMAL);			
		rectMode(CORNER);	
		sphereDetail(4);
		//data being printed to console	
		consoleStrings = new ArrayDeque<String>();
		drawCount = 0;
		//mouse scrolling scale
		AppMgr.firstInit(width, height);
		
		outStr2Scr("Current sketchPath " + sketchPath());
		//finalize windows
		AppMgr.endInit();
		//camVals = new float[]{width/2.0f, height/2.0f, (height/2.0f) / tan(PI/6.0f), width/2.0f, height/2.0f, 0, 0, 1, 0};
		camVals = new float[]{0, 0, (height/2.0f) / tan(PI/6.0f), 0, 0, 0, 0,1,0};
		
		
		glblStartProgTime = millis();
		glblStartSimFrameTime = glblStartProgTime;
		glblLastSimFrameTime =  glblStartProgTime;	
		
	}//	initVisOnce
	
	public int timeSinceStart() {return millis() - glblStartProgTime;}

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
	
	//get difference between frames and set both glbl times
	private float getModAmtMillis() {
		glblStartSimFrameTime = millis();
		float modAmtMillis = (glblStartSimFrameTime - glblLastSimFrameTime);
		glblLastSimFrameTime = millis();
		return modAmtMillis;
	}	
	/**
	 * setup 
	 */
	public void drawSetup(){
		perspective(PI/3.0f, (1.0f*width)/(1.0f*height), .5f, camVals[2]*100.0f);
		enableLights(); 	
	    //dispWinFrames[curFocusWin].drawSetupWin(camVals);
	    AppMgr.getCurFocusDispWindow().drawSetupWin(camVals);
	}//drawSetup		


	/**
	 * main draw loop - override if handling draw differently
	 */
	@Override
	public void draw(){
		if(!AppMgr.isFinalInitDone()) {drawCount = 0;AppMgr.initOnce(); return;}	
		float modAmtMillis = getModAmtMillis();
		//simulation section
		boolean execSim = AppMgr.execSimDuringDrawLoop(modAmtMillis);
		if(execSim) {++drawCount;}
		//drawing section
		pushMatState();
		drawSetup();																//initialize camera, lights and scene orientation and set up eye movement
		AppMgr.drawMe(modAmtMillis);				
		
		//Draw UI and 
		AppMgr.drawUI(modAmtMillis);												//draw UI overlay on top of rendered results			
		if (AppMgr.doSaveAnim()) {	savePic();}
		updateConsoleStrs();
		//build window title
		surface.setTitle(AppMgr.getPrjNmLong() + " : " + (int)(frameRate) + " fps|cyc curFocusWin : " + AppMgr.curFocusWin);
	}//draw	
	
	/**
	 * draw a translucent represenation of a canvas plane ortho to eye-to-mouse vector
	 * @param eyeToMse vector 
	 * @param canvas3D
	 */
	@Override
	public void drawCanvas(myVector eyeToMse, myPointf[] canvas3D){
		disableLights();
		pushMatState();
		beginShape(PConstants.QUAD);
		setFill(255,255,255,80);
		//p.noStroke();
		gl_normal(eyeToMse);
     	//for(int i =0;i<canvas3D.length;++i){		//build invisible canvas to draw upon
        for(int i =canvas3D.length-1;i>=0;--i){		//build invisible canvas to draw upon
     		//p.line(canvas3D[i], canvas3D[(i+1)%canvas3D.length]);
     		gl_vertex(canvas3D[i]);
     	}
     	endShape(PConstants.CLOSE);
     	popMatState();
     	enableLights();
	}//drawCanvas
	
	private void updateConsoleStrs(){
		++drawCount;
		if(drawCount % cnslStrDecay == 0){drawCount = 0;	consoleStrings.poll();}			
	}//updateConsoleStrs
	
//	@Override
//	/**
//	 * main draw loop - override if handling draw differently
//	 */
//	public void draw(){
//		if(!AppMgr.isFinalInitDone()) {AppMgr.initOnce(); return;}	
//		float modAmtMillis = getModAmtMillis();
//		//simulation section
//		AppMgr.execRunSim(modAmtMillis);
////		if(AppMgr.isRunSim() ){
////			//run simulation
////			drawCount++;									//needed here to stop draw update so that pausing sim retains animation positions	
////			for(int i =1; i<AppMgr.numDispWins; ++i){if((isShowingWindow(i)) && (AppMgr.dispWinFrames[i].getFlags(myDispWindow.isRunnable))){AppMgr.dispWinFrames[i].simulate(modAmtMillis);}}
////			if(AppMgr.isSingleStep()){AppMgr.setSimIsRunning(false);}
////			simCycles++;
////		}		//play in current window
//
//		//drawing section
//		pushMatState();
//		drawSetup();																//initialize camera, lights and scene orientation and set up eye movement
//		
//		
//		if((AppMgr.curFocusWin == -1) || (AppMgr.curDispWinIs3D())){	//allow for single window to have focus, but display multiple windows	
//			//if refreshing screen, this clears screen, sets background
//			if(AppMgr.getShouldClearBKG()) {
//				AppMgr.setBkgrnd();				
//				AppMgr.draw3D_solve3D(modAmtMillis, -c.getViewDimW()/2.0f+40);
//				c.buildCanvas();
//				if(AppMgr.curDispWinCanShow3dbox()){AppMgr.drawBoxBnds();}
//				if(AppMgr.getCurFocusDispWindow().chkDrawMseRet()){			c.drawMseEdge(AppMgr.getCurFocusDispWindow());	}		
//			} else {
//				AppMgr.draw3D_solve3D(modAmtMillis, -c.getViewDimW()/2.0f+40);
//				c.buildCanvas();
//			}
//			popMatState(); 
//		} else {	//either/or 2d window
//			//2d windows paint window box so background is always cleared
//			c.buildCanvas();
//			c.drawMseEdge(AppMgr.getCurFocusDispWindow());
//			popMatState(); 
//			//for(int i =1; i<numDispWins; ++i){if (isShowingWindow(i) && !(dispWinFrames[i].getFlags(myDispWindow.is3DWin))){dispWinFrames[i].draw2D(modAmtMillis);}}
//			AppMgr.draw2D(modAmtMillis);
//		}
//		AppMgr.drawUI(modAmtMillis);																	//draw UI overlay on top of rendered results			
//		if (AppMgr.doSaveAnim()) {	savePic();}
//		AppMgr.updateConsoleStrs();
//		surface.setTitle(AppMgr.getPrjNmLong() + " : " + (int)(frameRate) + " fps|cyc curFocusWin : " + AppMgr.curFocusWin);
//	}//draw	
//	
	
//	protected abstract String getPrjNmLong();
//	protected abstract String getPrjNmShrt();

	
//	protected final void draw3D_solve3D(float modAmtMillis){
//		//System.out.println("drawSolve");
//		pushMatState();
//		for(int i =1; i<numDispWins; ++i){
//			if((isShowingWindow(i)) && (dispWinFrames[i].getFlags(myDispWindow.is3DWin))){	dispWinFrames[i].draw3D(modAmtMillis);}
//		}
//		popMatState();
//		//fixed xyz rgb axes for visualisation purposes and to show movement and location in otherwise empty scene
//		drawAxes(100,3, new myPoint(-c.getViewDimW()/2.0f+40,0.0f,0.0f), 200, false); 		
//	}//draw3D_solve3D
	
//	protected final void drawUI(float modAmtMillis){					
//		//for(int i =1; i<numDispWins; ++i){if ( !(dispWinFrames[i].dispFlags[myDispWindow.is3DWin])){dispWinFrames[i].draw(sceneCtrVals[sceneIDX]);}}
//		//dispWinFrames[0].draw(sceneCtrVals[sceneIDX]);
//		for(int i =1; i<numDispWins; ++i){dispWinFrames[i].drawHeader(modAmtMillis);}
//		//menu always idx 0
//		normal(0,0,1);
//		dispWinFrames[0].draw2D(modAmtMillis);
//		dispWinFrames[0].drawHeader(modAmtMillis);
//		drawOnScreenData();				//debug and on-screen data
//	}//drawUI	
//	
//	/**
//	 * Draw Axes at ctr point
//	 * @param len length of axis
//	 * @param stW stroke weight (line thickness)
//	 * @param ctr ctr point to draw axes
//	 * @param alpha alpha value for how dark/faint axes should be
//	 * @param centered whether axis should be centered at ctr or just in positive direction at ctr
//	 */
//	@Override
//	public void drawAxes(double len, float stW, myPoint ctr, int alpha, boolean centered){//axes using current global orientation
//		pushMatState();
//			strokeWeight(stW);
//			stroke(255,0,0,alpha);
//			if(centered){
//				double off = len*.5f;
//				line(ctr.x-off,ctr.y,ctr.z,ctr.x+off,ctr.y,ctr.z);stroke(0,255,0,alpha);line(ctr.x,ctr.y-off,ctr.z,ctr.x,ctr.y+off,ctr.z);stroke(0,0,255,alpha);line(ctr.x,ctr.y,ctr.z-off,ctr.x,ctr.y,ctr.z+off);} 
//			else {		line(ctr.x,ctr.y,ctr.z,ctr.x+len,ctr.y,ctr.z);stroke(0,255,0,alpha);line(ctr.x,ctr.y,ctr.z,ctr.x,ctr.y+len,ctr.z);stroke(0,0,255,alpha);line(ctr.x,ctr.y,ctr.z,ctr.x,ctr.y,ctr.z+len);}
//		popStyle();	popMatrix();	
//	}//	drawAxes
//	@Override
//	public void drawAxes(double len, float stW, myPoint ctr, myVector[] _axis, int alpha, boolean drawVerts){//RGB -> XYZ axes
//		pushMatState();
//		if(drawVerts){
//			showPtAsSphere(ctr,3,5,gui_Black,gui_Black);
//			for(int i=0;i<_axis.length;++i){showPtAsSphere(myPoint._add(ctr, myVector._mult(_axis[i],len)),3,5,rgbClrs[i],rgbClrs[i]);}
//		}
//		strokeWeight(stW);
//		for(int i =0; i<3;++i){	setColorValStroke(rgbClrs[i],255);	showVec(ctr,len, _axis[i]);	}
//		popStyle();	popMatrix();	
//	}//	drawAxes
//	@Override
//	public void drawAxes(double len, float stW, myPoint ctr, myVector[] _axis, int[] clr, boolean drawVerts){//all axes same color
//		pushMatState();
//			if(drawVerts){
//				showPtAsSphere(ctr,2,5,gui_Black,gui_Black);
//				for(int i=0;i<_axis.length;++i){showPtAsSphere(myPoint._add(ctr, myVector._mult(_axis[i],len)),2,5,rgbClrs[i],rgbClrs[i]);}
//			}
//			strokeWeight(stW);stroke(clr[0],clr[1],clr[2],clr[3]);
//			for(int i =0; i<3;++i){	showVec(ctr,len, _axis[i]);	}
//		popStyle();	popMatrix();	
//	}//	drawAxes
//	@Override
//	public void drawAxes(double len, double stW, myPoint ctr, myVectorf[] _axis, int alpha){
//		pushMatState();
//			strokeWeight((float)stW);
//			stroke(255,0,0,alpha);
//			line(ctr.x,ctr.y,ctr.z,ctr.x+(_axis[0].x)*len,ctr.y+(_axis[0].y)*len,ctr.z+(_axis[0].z)*len);
//			stroke(0,255,0,alpha);
//			line(ctr.x,ctr.y,ctr.z,ctr.x+(_axis[1].x)*len,ctr.y+(_axis[1].y)*len,ctr.z+(_axis[1].z)*len);	
//			stroke(0,0,255,alpha);	
//			line(ctr.x,ctr.y,ctr.z,ctr.x+(_axis[2].x)*len,ctr.y+(_axis[2].y)*len,ctr.z+(_axis[2].z)*len);
//		popStyle();	popMatrix();	
//	}//	drawAxes
	
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
	public void gl_SetFill(int r, int g, int b, int alpha) {super.fill(r,g,b,alpha);}

	/**
	 * set stroke color by value during shape building
	 * @param clr rgba
	 * @param alpha 
	 */
	@Override
	public void gl_SetStroke(int r, int g, int b, int alpha) {super.stroke(r,g,b,alpha);}	
	
	
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
	public void gl_beginShape(int type) {
		if(type==-1) {			beginShape(POLYGON);		}
		else {				beginShape(type);		}
	}
	/**
	 * type needs to be -1 for blank, otherwise will be CLOSE, regardless of passed value
	 */
	@Override
	public void gl_endShape(int type) {		
		if(type==-1) {			endShape();		}
		else {				endShape(CLOSE);		}
	}
	
	@Override
	public void drawSphere(float rad) {sphere(rad);}
	@Override
	public void setSphereDetail(int det) {sphereDetail(det);}
	
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
		beginShape();
		this.strokeWeight(1.0f);
		this.setColorValStroke(stClr, 255);
		this.vertex((float)a.x,(float)a.y,(float)a.z);
		this.setColorValStroke(endClr,255);
		this.vertex((float)b.x,(float)b.y,(float)b.z);
		endShape();
	}
	@Override
	public void drawLine(myPointf a, myPointf b, int[] stClr, int[] endClr){
		beginShape();
		this.strokeWeight(1.0f);
		this.setStroke(stClr, 255);
		this.vertex((float)a.x,(float)a.y,(float)a.z);
		this.setStroke(endClr,255);
		this.vertex((float)b.x,(float)b.y,(float)b.z);
		endShape();
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
		beginShape(PConstants.POINTS);
		for(int i=0;i<=numPts-ptIncr;i+=ptIncr) {				
			//pa.stroke(h_part_clr[i][0], h_part_clr[i][1], h_part_clr[i][2]);
			stroke(h_part_clr_int[i][0], h_part_clr_int[i][1], h_part_clr_int[i][2]);
			//pa.point(h_part_pos_x[i], h_part_pos_y[i], h_part_pos_z[i]);
			vertex(h_part_pos_x[i], h_part_pos_y[i], h_part_pos_z[i]);
		}
		endShape();
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
	public void drawCircle3D(myPoint P, float r, myVector I, myVector J, int n) {
		myPoint[] pts = AppMgr.buildCircleInscribedPoints(P,r,I,J,n);
		pushMatState();noFill(); show(pts);popMatState();
	}; 
	@Override
	public void drawCircle3D(myPointf P, float r, myVectorf I, myVectorf J, int n) {
		myPointf[] pts = AppMgr.buildCircleInscribedPoints(P,r,I,J,n);
		pushMatState();noFill(); show(pts);popMatState();
	}; 
	
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

	
	private final float deltaThet = MyMathUtils.twoPi_f/36, finalThet = MyMathUtils.twoPi_f+deltaThet;
	@Override
	public void drawCylinder_NoFill(myPoint A, myPoint B, float r, int c1, int c2) {
		myVector[] frame = AppMgr.buildViewBasedFrame(A, B);
		float rca, rsa;
		noFill();
		beginShape(QUAD_STRIP);
			for(float a=0; a<=finalThet; a+=deltaThet) {
				stroke(c1); 
				rca = r*cos(a);rsa=r*sin(a);
				//gl_vertex(myPoint._add(A,r*cos(a),I,r*sin(a),J,0,V)); 
				gl_vertex(myPoint._add(A,rca,frame[1],rsa,frame[2])); 
				stroke(c2); 
				gl_vertex(myPoint._add(A,rca,frame[1],rsa,frame[2],1,frame[0]));}
		endShape();
	}
	@Override
	public void drawCylinder_NoFill(myPointf A, myPointf B, float r, int c1, int c2) {
		myVectorf[] frame = AppMgr.buildViewBasedFrame_f(A, B);
		float rca, rsa;
		noFill();
		beginShape(QUAD_STRIP);
			for(float a=0; a<=finalThet; a+=deltaThet) {
				stroke(c1); 
				rca = r*cos(a);rsa=r*sin(a);
				//gl_vertex(myPoint._add(A,r*cos(a),I,r*sin(a),J,0,V)); 
				gl_vertex(myPointf._add(A,rca,frame[1],rsa,frame[2])); 
				stroke(c2); 
				gl_vertex(myPointf._add(A,rca,frame[1],rsa,frame[2],1,frame[0]));}
		endShape();
	}

	@Override
	public void drawCylinder(myPoint A, myPoint B, float r, int c1, int c2) {
		myVector[] frame = AppMgr.buildViewBasedFrame(A, B);
		float rca, rsa;
		beginShape(QUAD_STRIP);
			for(float a=0; a<=finalThet; a+=deltaThet) {
				fill(c1); 
				rca = r*cos(a);rsa=r*sin(a);
				//gl_vertex(myPoint._add(A,r*cos(a),I,r*sin(a),J,0,V)); 
				gl_vertex(myPoint._add(A,rca,frame[1],rsa,frame[2])); 
				fill(c2); 
				gl_vertex(myPoint._add(A,rca,frame[1],rsa,frame[2],1,frame[0]));}
		endShape();
	}
	
	@Override
	public void drawCylinder(myPointf A, myPointf B, float r, int c1, int c2) {
		myVectorf[] frame = AppMgr.buildViewBasedFrame_f(A, B);
		float rca, rsa;
		beginShape(QUAD_STRIP);
			for(float a=0; a<=finalThet; a+=deltaThet) {
				fill(c1); 
				rca = r*cos(a);rsa=r*sin(a);
				//gl_vertex(myPoint._add(A,r*cos(a),I,r*sin(a),J,0,V)); 
				gl_vertex(myPointf._add(A,rca,frame[1],rsa,frame[2])); 
				fill(c2); 
				gl_vertex(myPointf._add(A,rca,frame[1],rsa,frame[2],1,frame[0]));}
		endShape();
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
	public void mouseWheel(MouseEvent event) {		AppMgr.mouseWheel(event);	}
	/**
	 * called by papplet super
	 */
	@Override
	public void mouseReleased(){	AppMgr.mouseReleased();	}
	
//	public void setCamOrient_Glbl(){rotateX(rx);rotateY(ry); rotateX(PI/(2.0f));		}//sets the rx, ry, pi/2 orientation of the camera eye	
//	public void unSetCamOrient_Glbl(){rotateX(-PI/(2.0f)); rotateY(-ry);   rotateX(-rx); }//reverses the rx,ry,pi/2 orientation of the camera eye - paints on screen and is unaffected by camera movement
	
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
	
	/**
	 * print out a string ara to screen with perLine # of strings per line
	 * @param sAra array of strings
	 * @param perLine # of strings per line to display to screen
	 */
	@Override
	public void outStr2ScrAra(String[] sAra, int perLine){
		for(int i=0;i<sAra.length; i+=perLine){
			String s = "";
			for(int j=0; j<perLine; ++j){	s+= sAra[i+j]+ "\t";}
			outStr2Scr(s,true);}
	}
	/**
	 * print out a string to screen
	 * @param str string to display
	 */	
	@Override
	public void outStr2Scr(String str){outStr2Scr(str,true);}
	/**
	 * print informational string data to console, and to screen
	 * @param str
	 * @param showDraw whether to show in graphical window as well as console
	 */

	@Override
	public void outStr2Scr(String str, boolean showDraw){
		if(str.trim() != ""){	System.out.println(str);}
		String[] res = str.split("\\r?\\n");
		if(showDraw){
			for(int i =0; i<res.length; ++i){
				consoleStrings.add(res[i]);		//add console string output to screen display- decays over time
			}
		}
	}

	@Override
	public void bezier(myPoint A, myPoint B, myPoint C, myPoint D) {bezier((float)A.x,(float)A.y,(float)A.z,(float)B.x,(float)B.y,(float)B.z,(float)C.x,(float)C.y,(float)C.z,(float)D.x,(float)D.y,(float)D.z);} // draws a cubic Bezier curve with control points A, B, C, D
	@Override
	public final myPoint bezierPoint(myPoint[] C, float t) {return new myPoint(bezierPoint((float)C[0].x,(float)C[1].x,(float)C[2].x,(float)C[3].x,(float)t),bezierPoint((float)C[0].y,(float)C[1].y,(float)C[2].y,(float)C[3].y,(float)t),bezierPoint((float)C[0].z,(float)C[1].z,(float)C[2].z,(float)C[3].z,(float)t)); }
	@Override
	public final myVector bezierTangent(myPoint[] C, float t) {return new myVector(bezierTangent((float)C[0].x,(float)C[1].x,(float)C[2].x,(float)C[3].x,(float)t),bezierTangent((float)C[0].y,(float)C[1].y,(float)C[2].y,(float)C[3].y,(float)t),bezierTangent((float)C[0].z,(float)C[1].z,(float)C[2].z,(float)C[3].z,(float)t)); }
	
//	//public final int color(myPoint p){return color((int)p.x,(int)p.z,(int)p.y);}	//needs to be x,z,y for some reason - to match orientation of color frames in z-up 3d geometry
//	public final int color(myPoint p){return color((int)p.x,(int)p.y,(int)p.z);}	
//	public final int color(myPointf p){return color((int)p.x,(int)p.y,(int)p.z);}	
	/**
	 * vertex with texture coordinates
	 * @param P vertex location
	 * @param u,v txtr coords
	 */
	public void vTextured(myPointf P, float u, float v) {vertex((float)P.x,(float)P.y,(float)P.z,(float)u,(float)v);};                         

	
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
	
	private void checkClrInts(int fclr, int sclr) {
		if(fclr > -1){setColorValFill(fclr,255); } else if(fclr <= -2) {noFill();}		
		if(sclr > -1){setColorValStroke(sclr,255);} else if(sclr <= -2) {noStroke();}
	}
	
	private void checkClrIntArrays(int[] fclr, int[] sclr) {
		if(fclr!= null){setFill(fclr,255);}
		if(sclr!= null){setStroke(sclr,255);}
	}	
	
	///////////
	// end text
	
	///////////
	// points
	
	//base show function
//	public void show(myPoint P, double rad, int det){			
//		pushMatState(); 
//		fill(0,0,0,255); 
//		stroke(0,0,0,255);
//		drawSphere(P, rad, det);
//		popMatState();
//	}
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
	//public void showText(myPoint P, double r, String s, myVector D){showPtAsSphere(P,r, 5, gui_Black, gui_Black);pushStyle();setColorValFill(gui_Black,255);showText(P,s,D);popStyle();}
	
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
	
	public void show(myPoint[] ara) {beginShape(); for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} endShape(CLOSE);};                     
	public void show(myPoint[] ara, myVector norm) {beginShape();gl_normal(norm); for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} endShape(CLOSE);};   
	
	///////////
	// end double points
	///////////
	// float points (pointf)
	
	/////////////
	//base show functions
//	public void show(myPointf P, float rad, int det){			
//		pushMatState(); 
//		fill(0,0,0,255); 
//		stroke(0,0,0,255);
//		drawSphere(P, rad, det);
//		popMatState();
//	}
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

//	
//	public void show(myPointf P, float rad, int det, int[] fclr, int[] sclr) {
//		pushMatState(); 
//		if((fclr!= null) && (sclr!= null)){setFill(fclr,255); setStroke(sclr,255);}
//		drawSphere(P,rad, det);
//		popMatState();
//	}// render sphere of radius r and center P)

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
	public void showOffsetTextAra(float d, int tclr, String[] txtAra){
		setColorValFill(tclr, 255);setColorValStroke(tclr, 255);
		float y = d;
		for (String txt : txtAra) {
			showText(txt, d, y, d);
			y+=10;
		}
	}	

//	public void show(myPointf P, float rad, int det, int[] clrs) {//only call with set fclr and sclr - idx0 == fill, idx 1 == strk
//		pushMatState(); 
//		setColorValFill(clrs[0],255); 
//		setColorValStroke(clrs[1],255);
//		drawSphere(P, rad, det);
//		popMatState();
//	} // render sphere of radius r and center P)
//	
	/**
	 * show array displayed at specific point on screens
	 * @param P
	 * @param rad
	 * @param det
	 * @param clrs
	 * @param txtAra
	 */
	@Override
	public void showTxtAra(myPointf P, float rad, int det, int[] clrs, String[] txtAra) {//only call with set fclr and sclr - idx0 == fill, idx 1 == strk, idx2 == txtClr
		pushMatState(); 
		setColorValFill(clrs[0],255); 
		setColorValStroke(clrs[1],255);
		drawSphere(P, rad, det);
		translate(P.x,P.y,P.z); 
		showOffsetTextAra(1.2f * rad, clrs[2], txtAra);
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
	public void showBoxTxtAra(myPointf P, float rad, int det, int[] clrs, String[] txtAra, float[] rectDims) {
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
				showOffsetTextAra(1.2f * rad, clrs[2], txtAra);
			 popMatState();
		 popMatState();
	} // render sphere of radius r and center P)
	
	
	//public void showText(myPointf P, float r, String s, myVectorf D){showPtAsSphere(P,r,5, gui_Black, gui_Black);pushStyle();setColorValFill(gui_Black,255);showText(P,s,D);popStyle();}
	
	public void show(myPointf[] ara) {beginShape(); for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} endShape(CLOSE);};                     
	public void show(myPointf[] ara, myVectorf norm) {beginShape();gl_normal(norm); for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} endShape(CLOSE);};                     
	
	public void showNoClose(myPoint[] ara) {beginShape(); for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} endShape();};                     
	public void showNoClose(myPointf[] ara) {beginShape(); for(int i=0;i<ara.length;++i){gl_vertex(ara[i]);} endShape();};     

	
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
			beginShape(); curveVertex2D(ara[0]);for(int i=0;i<ara.length;++i){curveVertex2D(ara[i]);} curveVertex2D(ara[ara.length-1]);endShape();
			return;}		
		beginShape(); for(int i=0;i<ara.length;++i){curveVertex2D(ara[i]);} endShape();
	}
	/**
	 * implementation of catumull rom - array needs to be at least 4 points, if not, then reuses first and last points as extra cntl points  
	 * @param pts
	 */
	@Override
	public void catmullRom2D(myPoint[] ara) {
		if(ara.length < 4){
			if(ara.length == 0){return;}
			beginShape(); curveVertex2D(ara[0]);for(int i=0;i<ara.length;++i){curveVertex2D(ara[i]);} curveVertex2D(ara[ara.length-1]);endShape();
			return;}		
		beginShape(); for(int i=0;i<ara.length;++i){curveVertex2D(ara[i]);} endShape();		
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
			beginShape(); curveVertex3D(ara[0]);for(int i=0;i<ara.length;++i){curveVertex3D(ara[i]);} curveVertex3D(ara[ara.length-1]);endShape();
			return;}		
		beginShape(); for(int i=0;i<ara.length;++i){curveVertex3D(ara[i]);} endShape();
	}
	/**
	 * implementation of catumull rom - array needs to be at least 4 points, if not, then reuses first and last points as extra cntl points  
	 * @param pts
	 */
	@Override
	public void catmullRom3D(myPoint[] ara) {
		if(ara.length < 4){
			if(ara.length == 0){return;}
			beginShape(); curveVertex3D(ara[0]);for(int i=0;i<ara.length;++i){curveVertex3D(ara[i]);} curveVertex3D(ara[ara.length-1]);endShape();
			return;}		
		beginShape(); for(int i=0;i<ara.length;++i){curveVertex3D(ara[i]);} endShape();		
	}
	
	protected final void curveVertex3D(myPoint P) {curveVertex((float)P.x,(float)P.y,(float)P.z);};                                           // curveVertex for shading or drawing
	protected final void curveVertex3D(myPointf P) {curveVertex(P.x,P.y,P.z);};                                           // curveVertex for shading or drawing


	///////////////////////////////////
	// getters/setters
	//////////////////////////////////
	
	/**
	 * returns the width of the visible display in pxls
	 * @return
	 */
	@Override
	public final int getDisplayWidth() {return _displayWidth;}
	/**
	 * returns the height of the visible display in pxls
	 * @return
	 */
	@Override
	public final int getDisplayHeight() {return _displayHeight;}
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
	 * set camera to passed 9-element values - should be called from window!
	 * @param camVals
	 */
	@Override
	public void setCameraWinVals(float[] camVals) {		camera(camVals[0],camVals[1],camVals[2],camVals[3],camVals[4],camVals[5],camVals[6],camVals[7],camVals[8]);}
	/**
	 * used to handle camera location/motion
	 */
	@Override
	public void setCamOrient(float rx, float ry){rotateX(rx);rotateY(ry); rotateX(MyMathUtils.halfPi_f);		}//sets the rx, ry, pi/2 orientation of the camera eye	
	/**
	 * used to draw text on screen without changing mode - reverses camera orientation setting
	 */
	@Override
	public void unSetCamOrient(float rx, float ry){rotateX(-MyMathUtils.halfPi_f); rotateY(-ry);   rotateX(-rx); }//reverses the rx,ry,pi/2 orientation of the camera eye - paints on screen and is unaffected by camera movement

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
		FloatBuffer depthBuffer = ByteBuffer.allocateDirect(1 << 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
		int newY = height - y;		pgl.readPixels(x, newY - 1, 1, 1, PGL.DEPTH_COMPONENT, PGL.FLOAT, depthBuffer);
		float depthValue = depthBuffer.get(0);
		endPGL();
		return depthValue;
	}
	
	/**
	 * determine world location as myPoint based on mouse click and passed depth
	 * @param x
	 * @param y
	 * @param depth
	 * @return
	 */
	@Override
	public myPoint getWorldLoc(int x, int y, float depth){
		int newY = height - y;
		float depthValue = depth;
		
		if(depth == -1){depthValue = getDepth( x,  y); }	
		//p.outStr2Scr("cur depth in pick : " + depthValue);
		//get 3d matrices
		PGraphics3D p3d = (PGraphics3D)g;
		PMatrix3D proj = p3d.projection.get(), modelView = p3d.modelview.get(), modelViewProjInv = proj; modelViewProjInv.apply( modelView ); modelViewProjInv.invert();	  
		float[] viewport = {0, 0, width, height},
				normalized = new float[] {
						((x - viewport[0]) / viewport[2]) * 2.0f - 1.0f, 
						((newY - viewport[1]) / viewport[3]) * 2.0f - 1.0f, 
						depthValue * 2.0f - 1.0f, 
						1.0f};	  
		float[] unprojected = new float[4];	  
		modelViewProjInv.mult( normalized, unprojected );
		myPoint pickLoc = new myPoint( unprojected[0]/unprojected[3], unprojected[1]/unprojected[3], unprojected[2]/unprojected[3] );
		//p.outStr2Scr("Depth Buffer val : "+String.format("%.4f",depthValue)+ " for mx,my : ("+mX+","+mY+") and world loc : " + pickLoc.toStrBrf());
		return pickLoc;
	}		
	/**
	 * determine world location as myPointf based on mouse click and passed depth
	 * @param x
	 * @param y
	 * @param depth
	 * @return
	 */
	@Override
	public myPointf getWorldLoc_f(int x, int y, float depth){
		int newY = height - y;
		float depthValue = depth;
		
		if(depth == -1){depthValue = getDepth( x,  y); }	
		//p.outStr2Scr("cur depth in pick : " + depthValue);
		//get 3d matrices
		PGraphics3D p3d = (PGraphics3D)g;
		PMatrix3D proj = p3d.projection.get(), modelView = p3d.modelview.get(), modelViewProjInv = proj; modelViewProjInv.apply( modelView ); modelViewProjInv.invert();	  
		float[] viewport = {0, 0, width, height},
				normalized = new float[] {
						((x - viewport[0]) / viewport[2]) * 2.0f - 1.0f, 
						((newY - viewport[1]) / viewport[3]) * 2.0f - 1.0f, 
						depthValue * 2.0f - 1.0f, 
						1.0f};	  
		float[] unprojected = new float[4];	  
		modelViewProjInv.mult( normalized, unprojected );
		myPointf pickLoc = new myPointf( unprojected[0]/unprojected[3], unprojected[1]/unprojected[3], unprojected[2]/unprojected[3] );
		//p.outStr2Scr("Depth Buffer val : "+String.format("%.4f",depthValue)+ " for mx,my : ("+mX+","+mY+") and world loc : " + pickLoc.toStrBrf());
		return pickLoc;
	}		
	
	//public final myPoint WrldToScreen(myPoint wPt){			return new myPoint(screenX((float)wPt.x,(float)wPt.y,(float)wPt.z),screenY((float)wPt.x,(float)wPt.y,(float)wPt.z),screenZ((float)wPt.x,(float)wPt.y,(float)wPt.z));}
	@Override
	public final myPoint getScrLocOf3dWrldPt(myPoint pt){	return new myPoint(screenX((float)pt.x,(float)pt.y,(float)pt.z),screenY((float)pt.x,(float)pt.y,(float)pt.z),screenZ((float)pt.x,(float)pt.y,(float)pt.z));}

	/**
	 * return current array deque of console strings, to be printed to screen
	 * @return
	 */
	@Override
	public ArrayDeque<String> getConsoleStrings() {		return consoleStrings;	}
	
	
	
	
	/////////////////////		
	///color utils
	/////////////////////


//	@Override
//	public void setFill(int[] clr, int alpha){fill(clr[0],clr[1],clr[2], alpha);}
//	@Override
//	public void setStroke(int[] clr, int alpha){stroke(clr[0],clr[1],clr[2], alpha);}
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
	public final int[] getRndClr(int alpha){return new int[]{(int)random(0,255),(int)random(0,255),(int)random(0,255),alpha};	}
	@Override
	public final int[] getRndClrBright(int alpha){return new int[]{(int)random(50,255),(int)random(25,200),(int)random(80,255),alpha};	}
	
	
	public final int getRndClrInt(){return (int)random(0,23);}		//return a random color flag value from below
	public final Integer[] getClrMorph(int a, int b, double t){return getClrMorph(getClr(a,255), getClr(b,255), t);}    
	public final Integer[] getClrMorph(int[] a, int[] b, double t){
		if(t==0){return new Integer[]{a[0],a[1],a[2],a[3]};} else if(t==1){return new Integer[]{b[0],b[1],b[2],b[3]};}
		return new Integer[]{(int)(((1.0f-t)*a[0])+t*b[0]),(int)(((1.0f-t)*a[1])+t*b[1]),(int)(((1.0f-t)*a[2])+t*b[2]),(int)(((1.0f-t)*a[3])+t*b[3])};
	}

	//public final int color(myPoint p){return color((int)p.x,(int)p.z,(int)p.y);}	//needs to be x,z,y for some reason - to match orientation of color frames in z-up 3d geometry
	public final int color(myPoint p){return color((int)p.x,(int)p.y,(int)p.z);}	
	public final int color(myPointf p){return color((int)p.x,(int)p.y,(int)p.z);}	

	//save screenshot
	public void savePic(){			
		String picName = AppMgr.getAnimPicName(); 
		if(null==picName) {return;}
		save(picName);
	}


	protected String getScreenShotSaveName(String prjNmShrt) {
		return sketchPath() +File.separatorChar+prjNmShrt+"_"+AppMgr.getDateString()+File.separatorChar+prjNmShrt+"_img"+AppMgr.getTimeString() + ".jpg";
	}
	
	//handle user-driven file load or save - returns a filename + filepath string
	public String FileSelected(File selection){
		if (null==selection){return null;}
		return selection.getAbsolutePath();		
	}//FileSelected
	

	public String getFName(String fNameAndPath){
		String[] strs = fNameAndPath.split("/");
		return strs[strs.length-1];
	}
	
	//load a file as text strings
	public String[] loadFileIntoStringAra(String fileName, String dispYesStr, String dispNoStr){
		String[] strs = null;
		try{
			strs = loadStrings(fileName);
			System.out.println(dispYesStr+"\tLength : " + strs.length);
		} catch (Exception e){System.out.println("!!"+dispNoStr);return null;}
		return strs;		
	}//loadFileIntoStrings

}//my_procApplet
