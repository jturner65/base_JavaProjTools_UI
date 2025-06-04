package base_UI_Objects.windowUI.drawnTrajectories;

import java.util.ArrayList;
import java.util.TreeMap;

import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Math_Objects.vectorObjs.floats.myPointf;
import base_Render_Interface.IRenderInterface;
import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_Utils_Objects.io.messaging.MessageObject;


/**
 * this class will manage the trajectories in a single Base_DispWindow, if the window supports drawn trajectories
 * @author john
 *
 */
public class TrajectoryManager {
	//owning window
	public Base_DispWindow ownr;

	/**
	 * msg object for output to console or log
	 */
	protected MessageObject msgObj;

	public int[] 
			trajFillClrCnst = new int[] {0,120,120,255},		//trajectory default colors - can override
			trajStrkClrCnst = new int[] {0,255,255,255};
	
	public DrawnSimpleTraj tmpDrawnTraj;						//currently drawn curve and all handling code - send to instanced owning screen

	/**
	 * all trajectories in this particular display window - String key is unique identifier for what component trajectory is connected to
	 */
	public TreeMap<Integer,TreeMap<String,ArrayList<DrawnSimpleTraj>>> drwnTrajMap;				
	
	//edit circle quantities for visual cues when grab and smoothen trajectories
	public static final int[] editCrcFillClrs = new int[] {IRenderInterface.gui_FaintMagenta, IRenderInterface.gui_FaintGreen};			
	public static final float[] editCrcRads = new float[] {20.0f,40.0f};			
	public static final float[] editCrcMods = new float[] {1f,2f};			
	public final myPoint[] editCrcCtrs = new myPoint[] {new myPoint(0,0,0),new myPoint(0,0,0)};			
	public float[] editCrcCurRads = new float[] {0,0};	

	//ara to hold uniquely-identifying strings for each trajectory-receiving component
	public String[] trajNameAra;		
	
	public int numSubScrInWin = 2;									//# of subscreens/components in owning window that will support traj drawing
	public int numTrajPerScr = 10;
	public int[] numTrajInSubScr;									//# of trajectories available for each sub screen
	public static final int 
				traj1IDX = 0,
				traj2IDX = 1;
	
	public int curDrnTrajScrIDX;									//currently used/shown drawn trajectory - 1st idx (which screen)
	public int curTrajAraIDX;										//currently used/shown drawn trajectory - 2nd idx (which staff trajectory applies to)

	public int[][] drawTrajBoxFillClrs;
	
	private int[] trajFlags;	
	private static final int 
		debugIDX			= 0,
		canDrawTraj 		= 1,			//whether or not owning window will accept a drawn trajectory
		drawingTraj 		= 2,			//whether a trajectory is being drawn in owning window - all windows handle trajectory input, has different functions in each window
		editingTraj 		= 3,			//whether a trajectory is being edited in owning window
		showTrajEditCrc 	= 4,			//set this when some editing mechanism has taken place - draw a circle of appropriate diameter at mouse and shrink it quickly, to act as visual cue
		smoothTraj 			= 5,			//trajectory has been clicked nearby, time to smooth
		trajDecays 			= 6,			//drawn trajectories eventually/immediately disappear
		trajPointsAreFlat 	= 7;			//trajectory drawn points are flat (for pick, to prevent weird casting collisions	
	
	private static final int numTrajFlags = 8;


	public TrajectoryManager(Base_DispWindow _ownr, boolean _trajIsFlat) {
		ownr=_ownr; msgObj = ownr.getMsgObj();
		initFlags();
		setFlags(canDrawTraj, true);
		setFlags(trajPointsAreFlat, _trajIsFlat);
		initTmpTrajStuff();
	}
	
	/**
	 * build a new trajectory
	 * @param _trajIsFlat
	 * @return
	 */
	protected DrawnSimpleTraj buildTraj(boolean _trajIsFlat) {
		return new DrawnSimpleTraj(ownr, Base_DispWindow.AppMgr, this, trajFillClrCnst, trajStrkClrCnst, _trajIsFlat, !_trajIsFlat);		
	}
	
