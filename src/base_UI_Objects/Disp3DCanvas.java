package base_UI_Objects;

import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;

import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_Render_Interface.IRenderInterface;

/**
 * Class to manage display canvas and reticle for 3d windows
 * @author John Turner
 *
 */
public class Disp3DCanvas {
	/**
	 * GL-interface for rendering and screen<->world coords transforms
	 */
	public IRenderInterface p;
	
	/**
	 * Screen ctr location in world coords
	 */		
	private myPoint scrCtrInWorld;
	private myPoint eyeInWorld; 
	private myPoint oldMseLoc;
	private myPoint dfCtr;														//mouse location projected onto current drawing canvas

	private final float canvasDim = 15000,
			canvasDimOvSqrt2 = MyMathUtils.INV_SQRT_2_F * canvasDim; 			//canvas dimension for "virtual" 3d		
	private myPoint[] canvas3D;													//3d plane, normal to camera eye, to be used for drawing - need to be in "view space" not in "world space", so that if camera moves they don't change
	private myVector eyeToMse,													//eye to 2d mouse location 
					eyeToCtr,													//vector from eye to center of cube, to be used to determine which panels of bounding box to show or hide
					drawSNorm;													//current normal of viewport/screen
		
	private int viewDimW, viewDimH,viewDimW2, viewDimH2;
	
	private int[] mseFillClr;
	
	private GUI_AppManager AppMgr;
	
	/**
	 * Screen-space depth in window of screen-space halfway point.
	 */
	private float rawCtrDepth;
	
	public Disp3DCanvas(GUI_AppManager _AppMgr, IRenderInterface _p, int w, int h) {
		p = _p;
		AppMgr = _AppMgr;
		mseFillClr = new int[] {0,0,0,255};
		initCanvasVars();
		setViewDim(w,h);
	}
	
	private void initCanvasVars(){
		canvas3D = new myPoint[4];		//3 points to define canvas
		canvas3D[0]=new myPoint();canvas3D[1]=new myPoint();canvas3D[2]=new myPoint();canvas3D[3]=new myPoint();
		eyeInWorld = new myPoint();		
		scrCtrInWorld = new myPoint();
		eyeInWorld = new myPoint();
		oldMseLoc  = new myPoint();
		dfCtr = new myPoint();											//mouse location projected onto current drawing canvas
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
		rawCtrDepth = p.getDepth(viewDimW2, viewDimH2);
		buildCanvas();
	}

	/**
	 * find points to define plane normal to camera eye, at set distance from camera, to use drawing canvas 	
	 */
	public void buildCanvas(){	
		//float rawCtrDepth = p.getDepth(viewDimW2, viewDimH2);
		myPoint rawScrCtrInWorld = p.getWorldLoc(viewDimW2, viewDimH2, rawCtrDepth);		
		myVector A = new myVector(rawScrCtrInWorld,  p.getWorldLoc(viewDimW-10, 10, rawCtrDepth)),
				B = new myVector(rawScrCtrInWorld,  p.getWorldLoc(viewDimW-10, viewDimH-10, rawCtrDepth));	//ctr to upper right, ctr to lower right
		drawSNorm = myVector._cross(A,B)._normalize();
		//build plane using norm - have canvas go through canvas ctr in 3d
		myVector planeTan = myVector._cross(drawSNorm, myVector._normalize(new myVector(drawSNorm.x+10000,drawSNorm.y+10,drawSNorm.z+10)))._normalize();			//result of vector crossed with normal will be in plane described by normal
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
		eyeInWorld = p.getWorldLoc(viewDimW2, viewDimH2,-.00001f);
		//eyeInWorld =myPoint._add(rawScrCtrInWorld, myPoint._dist( p.pick(0,0,-1), rawScrCtrInWorld), drawSNorm);								//location of "eye" in world space
		eyeToCtr.set(eyeInWorld, rawScrCtrInWorld);
		scrCtrInWorld = getPlInterSect(rawScrCtrInWorld, myVector._normalize(eyeToCtr));
		
		myPoint mseLocInWorld = getMseLocInWorld();	
		eyeToMse.set(eyeInWorld, mseLocInWorld);		//unit vector in world coords of "eye" to mouse location
		eyeToMse._normalize();
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
		return AppMgr.intersectPl(pt, unitT, canvas3D[0],canvas3D[1],canvas3D[2]);		
	}//getPlInterSect	

	/**
	 * Mouse location in world at given depth
	 * @return
	 */
	public myPoint getMseLocInWorld() {
		float ctrDepth = p.getSceenZ((float)scrCtrInWorld.x, (float)scrCtrInWorld.y, (float)scrCtrInWorld.z);
		int[] mse = p.getMouse_Raw_Int();
		return p.getWorldLoc(mse[0],mse[1],ctrDepth);		
	}

	/**
	 * 
	 * @return
	 */
	public myPoint getMseLoc(){return new myPoint(dfCtr);	}
	/**
	 * 
	 * @return
	 */
	public myPointf getMseLoc_f(){return new myPointf(dfCtr.x,dfCtr.y,dfCtr.z);	}
	/**
	 * 
	 * @return
	 */
	public myPoint getOldMseLoc(){return new myPoint(oldMseLoc);	}	
	/**
	 * 
	 * @return
	 */
	public myVector getMseDragVec(){return new myVector(oldMseLoc,dfCtr);}
	
	/**
	 * relative to passed origin
	 * @param glbTrans
	 * @return
	 */
	public myPoint getMseLoc(myPoint glbTrans){return myPoint._sub(dfCtr, glbTrans);	}
	/**
	 * move by passed translation
	 * @param glbTrans
	 * @return
	 */
	public myPointf getTransMseLoc(myPointf glbTrans){return myPointf._add(dfCtr, glbTrans);	}
	/**
	 * dist from mouse to passed location
	 * @param glbTrans
	 * @return
	 */
	public float getMseDist(myPointf glbTrans){return new myVectorf(dfCtr, glbTrans).magn;	}
	/**
	 * 
	 * @param glbTrans
	 * @return
	 */
	public myPoint getOldMseLoc(myPoint glbTrans){return myPoint._sub(oldMseLoc, glbTrans);	}
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
	

	private final void drawText(Base_DispWindow win, String str, float x, float y, float z){
		p.pushMatState();
			p.setFill(mseFillClr,mseFillClr[3]);
			win.unSetCamOrient();
			p.translate(x,y,z);
			p.showText(str,0,0,0);		
		p.popMatState();	
	}//drawText	

	public void drawMseEdge(Base_DispWindow win, boolean projOnBox){//draw mouse sphere and edge normal to cam eye through mouse sphere 
		p.pushMatState();
			p.setStrokeWt(1f);
			p.setStroke(255, 0,255, 255);
			//draw line through mouse point and eye location in world	
			p.drawLine(eyeInWorld, dfCtr);
			p.translate(dfCtr);
			//project mouse point on bounding box walls if appropriate
			if(projOnBox){AppMgr.drawProjOnBox(dfCtr);}
			AppMgr.drawAxes(10000,1f, myPoint.ZEROPT, 100, true);//
			//draw center point
			p.showPtAsSphere(myPointf.ZEROPT,3.0f, 5, IRenderInterface.gui_Black, IRenderInterface.gui_Black);
			drawText(win, ""+dfCtr+ "|fr:"+p.getFrameRate(),4.0f, 15.0f, 4.0f);
		p.popMatState();			
	}//drawMseEdge		
}//class Disp3DCanvas


