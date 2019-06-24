package base_UI_Objects;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import base_Utils_Objects.*;
import base_Utils_Objects.vectorObjs.myEdge;
import base_Utils_Objects.vectorObjs.myPoint;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVector;
import base_Utils_Objects.vectorObjs.myVectorf;
import processing.core.*;
import processing.opengl.PGL;
import processing.opengl.PGraphics3D;

public class my3DCanvas {
	public my_procApplet p;
	
	public myPoint drawEyeLoc,													//rx,ry,dz coords where eye was when drawing - set when first drawing and return eye to this location whenever trying to draw again - rx,ry,dz
		scrCtrInWorld,mseLoc, eyeInWorld, oldMseLoc, distMsePt;//mseIn3DBox;
	private myPoint dfCtr;											//mouse location projected onto current drawing canvas

	private myEdge camEdge;												//denotes line perp to cam eye, to use for intersections for mouse selection
	private final float canvasDim = 15000,
			canvasDimOvSqrt2 = MyMathUtils.invSqrt2_f * canvasDim; 									//canvas dimension for "virtual" 3d		
	private myPoint[] canvas3D;											//3d plane, normal to camera eye, to be used for drawing - need to be in "view space" not in "world space", so that if camera moves they don't change
	private myVector eyeToMse,											//eye to 2d mouse location 
					eyeToCtr,													//vector from eye to center of cube, to be used to determine which panels of bounding box to show or hide
					eyeTodfCtr,
					drawSNorm;													//current normal of viewport/screen
		
	private int viewDimW2, viewDimH2;
	private final int viewDimW, viewDimH;
	private float curDepth;
	
	private int[] mseFillClr;
	

	
	public my3DCanvas(my_procApplet _p) {
		p = _p;
		viewDimW = p.width; viewDimH = p.height;
		curDepth = -1;		
		mseFillClr = new int[] {0,0,0,255};
		initCanvas();
	}
	
	private void initCanvas(){
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
		camEdge = new myEdge(p);	
		eyeToMse = new myVector();		
		eyeToCtr = new myVector();	
		eyeTodfCtr = new myVector();
		drawSNorm = new myVector();	
		buildCanvas();
	}

	//find points to define plane normal to camera eye, at set distance from camera, to use drawing canvas 	
	public void buildCanvas(){	
		float rawCtrDepth = getDepth(viewDimW2, viewDimH2);
		myPoint rawScrCtrInWorld = pick(viewDimW2, viewDimH2,rawCtrDepth);		
		myVector A = new myVector(rawScrCtrInWorld,  pick(viewDimW-10, 10,rawCtrDepth)),	B = new myVector(rawScrCtrInWorld,  pick(viewDimW-10, viewDimH-10,rawCtrDepth));	//ctr to upper right, ctr to lower right		
		drawSNorm = myVector._cross(A,B)._normalize();
		//build plane using norm - have canvas go through canvas ctr in 3d
		myVector planeTan = myVector._cross(drawSNorm, myVector._normalize(new myVector(drawSNorm.x+10000,drawSNorm.y+10,drawSNorm.z+10)))._normalize();			//result of vector crossed with normal will be in plane described by normal
     	//myPoint lastPt = p.P(myPoint._add(p.P(), .707 * canvasDim, planeTan));
     	//myPoint lastPt = p.P(myPoint._add(new myPoint(), canvasDimOvSqrt2, planeTan));
     	myPoint lastPt = new myPoint(new myPoint(), canvasDimOvSqrt2, planeTan);
     	planeTan = myVector._rotAroundAxis(planeTan, drawSNorm, MyMathUtils.threeQtrPI);
		for(int i =0;i<canvas3D.length;++i){		//build invisible canvas to draw upon
     		canvas3D[i].set(myPoint._add(lastPt, canvasDim, planeTan));
     		//planeTan = myVector._cross(planeTan, drawSNorm)._normalize();												//this effectively rotates around center point by 90 degrees -builds a square
     		planeTan = myVector._rotAroundAxis(planeTan, drawSNorm);
     		//p.show(canvas3D[i],5,"i="+i,p.V(10,10,10));
     		lastPt = canvas3D[i];
     	}

		//normal to canvas through eye moved far behind viewer
		eyeInWorld =pick(viewDimW2, viewDimH2,-.00001f);
		//eyeInWorld =myPoint._add(rawScrCtrInWorld, myPoint._dist( pick(0,0,-1), rawScrCtrInWorld), drawSNorm);								//location of "eye" in world space
		eyeToCtr.set(eyeInWorld, rawScrCtrInWorld);
		scrCtrInWorld = getPlInterSect(rawScrCtrInWorld, myVector._normalize(eyeToCtr));
		
		float ctrDepth = p.screenZ((float)scrCtrInWorld.x, (float)scrCtrInWorld.y, (float)scrCtrInWorld.z);
		mseLoc = MouseScr(ctrDepth);	
		eyeToMse.set(eyeInWorld, mseLoc);		//unit vector in world coords of "eye" to mouse location
		eyeToMse._normalize();
		oldMseLoc.set(dfCtr);
		dfCtr = getPlInterSect(mseLoc, eyeToMse);
		distMsePt = new myPoint(dfCtr,myVector._mult(drawSNorm, -1000));

	}//buildCanvas()
	