	/////////////////////
	// traj manager init
	/**
	 * set up an initial, temporary trajectory
	 * @param _trajIsFlat
	 */	
	private void initTmpTrajStuff(){
		tmpDrawnTraj = buildTraj(getFlags(trajPointsAreFlat));
		curDrnTrajScrIDX = 0;
		curTrajAraIDX = 0;		
	}//	

	
	/**
	 * (re)initialize traj-specific stuff for this window
	 */
	public void initTrajStructs(){
		drwnTrajMap = new TreeMap<Integer,TreeMap<String,ArrayList<DrawnSimpleTraj>>>();
		TreeMap<String,ArrayList<DrawnSimpleTraj>> tmpTrajMap;
		for(int scr =0;scr<numSubScrInWin; ++scr){
			tmpTrajMap = new TreeMap<String,ArrayList<DrawnSimpleTraj>>();
			for(int traj =0; traj<numTrajInSubScr[scr]; ++traj){
				tmpTrajMap.put(getTrajAraKeyStr(traj), new ArrayList<DrawnSimpleTraj>());			
			}	
			drwnTrajMap.put(scr, tmpTrajMap);
		}		
	}	
	
	/**
	 * set up initial trajectories - 2d array, 1 per UI Page, 1 per modifiable construct within page.
	 */
	public void initDrwnTrajs() {initDrwnTrajs(numSubScrInWin,numTrajPerScr);}
	public void initDrwnTrajs(int _numSubScrInWin, int _numTrajPerScr){
		numSubScrInWin = _numSubScrInWin;						//# of subscreens in a window.  will generally be 1, but with sequencer will have at least 2 (piano roll and score view)
		numTrajPerScr = _numTrajPerScr;
		trajNameAra = new String[_numTrajPerScr];
		numTrajInSubScr = new int[]{_numTrajPerScr,_numTrajPerScr};	
		for(int i =0; i<_numTrajPerScr; ++i){		trajNameAra[i] = "traj_"+(i+1);	}
		initTrajStructs();		
	}//initDrwnTrajs
	
	/**
	 * this will set the height of the rectangle enclosing this window - this will be called when a 
	 * window pushes up or pulls down this window - this resizes any drawn trajectories in this 
	 * window, and calls the instance class's code for resizing
	 * @param height
	 */
	public void setTrajRectDimsY(float height, float scale){
		if((null!=drwnTrajMap) && (0 < drwnTrajMap.size())){
			//resize drawn all trajectories
			TreeMap<String,ArrayList<DrawnSimpleTraj>> tmpTreeMap = drwnTrajMap.get(this.curDrnTrajScrIDX);
			if((tmpTreeMap != null) && (tmpTreeMap.size() != 0)) {
				for(int i =0; i<tmpTreeMap.size(); ++i){
					ArrayList<DrawnSimpleTraj> tmpAra = tmpTreeMap.get(getTrajAraKeyStr(i));			
					if(null!=tmpAra){	for(int j =0; j<tmpAra.size();++j){		tmpAra.get(j).reCalcCntlPoints(scale);	}	}
				}	
			}
		}
	}//setTrajRectDimsY
	

	/////////////////////
	// traj construction
	
	//drawn trajectory stuff	
	public void startBuildDrawObj(){
		Base_DispWindow.AppMgr.setIsDrawing(true);
		//drawnTrajAra[curDrnTrajScrIDX][curDrnTrajStaffIDX].startBuildTraj();
		tmpDrawnTraj = buildTraj(getFlags(trajPointsAreFlat));
		tmpDrawnTraj.startBuildTraj();
		setFlags(drawingTraj, true);
	}

	/**
	 * initialize circle so that it will draw at location of edit
	 * @param idx which circle to draw
	 * @param mse location to draw circle
	 */
	public void setEditCueCircle(int idx,myPoint mse){
		setFlags(showTrajEditCrc, true);
		editCrcCtrs[idx].set(mse.x, mse.y, 0);
		editCrcCurRads[idx] = editCrcRads[idx];
	}
	
