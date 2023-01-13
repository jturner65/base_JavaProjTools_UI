package base_UI_Objects;

import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Math_Objects.vectorObjs.floats.myVectorf;

import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_Render_Interface.IRenderInterface;


public class Disp3DCanvas {
	public IRenderInterface p;
	
	public myPoint drawEyeLoc,													//rx,ry,dz coords where eye was when drawing - set when first drawing and return eye to this location whenever trying to draw again - rx,ry,dz
		scrCtrInWorld,mseLoc, eyeInWorld, oldMseLoc, distMsePt;					//mseIn3DBox;
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
	
	public Disp3DCanvas(GUI_AppManager _AppMgr, IRenderInterface _p, int w, int h) {
		p = (my_procApplet)_p;
		AppMgr = _AppMgr;
		mseFillClr = new int[] {0,0,0,255};
		initCanvasVars();
		setViewDim(w,h);
	}
	
	private void initCanvasVars(){
		canvas3D = new myPoint[4];		//3 points to define canvas
		canvas3D[0]=new myPoint();canvas3D[1]=new myPoint();canvas3D[2]=new myPoint();canvas3D[3]=new myPoint();
		drawEyeLoc = new myPoint(-1, -1, -1000);
		eyeInWorld = new myPoint();		
		scrCtrInWorld = new myPoint();									//
		mseLoc = new myPoint();
		eyeInWorld = new myPoint();
		oldMseLoc  = new myPoint();
		distMsePt = new myPoint();
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
		buildCanvas();
	}