	public myVector getDrawSNorm() {return drawSNorm;}
	public int getViewDimW() {return viewDimW;}
	public int getViewDimH() {return viewDimH;}
	
	//return a unit vector from the screen location of the mouse pointer in the world to the reticle location in the world - for ray casting onto objects the mouse is over
	public myVector getMse2DtoMse3DinWorld(myPoint glbTrans){	
		myVector res = new myVector(pick(p.mouseX, p.mouseY,-.00001f),getMseLoc(glbTrans) );		
		return res._normalize();
	}
	//gets a unit vector that is up in the world relative to eye position and view direction
	public myVector getUScrUpInWorld(){			myVector res = new myVector(pick(viewDimW2, viewDimH2,-.00001f),pick(viewDimW2, 0,-.00001f));			return res._normalize();}	
	public myVector getUScrRightInWorld(){		myVector res = new myVector(pick(viewDimW2, viewDimH2,-.00001f),pick(viewDimW, viewDimH2,-.00001f));	return res._normalize();	}
	public myVectorf getUScrUpInWorldf(){		myVectorf res = new myVectorf(pick(viewDimW2, viewDimH2,-.00001f),pick(viewDimW2, 0,-.00001f));			return res._normalize();}	
	public myVectorf getUScrRightInWorldf(){	myVectorf res = new myVectorf(pick(viewDimW2, viewDimH2,-.00001f),pick(viewDimW, viewDimH2,-.00001f));	return res._normalize();}
	
	public void drawCanvas(){
		p.noLights();
		p.pushMatrix();p.pushStyle();
		p.beginShape(PConstants.QUAD);
		p.fill(255,255,255,80);
		//p.noStroke();
		p.gl_normal(eyeToMse);
     	//for(int i =0;i<canvas3D.length;++i){		//build invisible canvas to draw upon
        for(int i =canvas3D.length-1;i>=0;--i){		//build invisible canvas to draw upon
     		//p.line(canvas3D[i], canvas3D[(i+1)%canvas3D.length]);
     		p.gl_vertex(canvas3D[i]);
     	}
     	p.endShape(PConstants.CLOSE);
     	p.popStyle();p.popMatrix();
     	p.lights();
	}
	
	//find pt in drawing plane that corresponds with point and camera eye normal
	public myPoint getPlInterSect(myPoint pt, myVector unitT){
		myPoint dctr = new myPoint(0,0,0);	//actual click location on visible plane
		 // if ray from E along T intersects triangle (A,B,C), return true and set proposal to the intersection point
		p.intersectPl(pt, unitT, canvas3D[0],canvas3D[1],canvas3D[2],  dctr);//find point where mouse ray intersects canvas
		return dctr;		
	}//getPlInterSect	
	public myPoint getMseLoc(){return new myPoint(dfCtr);	}
	public myPoint getEyeLoc(){return pick(viewDimW2, viewDimH2,-.00001f);	}
	public myPoint getOldMseLoc(){return new myPoint(oldMseLoc);	}
	
	public myVector getMseDragVec(){return new myVector(oldMseLoc,dfCtr);}
	