	public void handleMouseRelease_Traj(myPoint msePt) {
		if (getFlags(editingTraj)){    this.tmpDrawnTraj.endEditObj();}    //this process assigns tmpDrawnTraj to owning window's traj array
		if (getFlags(drawingTraj)){	this.tmpDrawnTraj.endDrawObj(msePt);}	//drawing curve	
	}
	
	public boolean handleMouseClick_Traj(boolean keysToDrawClicked, myPoint mse){
		if((!getFlags(canDrawTraj)) || (null==mse)){return false;}
		boolean mod = false;
		if(keysToDrawClicked){					//drawing curve with click+alt - drawing on canvas
			//msgObj.dispInfoMessage("TrajectoryManager","handleTrajClick","Current trajectory key IDX " + curTrajAraIDX);
			startBuildDrawObj();	
			mod = true;
			//
		} else {
		//	msgObj.dispInfoMessage("TrajectoryManager","handleTrajClick","Current trajectory key IDX edit " + curTrajAraIDX);
			this.tmpDrawnTraj = findTraj(mse);							//find closest trajectory to the mouse's click location
			
			if ((null != this.tmpDrawnTraj)  && (null != this.tmpDrawnTraj.drawnTraj)) {					//alt key not pressed means we're possibly editing a curve, if it exists and if we click within "sight" of it, or moving endpoints
				//msgObj.dispInfoMessage("TrajectoryManager","handleTrajClick","Current trajectory ID " + tmpDrawnTraj.ID);
				mod = this.tmpDrawnTraj.startEditObj(mse);
			}
		}
		return mod;
	}//
	
	public boolean handleMouseDrag_Traj(int mouseX, int mouseY, int pmouseX, int pmouseY, myVector mseDragInWorld, int mseBtn) {
		boolean mod = false;
		if(getFlags(drawingTraj)){ 		//if drawing trajectory has started, then process it
			//msgObj.dispInfoMessage("TrajectoryManager","handleMouseDrag","drawing traj");
			myPoint pt =  ownr.getMsePoint(mouseX, mouseY);
			if(null==pt){return false;}
			this.tmpDrawnTraj.addPoint(pt);
			mod = true;
		}else if(getFlags(editingTraj)){		//if editing trajectory has started, then process it
			//msgObj.dispInfoMessage("TrajectoryManager","handleMouseDrag","edit traj");	
			myPoint pt =  ownr.getMsePoint(mouseX, mouseY);
			if(null==pt){return false;}
			mod = this.tmpDrawnTraj.editTraj(mouseX, mouseY,pmouseX, pmouseY,pt,mseDragInWorld);
		}
		return mod;
	}//handleMouseDrag_Traj

	public DrawnSimpleTraj findTraj(myPoint mse){
		TreeMap<String,ArrayList<DrawnSimpleTraj>> tmpTreeMap = drwnTrajMap.get(this.curDrnTrajScrIDX);
		if((tmpTreeMap != null) && (tmpTreeMap.size() != 0)) {
			for(int i =0; i<tmpTreeMap.size(); ++i){
				ArrayList<DrawnSimpleTraj> tmpAra = tmpTreeMap.get(getTrajAraKeyStr(i));			
				if(null!=tmpAra){	for(int j =0; j<tmpAra.size();++j){	if(tmpAra.get(j).clickedMe(mse)){return tmpAra.get(j);}}}
			}	
		}
		return null;		
	}
	