	/**
	 * find points to define plane normal to camera eye, at set distance from camera, to use drawing canvas 	
	 */
	public void buildCanvas(){	
		float rawCtrDepth = p.getDepth(viewDimW2, viewDimH2);
		myPoint rawScrCtrInWorld = p.getWorldLoc(viewDimW2, viewDimH2,rawCtrDepth);		
		myVector A = new myVector(rawScrCtrInWorld,  p.getWorldLoc(viewDimW-10, 10,rawCtrDepth)),	
				B = new myVector(rawScrCtrInWorld,  p.getWorldLoc(viewDimW-10, viewDimH-10,rawCtrDepth));	//ctr to upper right, ctr to lower right		
		drawSNorm = myVector._cross(A,B)._normalize();
		//build plane using norm - have canvas go through canvas ctr in 3d
		myVector planeTan = myVector._cross(drawSNorm, myVector._normalize(new myVector(drawSNorm.x+10000,drawSNorm.y+10,drawSNorm.z+10)))._normalize();			//result of vector crossed with normal will be in plane described by normal
     	//myPoint lastPt = p.P(myPoint._add(p.P(), .707 * canvasDim, planeTan));
     	//myPoint lastPt = p.P(myPoint._add(new myPoint(), canvasDimOvSqrt2, planeTan));
     	myPoint lastPt = new myPoint(new myPoint(), canvasDimOvSqrt2, planeTan);
     	planeTan = myVector._rotAroundAxis(planeTan, drawSNorm, MyMathUtils.THREE_QTR_PI);
		for(int i =0;i<canvas3D.length;++i){		
			//build invisible canvas to draw upon
     		canvas3D[i].set(myPoint._add(lastPt, canvasDim, planeTan));
     		//planeTan = myVector._cross(planeTan, drawSNorm)._normalize();												//this effectively rotates around center point by 90 degrees -builds a square
     		planeTan = myVector._rotAroundAxis(planeTan, drawSNorm);
     		//p.show(canvas3D[i],5,"i="+i,p.V(10,10,10));
     		lastPt = canvas3D[i];
     	}

		//normal to canvas through eye moved far behind viewer
		eyeInWorld = p.getWorldLoc(viewDimW2, viewDimH2,-.00001f);
		//eyeInWorld =myPoint._add(rawScrCtrInWorld, myPoint._dist( p.pick(0,0,-1), rawScrCtrInWorld), drawSNorm);								//location of "eye" in world space
		eyeToCtr.set(eyeInWorld, rawScrCtrInWorld);
		scrCtrInWorld = getPlInterSect(rawScrCtrInWorld, myVector._normalize(eyeToCtr));
		
		float ctrDepth = p.getSceenZ((float)scrCtrInWorld.x, (float)scrCtrInWorld.y, (float)scrCtrInWorld.z);
		mseLoc = mseScr(ctrDepth);	
		eyeToMse.set(eyeInWorld, mseLoc);		//unit vector in world coords of "eye" to mouse location
		eyeToMse._normalize();
		oldMseLoc.set(dfCtr);
		dfCtr = getPlInterSect(mseLoc, eyeToMse);
		distMsePt = new myPoint(dfCtr,myVector._mult(drawSNorm, -1000));

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
	 * @param depth
	 * @return
	 */
	private myPoint mseScr(float depth) {
		int[] mse = p.getMouse_Raw_Int();
		return p.getWorldLoc(mse[0],mse[1],depth);
	} 
	
	public myPoint getMseLoc(){return new myPoint(dfCtr);	}
	public myPointf getMseLoc_f(){return new myPointf(dfCtr.x,dfCtr.y,dfCtr.z);	}
	public myPoint getOldMseLoc(){return new myPoint(oldMseLoc);	}	
	public myVector getMseDragVec(){return new myVector(oldMseLoc,dfCtr);}
	
	//relative to passed origin
	public myPoint getMseLoc(myPoint glbTrans){return myPoint._sub(dfCtr, glbTrans);	}
	//move by passed translation
	public myPointf getTransMseLoc(myPointf glbTrans){return myPointf._add(dfCtr, glbTrans);	}
	//dist from mouse to passed location
	public float getMseDist(myPointf glbTrans){return new myVectorf(dfCtr, glbTrans).magn;	}
	public myPoint getOldMseLoc(myPoint glbTrans){return myPoint._sub(oldMseLoc, glbTrans);	}
	
	//get normalized ray from eye loc to mouse loc
	public myVectorf getEyeToMouseRay_f() {
		myVectorf ray = new myVectorf(eyeInWorld, dfCtr);
		return ray._normalize();
	}
	
//	public float getDepth(int mX, int mY){
//		PGL pgl = p.beginPGL();
//		FloatBuffer depthBuffer = ByteBuffer.allocateDirect(1 << 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
//		int newMy = viewDimH - mY;		pgl.readPixels(mX, newMy - 1, 1, 1, PGL.DEPTH_COMPONENT, PGL.FLOAT, depthBuffer);
//		float depthValue = depthBuffer.get(0);
//		p.endPGL();
//		return depthValue;
//	}
	
//	public myPoint p.pick(int x, int y, float depth){
//		int newY = viewDimH - y;
//		float depthValue = depth;
//		
//		if(depth == -1){depthValue = p.getDepth( x,  y); }	
//		//p.outStr2Scr("cur depth in pick : " + depthValue);
//		//get 3d matrices
//		PGraphics3D p3d = (PGraphics3D)p.g;
//		PMatrix3D proj = p3d.projection.get(), modelView = p3d.modelview.get(), modelViewProjInv = proj; modelViewProjInv.apply( modelView ); modelViewProjInv.invert();	  
//		float[] viewport = {0, 0, viewDimW, viewDimH},
//				normalized = new float[] {
//						((x - viewport[0]) / viewport[2]) * 2.0f - 1.0f, 
//						((newY - viewport[1]) / viewport[3]) * 2.0f - 1.0f, 
//						depthValue * 2.0f - 1.0f, 
//						1.0f};	  
//		float[] unprojected = new float[4];	  
//		modelViewProjInv.mult( normalized, unprojected );
//		myPoint pickLoc = new myPoint( unprojected[0]/unprojected[3], unprojected[1]/unprojected[3], unprojected[2]/unprojected[3] );
//		//p.outStr2Scr("Depth Buffer val : "+String.format("%.4f",depthValue)+ " for mx,my : ("+mX+","+mY+") and world loc : " + pickLoc.toStrBrf());
//		return pickLoc;
//	}		
	

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
			Base_DispWindow.AppMgr.drawAxes(10000,1f, myPoint.ZEROPT, 100, true);//
			//draw center point
			p.showPtAsSphere(myPointf.ZEROPT,3.0f, 5, IRenderInterface.gui_Black, IRenderInterface.gui_Black);
			drawText(win, ""+dfCtr+ "|fr:"+p.getFrameRate(),4.0f, 15.0f, 4.0f);
			//p.scale(1.5f,1.5f,1.5f);
			//drawText(""+text_value_at_Cursor,4, -8, 4,0);getMseLoc(sceneCtrVals[sceneIDX])
			p.popMatState();			
	}//drawMseEdge		
}//class Disp3DCanvas


