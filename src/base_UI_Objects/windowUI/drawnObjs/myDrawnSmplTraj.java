package base_UI_Objects.windowUI.drawnObjs;

import base_JavaProjTools_IRender.base_Render_Interface.IRenderInterface;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_UI_Objects.GUI_AppManager;
import base_UI_Objects.windowUI.base.myDispWindow;
import base_UI_Objects.windowUI.drawnObjs.base.myDrawnObject;


//class holds trajectory and 4 macro cntl points, and handling for them
public class myDrawnSmplTraj {
	//public IRenderInterface pa;
	public static GUI_AppManager AppMgr;
	public myDispWindow win;
	public myTrajManager trajMgr;
	public static int trjCnt = 0;
	public int ID;

	public myDrawnObject drawnTraj;						//a drawable curve - per staff
	public static float topOffY;
	public int drawnTrajPickedIdx;						//idx of mouse-chosen point	in editable drawn trajectory		
	public static final int drawnTrajEditWidth = 10;			//width in cntl points of the amount of the drawn trajectory deformed by dragging
	public static final float trajDragScaleAmt = 100.0f;					//amt of displacement when dragging drawn trajectory to edit
	public static float msClkPtRad = 10,msClkPt3DRad = 20,	//radius within which a mouse click will register on a point
			spTmplOffset, 
			mseSens = 100.0f,
			minTmplOff, maxTmplOff;		//offset between end points in edit draw region, min and max 
	public static float sqMsClkRad = msClkPtRad*msClkPtRad;

	public myPoint[] edtCrvEndPts;					//end points for edit curve in editable region	- modify these guys	to move curve - pts 2 and 3 are perp control axes						
	public int editEndPt;							//if an endpoint is being edited(moved around) : -1 == no, 0 = pt 0, 1 = pt 1, 2 = pt 2, 3 = pt 3 ;
		
	//public myPoint[] pathBetweenPts;				//Array that stores all the path Points, once they are scaled
	
	public int[] fillClrCnst, strkClrCnst;
		
	public boolean[] trajFlags;
	public static final int 
				flatPtIDX 		= 0,						//whether this should draw plat circles or spheres for its points
				smCntlPtsIDX 	= 1,						//whether the 4 cntl points should be as small as regular points or larger
				ownrWinIs3dIDX 	= 2;					
	public static final int numTrajFlags = 3;
	
	public int ctlRad;
	
	public myDrawnSmplTraj(myDispWindow _win, GUI_AppManager _AppMgr, myTrajManager _trajMgr, float _topOffy, int[] _fillClrCnst, int[] _strkClrCnst, boolean _flat, boolean _smCntl){
		win = _win;AppMgr = _AppMgr; trajMgr=_trajMgr;
		fillClrCnst = _fillClrCnst; 
		strkClrCnst = _strkClrCnst;
		
		ID = trjCnt++;
		setTopOffy(_topOffy);			//offset in y from top of screen
		initTrajFlags();
		initTrajStuff();		
		trajFlags[flatPtIDX] = _flat;
		trajFlags[smCntlPtsIDX] = _smCntl;
		trajFlags[ownrWinIs3dIDX] = win.getIs3DWindow();
		ctlRad = (trajFlags[smCntlPtsIDX] ? myDrawnObject.trajPtRad : 5 );
	}
	protected void initTrajFlags(){trajFlags = new boolean[numTrajFlags];for(int i=0;i<numTrajFlags;++i){trajFlags[i]=false;}}
	public void initTrajStuff(){
		spTmplOffset = 400;						//vary this based on scale of drawn arc, to bring the end points together and keep arcs within gray region
		minTmplOff = 10; 		
		maxTmplOff = 400;		
		drawnTrajPickedIdx = -1;
		editEndPt = -1;
		
		edtCrvEndPts = new myPoint[4];
		//pathBetweenPts = new myPoint[0];
		edtCrvEndPts[0] = new myPoint(win.rectDim[0] + .25 * win.rectDim[2], .5 * (2*win.rectDim[1] + win.rectDim[3]),0);
		edtCrvEndPts[1] = new myPoint(win.rectDim[0] + .75 * win.rectDim[2], .5 * (2*win.rectDim[1] + win.rectDim[3]),0);		
	}