	/**
	 * for adding/deleting a screen programatically (loading a song) TODO
	 * add or delete a new map of treemaps (if trajAraKey == "" or null), or a new map of traj arrays to existing key map
	 * @param scrKey
	 * @param trajAraKey
	 * @param del
	 */
	public void modTrajStructs(int scrKey, String trajAraKey, boolean del){
		int modMthd = -1;
		if(del){//delete a screen's worth of traj arrays, or a single traj array from a screen 
			if((trajAraKey == null) || (trajAraKey == "") ){		//delete screen map				
				TreeMap<String,ArrayList<DrawnSimpleTraj>> tmpTrajMap = drwnTrajMap.remove(scrKey);
				if(null != tmpTrajMap){			msgObj.dispInfoMessage("TrajectoryManager","modTrajStructs","Screen trajectory map removed for scr : " + scrKey);				modMthd = 0;}
				else {							msgObj.dispErrorMessage("TrajectoryManager","modTrajStructs","Error : Screen trajectory map not found for scr : " + scrKey); 	modMthd = -1; }
			} else {												//delete a submap within a screen
				modMthd = 2;					//modifying existing map at this location
				TreeMap<String,ArrayList<DrawnSimpleTraj>> tmpTrajMap = drwnTrajMap.get(scrKey);
				if(null == tmpTrajMap){			msgObj.dispErrorMessage("TrajectoryManager","modTrajStructs","Error : Screen trajectory map not found for scr : " + scrKey + " when trying to remove arraylist : "+trajAraKey); modMthd = -1;}
				else { 
					ArrayList<DrawnSimpleTraj> tmpTrajAra = drwnTrajMap.get(scrKey).remove(trajAraKey);modMthd = 2;
					if(null == tmpTrajAra){		msgObj.dispErrorMessage("TrajectoryManager","modTrajStructs","Error : attempting to remove a trajectory array from a screen but trajAra not found. scr : " + scrKey + " | trajAraKey : "+trajAraKey);modMthd = -1; }
				}
			}			 
		} else {													//add
			TreeMap<String,ArrayList<DrawnSimpleTraj>> tmpTrajMap = drwnTrajMap.get(scrKey);
			if((trajAraKey == null) || (trajAraKey == "") ){		//add map of maps - added a new screen				
				if(null != tmpTrajMap){msgObj.dispErrorMessage("TrajectoryManager","modTrajStructs","Error : attempting to add a new drwnTrajMap where one exists. scr : " + scrKey);modMthd = -1; }
				else {tmpTrajMap = new TreeMap<String,ArrayList<DrawnSimpleTraj>>();	drwnTrajMap.put(scrKey, tmpTrajMap);modMthd = 1;}
			} else {												//add new map of trajs to existing screen's map
				ArrayList<DrawnSimpleTraj> tmpTrajAra = drwnTrajMap.get(scrKey).get(trajAraKey);	
				if(null == tmpTrajMap){msgObj.dispErrorMessage("TrajectoryManager","modTrajStructs","Error : attempting to add a new trajectory array to a screen that doesn't exist. scr : " + scrKey + " | trajAraKey : "+trajAraKey); modMthd = -1; }
				else if(null != tmpTrajAra){msgObj.dispErrorMessage("TrajectoryManager","modTrajStructs","Error : attempting to add a new trajectory array to a screen where one already exists. scr : " + scrKey + " | trajAraKey : "+trajAraKey);modMthd = -1; }
				else {	tmpTrajAra = new ArrayList<DrawnSimpleTraj>();			tmpTrajMap.put(trajAraKey, tmpTrajAra);	drwnTrajMap.put(scrKey, tmpTrajMap);modMthd = 2;}
			}			
		}//if del else add
		//rebuild arrays of start loc
		rebuildTrnsprtAras(scrKey, modMthd);
	}
	