	//relative to passed origin
	public myPoint getMseLoc(myPoint glbTrans){return myPoint._sub(dfCtr, glbTrans);	}
	//move by passed translation
	public myPointf getTransMseLoc(myPointf glbTrans){return myPointf._add(dfCtr, glbTrans);	}
	//dist from mouse to passed location
	public float getMseDist(myPointf glbTrans){return new myVectorf(dfCtr, glbTrans).magn;	}
	//public myPoint getEyeLoc(myPoint glbTrans){return myPoint._sub(getEyeLoc(), glbTrans);	}
	public myPoint getOldMseLoc(myPoint glbTrans){return myPoint._sub(oldMseLoc, glbTrans);	}
	
	public float getDepth(int mX, int mY){
		PGL pgl = p.beginPGL();
		FloatBuffer depthBuffer = ByteBuffer.allocateDirect(1 << 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
		int newMy = viewDimH - mY;		pgl.readPixels(mX, newMy - 1, 1, 1, PGL.DEPTH_COMPONENT, PGL.FLOAT, depthBuffer);
		float depthValue = depthBuffer.get(0);
		p.endPGL();
		return depthValue;
	}
	
	public myPoint pick(int mX, int mY, float depth){
		int newMy = viewDimH - mY;
		float depthValue = depth;
		
		if(depth == -1){depthValue = getDepth( mX,  mY); }	
		//p.outStr2Scr("cur depth in pick : " + depthValue);
		//get 3d matrices
		PGraphics3D p3d = (PGraphics3D)p.g;
		PMatrix3D proj = p3d.projection.get(), modelView = p3d.modelview.get(), modelViewProjInv = proj; modelViewProjInv.apply( modelView ); modelViewProjInv.invert();	  
		float[] viewport = {0, 0, viewDimW, viewDimH},
				normalized = new float[] {
						((mX - viewport[0]) / viewport[2]) * 2.0f - 1.0f, 
						((newMy - viewport[1]) / viewport[3]) * 2.0f - 1.0f, 
						depthValue * 2.0f - 1.0f, 
						1.0f};	  
		float[] unprojected = new float[4];	  
		modelViewProjInv.mult( normalized, unprojected );
		myPoint pickLoc = new myPoint( unprojected[0]/unprojected[3], unprojected[1]/unprojected[3], unprojected[2]/unprojected[3] );
		//p.outStr2Scr("Depth Buffer val : "+String.format("%.4f",depthValue)+ " for mx,my : ("+mX+","+mY+") and world loc : " + pickLoc.toStrBrf());
		return pickLoc;
	}		
	//hold depth when clicked
	public myPoint MouseScr(float depth) {return pick(p.mouseX,p.mouseY,depth);} 	

	private final void drawText(String str, float x, float y, float z){
		p.pushMatrix();	p.pushStyle();
			p.fill(mseFillClr[0],mseFillClr[1],mseFillClr[2],mseFillClr[3]);
			p.unSetCamOrient();
			p.translate(x,y,z);
			p.text(str,0,0,0);		
		p.popStyle();	p.popMatrix();	
	}//drawText	
	

	public void drawMseEdge(){//draw mouse sphere and edge normal to cam eye through mouse sphere 
		p.pushMatrix();	p.pushStyle();
			p.strokeWeight(1f);
			p.setStroke(new int[] {255, 0,255}, 255);
			p.stroke(255,0,255,255);
			//c.camEdge.set(1000, c.eyeToMse, c.dfCtr);		//build edge through mouse point normal to camera eye	
			camEdge.set(eyeInWorld, dfCtr);		//build edge through mouse point and eye location in world	
			camEdge.drawMe();
			p.translate((float)dfCtr.x, (float)dfCtr.y, (float)dfCtr.z);
			//project mouse point on bounding box walls
			if(((p.curFocusWin == -1) || (p.curDispWinIs3D()))){p.drawProjOnBox(dfCtr);}
			p.drawAxes(10000,1f, myPoint.ZEROPT, 100, true);//
			//draw intercept with box
			myPointf.ZEROPT.showMeSphere(p,3.0f);
			drawText(""+dfCtr+ "|fr:"+p.frameRate,4.0f, 15.0f, 4.0f);
			p.scale(1.5f,1.5f,1.5f);
			//drawText(""+text_value_at_Cursor,4, -8, 4,0);getMseLoc(sceneCtrVals[sceneIDX])
		p.popStyle();		p.popMatrix();		
	}//drawMseEdge		
}