	public void calcPerpPoints(){
		//myVector dir = pa.U(myVector._rotAroundAxis(new myVector(edtCrvEndPts[0],edtCrvEndPts[1]), pa.c.getDrawSNorm()));
		myVector dir = (myVector._rotAroundAxis(new myVector(edtCrvEndPts[0],edtCrvEndPts[1]), AppMgr.getDrawSNorm()))._normalize();
		float mult =  .125f,
		dist = mult * (float)myPoint._dist(edtCrvEndPts[0],edtCrvEndPts[1]);
		myPoint avgPt = edtCrvEndPts[0]._avgWithMe(edtCrvEndPts[1]);
		edtCrvEndPts[2] = new myPoint(avgPt, dist,dir);
		edtCrvEndPts[3] = new myPoint(avgPt,-dist,dir);
	}
	//scale edit points and cntl points
	public void reCalcCntlPoints(float scale){
		for(int i = 0; i<edtCrvEndPts.length; ++i){	edtCrvEndPts[i].y = trajMgr.calcOffsetScale(edtCrvEndPts[i].y,scale,topOffY);edtCrvEndPts[i].z = 0; }//edtCrvEndPts[curDrnTrajScrIDX][i].y -= topOffY; edtCrvEndPts[curDrnTrajScrIDX][i].y *= scale;edtCrvEndPts[curDrnTrajScrIDX][i].y += topOffY;	}	
		if(drawnTraj != null){
			((myVariStroke)drawnTraj).scaleMeY(false,scale,topOffY);//only for display - rescaling changes the notes slightly so don't recalc notes
		}
	}//reCalcCntlPoints/
	
	public void startBuildTraj(){
		edtCrvEndPts[2] = null;
		edtCrvEndPts[3] = null;
		calcPerpPoints();
		drawnTraj = new myVariStroke( myDispWindow.AppMgr, new myVector(AppMgr.getDrawSNorm()),fillClrCnst, strkClrCnst);
		drawnTraj.startDrawing();
	}
	public boolean startEditEndPoint(int idx){
		editEndPt = idx; trajMgr.setFlags(myTrajManager.editingTraj, true);
		//pa.outStr2Scr("Handle TrajClick 2 startEditEndPoint : " + name + " | Move endpoint : "+editEndPt);
		return true;
	}
	//check if initiating an edit on an existing object, if so then set up edit
	public boolean startEditObj(myPoint mse){
		boolean doEdit = false; 
		float chkDist = trajFlags[ownrWinIs3dIDX] ? msClkPt3DRad : msClkPtRad;
		//first check endpoints, then check curve points
		double[] distTocPts = new double[1];			//using array as pointer, passing by reference
		int cntpIdx = trajMgr.findClosestPt(mse, distTocPts, edtCrvEndPts);	
		if(distTocPts[0] < chkDist){						startEditEndPoint(cntpIdx);} 
		else {
			double[] distToPts = new double[1];			//using array as pointer, passing by reference
			myPoint[] pts = ((myVariStroke)drawnTraj).getDrawnPtAra(false);
			int pIdx = trajMgr.findClosestPt(mse, distToPts, pts);
			//pa.outStr2Scr("Handle TrajClick 2 startEditObj : " + name);
			if(distToPts[0] < chkDist){//close enough to mod
				trajMgr.setEditCueCircle(0,mse);
				trajMgr.setFlags(myTrajManager.editingTraj, true);
				doEdit = true;
				//pa.outStr2Scr("Handle TrajClick 3 startEditObj modPt : " + name + " : pIdx : "+ pIdx);
				drawnTrajPickedIdx = pIdx;	
				editEndPt = -1;
			} else if (distToPts[0] < sqMsClkRad){//not close enough to mod but close to curve
				trajMgr.setEditCueCircle(1,mse);
				trajMgr.setFlags(myTrajManager.smoothTraj, true);
			}
		}
		return doEdit;
	}//startEditObj//rectDim trajFlags[ownrWinIs3dIDX]
	