	/**
	 * add trajectory to appropriately keyed current trajectory ara in treemap	
	 * @param drawnTraj
	 */
	public void processTrajectory(DrawnSimpleTraj drawnTraj){
		TreeMap<String,ArrayList<DrawnSimpleTraj>> tmpTreeMap = drwnTrajMap.get(this.curDrnTrajScrIDX);
		ArrayList<DrawnSimpleTraj> tmpAra;
		if(curTrajAraIDX != -1){		//make sure some trajectory has been selected
			if(tmpTreeMap == null) {tmpTreeMap = new TreeMap<String,ArrayList<DrawnSimpleTraj>>();} 
			tmpAra = new ArrayList<DrawnSimpleTraj>();
			tmpAra.add(drawnTraj);
//			if((tmpTreeMap != null) && (tmpTreeMap.size() != 0) ) {
//				//tmpAra = tmpTreeMap.get(getTrajAraKeyStr(curTrajAraIDX));	
//				//for this application always wish to clear traj
//				tmpAra = new ArrayList<DrawnSimpleTraj>();
//				tmpAra.add(drawnTraj); 				
//			} else {//empty or null tmpTreeMap - tmpAra doesn't exist
//				tmpAra = new ArrayList<DrawnSimpleTraj>();
//				tmpAra.add(drawnTraj);
//				if(tmpTreeMap == null) {tmpTreeMap = new TreeMap<String,ArrayList<DrawnSimpleTraj>>();} 
//			}
			tmpTreeMap.put(getTrajAraKeyStr(curTrajAraIDX), tmpAra);
			ownr.processTraj_Indiv(drawnTraj);
		}	
		//individual traj processing
	}

	public void rebuildAllDrawnTrajs(){
		for(TreeMap<String,ArrayList<DrawnSimpleTraj>> tmpTreeMap : drwnTrajMap.values()){
			if((tmpTreeMap != null) && (tmpTreeMap.size() != 0)) {
				for(int i =0; i<tmpTreeMap.size(); ++i){
					ArrayList<DrawnSimpleTraj> tmpAra = tmpTreeMap.get(getTrajAraKeyStr(i));			
					if(null!=tmpAra){	for(int j =0; j<tmpAra.size();++j){	tmpAra.get(j).rebuildDrawnTraj();}}
				}
			}	
		}			
	}//rebuildAllDrawnTrajs

	/**
	 * for adding/deleting a screen programatically  TODO
	 * rebuild arrays of start locs whenever trajectory maps/arrays 
	 * have changed - passed key is value modded in drwnTrajMap,
	 * modVal is if this is a deleted screen's map(0), a new map (new screen) 
	 * at this location (1), or a modified map (added or deleted trajectory) (2)
	 * may want to move back to Base_DispWindow if we ever use this
	 * @param modScrKey
	 * @param modVal
	 */
	@SuppressWarnings("unused")
	protected void rebuildTrnsprtAras(int modScrKey, int modVal){
		if(modVal == -1){return;}//denotes no mod taken place
		int tmpNumSubScrInWin = drwnTrajMap.size();
		int [] tmpNumTrajInSubScr = new int[tmpNumSubScrInWin];
		float [] tmpVsblStLoc = new float[tmpNumSubScrInWin];
		int [] tmpSeqVisStTime = new int[tmpNumSubScrInWin];
		if(modVal == 0){			//deleted a screen's map
			if(tmpNumSubScrInWin != (numSubScrInWin -1)){msgObj.dispErrorMessage("TrajectoryManager","rbldTrnsprtAras","Error in rbldTrnsprtAras : screen traj map not removed at idx : " + modScrKey); return;}
			for(int i =0; i< numSubScrInWin; ++i){					
			}			
			
		} else if (modVal == 1){	//added a new screen, with a new map			
		} else if (modVal == 2){	//modified an existing map (new or removed traj ara)			
		}
		numSubScrInWin = tmpNumSubScrInWin;
		numTrajInSubScr = tmpNumTrajInSubScr;
	}//rbldTrnsprtAras
	
	/**
	 * Scale a value and add an offset
	 * @param val
	 * @param sc
	 * @param off
	 * @return
	 */
	public float calcOffsetScale_f(double val, float sc, float off){float res =(float)val - off; res *=sc; return res+=off;}
	/**
	 * 
	 * @param val
	 * @param sc
	 * @param off
	 * @return
	 */
	public double calcOffsetScale(double val, float sc, double off){double res = val - off; res *=sc; return res+=off;}
	//finds closest point to p in sPts - put dist in d
	public final int findClosestPt(myPoint p, double[] d, myPoint[] _pts){
		int res = -1;
		double mindist = 99999999, _d;
		for(int i=0; i<_pts.length; ++i){_d = myPoint._dist(p,_pts[i]);if(_d < mindist){mindist = _d; d[0]=_d;res = i;}}	
		return res;
	}

	public final int findClosestPt(myPointf p, double[] d, myPointf[] _pts){
		int res = -1;
		double mindist = 99999999, _d;
		for(int i=0; i<_pts.length; ++i){_d = myPointf._dist(p,_pts[i]);if(_d < mindist){mindist = _d; d[0]=_d;res = i;}}	
		return res;
	}

	public void clearAllTrajectories(){//int instrIdx){
		TreeMap<String,ArrayList<DrawnSimpleTraj>> tmpTreeMap = drwnTrajMap.get(this.curDrnTrajScrIDX);
		//ArrayList<myDrawnSmplTraj> tmpAra;
		if(curTrajAraIDX != -1){		//make sure some trajectory/staff has been selected
			if((tmpTreeMap != null) && (tmpTreeMap.size() != 0) ) {
				tmpTreeMap.put(getTrajAraKeyStr(curTrajAraIDX), new ArrayList<DrawnSimpleTraj>());
			}
		}	
	}//clearAllTrajectories
	
	public void clearTmpDrawnTraj() {this.tmpDrawnTraj = null;}
	
	
	////////////////////////
	// draw/display routines
	/**
	 * Draw text for notifications regarding process of drawing or editing trajectory
	 * 
	 */
	public void drawNotifications(IRenderInterface ri, float xLoc, float yLoc){		
		if(!getFlags(canDrawTraj)) {return;}
		//debug stuff
		ri.pushMatState();	
		float[] winRectDim = ownr.getRectDims();
		ri.translate(winRectDim[0]+20,winRectDim[1]+winRectDim[3]-70, 0);
		Base_DispWindow.AppMgr.dispMenuTxtLat("Drawing trajectory curve", ri.getClr((getFlags(drawingTraj) ? IRenderInterface.gui_Green : IRenderInterface.gui_Red),255), true, xLoc, yLoc);
		Base_DispWindow.AppMgr.dispMenuTxtLat("Editing trajectory curve", ri.getClr((getFlags(editingTraj) ? IRenderInterface.gui_Green : IRenderInterface.gui_Red),255), true, xLoc, yLoc);
		ri.popMatState();		
	}//drawNotifications

	
	/**
	 * draw a circle corresponding to a click location
	 */
	public void drawClkCircle(IRenderInterface ri){
		ri.pushMatState();	
		boolean doneDrawing = true;
		ri.noStroke();
		for(int i =0; i<editCrcFillClrs.length;++i){
			if(editCrcCurRads[i] <= 0){continue;}
			ri.showPtAsCircle(editCrcCtrs[i], editCrcCurRads[i], editCrcFillClrs[i], -2);	
			editCrcCurRads[i] -= editCrcMods[i];
			doneDrawing = false;
		}
		if(doneDrawing){setFlags(showTrajEditCrc, false);}
		ri.popMatState();		
	}
	
	/**
	 * draw a trajectory
	 */	
	public void drawTraj_2d(IRenderInterface ri){
		if(!getFlags(canDrawTraj)) {return;}
		ri.pushMatState();	
		if(null != tmpDrawnTraj){tmpDrawnTraj.drawMe(ri);}
		TreeMap<String,ArrayList<DrawnSimpleTraj>> tmpTreeMap = drwnTrajMap.get(this.curDrnTrajScrIDX);
		if((tmpTreeMap != null) && (tmpTreeMap.size() != 0)) {
			for(int i =0; i<tmpTreeMap.size(); ++i){
				ArrayList<DrawnSimpleTraj> tmpAra = tmpTreeMap.get(getTrajAraKeyStr(i));			
				if(null!=tmpAra){	for(int j =0; j<tmpAra.size();++j){tmpAra.get(j).drawMe(ri);}}
			}	
		}
		ri.popMatState();	
		if(getFlags(showTrajEditCrc)){drawClkCircle(ri);}
	}//drawTraj
	