	//returns true if within eps of any of the points of this trajector
	@SuppressWarnings("unused")
	public boolean clickedMe(myPoint mse){
		float chkDist = trajFlags[ownrWinIs3dIDX] ? msClkPt3DRad : msClkPtRad;	
		double[] distToPts = new double[1];			//using array as pointer, passing by reference
		int cntpIdx = trajMgr.findClosestPt(mse, distToPts, edtCrvEndPts);	
		if(distToPts[0] < chkDist){return true;}
		distToPts[0] = 9999;
		int pIdx = trajMgr.findClosestPt(mse, distToPts, ((myVariStroke)drawnTraj).getDrawnPtAra(false));
		return (distToPts[0] < chkDist);
	}
	
	private boolean checkEndPoint(myPoint pt){return ((pt.x >= win.rectDim[0]) && (pt.x <= win.rectDim[0]+win.rectDim[2]) && (pt.y >= win.rectDim[1]) && (pt.y <=  win.rectDim[1]+win.rectDim[3]));}
	private void modTrajCntlPts(myVector diff){
		if((editEndPt == 0) || (editEndPt == 1)){
			myPoint newLoc = myPoint._add(edtCrvEndPts[editEndPt], diff);
			if(checkEndPoint(newLoc)){edtCrvEndPts[editEndPt].set(newLoc);}
			//edtCrvEndPts[editEndPt]._add(diff);		
			calcPerpPoints();
			drawnTraj.remakeDrawnTraj(false);
			rebuildDrawnTraj();	
		} else {//scale all traj points based on modification of pts 2 or 3 - only allow them to move along the perp axis			
			//myVector abRotAxis = pa.U(myVector._rotAroundAxis(new myVector(edtCrvEndPts[0],edtCrvEndPts[1]), pa.c.getDrawSNorm()));
			myVector abRotAxis = myVector._rotAroundAxis(new myVector(edtCrvEndPts[0],edtCrvEndPts[1]), AppMgr.getDrawSNorm())._normalize();
			float dist = (float)myPoint._dist(edtCrvEndPts[2], edtCrvEndPts[3]);
			double modAmt = diff._dot(abRotAxis);
			myVector a1 = myVector._mult(abRotAxis, modAmt), a2 = myVector._mult(abRotAxis, -modAmt);
			if(editEndPt == 2){	edtCrvEndPts[2]._add(a1);edtCrvEndPts[3]._add(a2);} 
			else {				edtCrvEndPts[2]._add(a2);edtCrvEndPts[3]._add(a1);}
			float dist2 = (float)myPoint._dist(edtCrvEndPts[2], edtCrvEndPts[3]);
			//pa.outStr2Scr("modTrajCntlPts : editEndPt : " + editEndPt + " : diff : "+ diff+ " dist : " + dist+ " dist2 :" + dist2 + " rot tangent axis : " + abRotAxis + " | Scale : " + (1+dist2)/(1+dist) );
			((myVariStroke)drawnTraj).scalePointsAboveAxis(edtCrvEndPts[0],edtCrvEndPts[1], abRotAxis, (1+dist2)/(1+dist));
			//pa.outStr2Scr("");
		}			
	}
	
	//edit the trajectory used for UI input in this window
	public boolean editTraj(int mouseX, int mouseY,int pmouseX, int pmouseY, myPoint mouseClickIn3D, myVector mseDragInWorld){
		boolean mod = false;
		if((drawnTrajPickedIdx == -1) && (editEndPt == -1) && (!trajMgr.getFlags(myTrajManager.smoothTraj))){return mod;}			//neither endpoints nor drawn points have been edited, and we're not smoothing
		myVector diff = trajFlags[ownrWinIs3dIDX] ? mseDragInWorld : new myVector(mouseX-pmouseX, mouseY-pmouseY,0);		
		//pa.outStr2Scr("Diff in editTraj for  " + name + "  : " +diff.toStrBrf());
		//needs to be before templateZoneY check
		if (editEndPt != -1){//modify scale of ornament here, or modify drawn trajectory	
			modTrajCntlPts(diff);
			mod = true;
		} else if (drawnTrajPickedIdx != -1){	//picked trajectory element to edit other than end points	
			diff._div(mseSens);
			((myVariStroke)drawnTraj).handleMouseDrag(diff,drawnTrajPickedIdx);
			mod = true;
		} 
		return mod;
	}//editTraj
	