	/**
	 * instancing window class will manage 3d drawing TODO perhaps modify this?
	 * @param animTimeMod
	 * @param trans
	 */
	public void drawTraj_3d(IRenderInterface ri, float modAmtMillis, myPointf trans){
		if(!getFlags(canDrawTraj)) {return;}
		ri.pushMatState();	
		ownr.drawTraj3D(modAmtMillis,trans);
		ri.popMatState();	
		if(getFlags(showTrajEditCrc)){drawClkCircle(ri);}
	}//drawTraj
	
	/**
	 * displays point with a name (used for key points in trajectory)
	 * @param a
	 * @param s
	 * @param rad
	 */
	public void showKeyPt(IRenderInterface ri,myPoint a, String s, float rad){		
		ri.showPtWithText(a,rad, s, new myVector(10,-5,0), IRenderInterface.gui_Cyan, getFlags(trajPointsAreFlat));	
	}	

	//release shift/control/alt keys
	public void endShiftKey(myPoint msePt){	}
	public void endAltKey(myPoint msePt){
		if(getFlags(drawingTraj)){this.tmpDrawnTraj.endDrawObj(msePt);}	
		this.tmpDrawnTraj = null;
	}	
	public void endCntlKey(myPoint msePt){}	
	public void endValueKeyPress() {}
	
	
	/////////////////////
	// getters/setters
	
	/**
	 * set colors of the trajectory for this window
	 * @param _tfc
	 * @param _tsc
	 */
	public void setTrajColors(int[] _tfc, int[] _tsc){trajFillClrCnst = _tfc;trajStrkClrCnst = _tsc;initTmpTrajStuff();}
	/**
	 * get key used to access arrays in traj array
	 * @param i
	 * @return
	 */
	public String getTrajAraKeyStr(int i){return trajNameAra[i];}
	/**
	 * get index of traj name
	 * @param str
	 * @return
	 */
	public int getTrajAraIDXVal(String str){for(int i=0; i<trajNameAra.length;++i){if(trajNameAra[i].equals(str)){return i;}}return -1; }

	/////////////////////
	// state flag handling
	
	public boolean getIsSmoothing() {return getFlags(smoothTraj);}
	public void setShouldSmooth(boolean _doSmooth) {setFlags(smoothTraj, _doSmooth);}
	public void setShouldDraw(boolean _doDraw) {setFlags(drawingTraj, _doDraw);}
	public void setShouldEdit(boolean _doEdit) {setFlags(editingTraj, _doEdit);}
	
	public void initFlags(){trajFlags = new int[1 + numTrajFlags/32];for(int i =0; i<numTrajFlags;++i){setFlags(i,false);}}		
	public boolean getFlags(int idx){int bitLoc = 1<<(idx%32);return (trajFlags[idx/32] & bitLoc) == bitLoc;}	
	/**
	 * set baseclass flags  //setFlags(showIDX, 
	 * @param idx
	 * @param val
	 */
	public void setFlags(int idx, boolean val){
		int flIDX = idx/32, mask = 1<<(idx%32);
		trajFlags[flIDX] = (val ?  trajFlags[flIDX] | mask : trajFlags[flIDX] & ~mask);
		switch(idx){
			case debugIDX 			: {	break;}	
			case canDrawTraj 		: {	break;}	
			case drawingTraj 		: {	break;}	
			case editingTraj 		: {	break;}	
			case showTrajEditCrc 	: {	break;}	
			case smoothTraj 		: {	break;}		
			case trajDecays 		: {	break;}		
			case trajPointsAreFlat 	: {	break;}	
		}				
	}//setFlags
	
	//TODO
	@Override
	public String toString() {
		return "";
	}

}//class myTrajManager