	public void endEditObj(){
		if((drawnTrajPickedIdx != -1) || (editEndPt != -1)
			|| ( trajMgr.getFlags(myTrajManager.smoothTraj))){//editing curve
			drawnTraj.remakeDrawnTraj(false);
			rebuildDrawnTraj();		
		}
//		else if( win.getFlags(myDispWindow.smoothTraj)){		
//			drawnTraj.remakeDrawnTraj(false);	
//			rebuildDrawnTraj();	
//		}
		//pa.outStr2Scr("In Traj : " + this.ID + " endEditObj ");
		trajMgr.processTrajectory(this);//dispFlags[trajDirty, true);
		drawnTrajPickedIdx = -1;
		editEndPt = -1;
		trajMgr.setFlags(myTrajManager.editingTraj, false);
		trajMgr.setFlags(myTrajManager.smoothTraj, false);
	}
	
	public void endDrawObj(myPoint endPoint){
		drawnTraj.addPt(endPoint);
		//pa.outStr2Scr("Size of drawn traj : " + drawnTraj.cntlPts.length);
		if(drawnTraj.cntlPts.length >= 2){
			drawnTraj.finalizeDrawing(true);
			myPoint[] pts = ((myVariStroke)drawnTraj).getDrawnPtAra(false);
			//pa.outStr2Scr("Size of pts ara after finalize (use drawn vels : " +false + " ): " + pts.length);
			edtCrvEndPts[0] = new myPoint(pts[0]);
			edtCrvEndPts[1] = new myPoint(pts[pts.length-1]);
			rebuildDrawnTraj();
			//pa.outStr2Scr("In Traj : " + this.ID + " endDrawObj ");
			trajMgr.processTrajectory(this);
		} else {
			drawnTraj = new myVariStroke(myDispWindow.AppMgr, new myVector(AppMgr.getDrawSNorm()),fillClrCnst, strkClrCnst);
		}
		trajMgr.setFlags(myTrajManager.drawingTraj, false);
	}//endDrawObj
	
	public void addPoint(myPoint mse){
		drawnTraj.addPt(mse);
	}
	//print out all points in this trajectory
	public void dbgPrintAllPoints(){
		if(drawnTraj == null){return;}
		((myVariStroke) drawnTraj).dbgPrintAllPoints(false);
	}
	
	//use animTimeMod to animate/decay showing this traj TODO 
	public void drawMe(IRenderInterface pa, float animTimeMod){
		if(drawnTraj != null){
			pa.setFill(fillClrCnst,255);
			pa.setStroke(strkClrCnst,255);
			for(int i =0; i< edtCrvEndPts.length; ++i){
				trajMgr.showKeyPt(pa, edtCrvEndPts[i],""+ (i+1),ctlRad);
			}	
			((myVariStroke)drawnTraj).drawMe(pa, false,trajFlags[flatPtIDX]);
		} 
	}
	@SuppressWarnings("unused")
	public void rebuildDrawnTraj(){
		//Once edge is drawn
		calcPerpPoints();
		if(drawnTraj != null){
			if(drawnTraj.flags[drawnTraj.isMade]){				  
				//Initialize the array that stores the path
				int a= 0, b= 1;
				boolean flipTraj = myDispWindow.AppMgr.doFlipTraj();
				if(flipTraj){	 a = 1; b= 0;}
				//TODO
				if(false){//pathBetweenPts =
						((myVariStroke)drawnTraj).moveVelCurveToEndPoints(edtCrvEndPts[a], edtCrvEndPts[b],flipTraj); }
				else{//pathBetweenPts =
						((myVariStroke)drawnTraj).moveCntlCurveToEndPoints(edtCrvEndPts[a], edtCrvEndPts[b], flipTraj);  	}
			}
		}	
	}//rebuildDrawnTraj	
	public void setTopOffy(float _topOffy){	topOffY = _topOffy;	}		//offset in y from top of screen
}//class myDrawnNoteTraj
