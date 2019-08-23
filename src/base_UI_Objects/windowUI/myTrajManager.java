package base_UI_Objects.windowUI;

import java.util.ArrayList;
import java.util.TreeMap;

import base_UI_Objects.my_procApplet;
import base_UI_Objects.drawnObjs.myDrawnSmplTraj;
import base_Utils_Objects.io.MessageObject;
import base_Utils_Objects.vectorObjs.myPoint;
import base_Utils_Objects.vectorObjs.myPointf;
import base_Utils_Objects.vectorObjs.myVector;

/**
 * this class will manage the trajectories in a single myDispWindow, if the window supports drawn trajectories
 * @author john
 *
 */
public class myTrajManager {
	public static my_procApplet pa;
	//owning window
	public myDispWindow ownr;
	/**
	 * msg object for output to console or log
	 */
	protected MessageObject msgObj;
	//owning window focus target, scene fcs(current and default) and the center of the scene
	protected myVector focusTar;							//target of focus - used in translate to set where the camera is looking - allow for modification
	protected myVector sceneFcsVal;							//set this value  to be default target of focus	- don't programmatically change, keep to use as reset
	protected myPoint sceneCtrVal;							//set this value to be different display center translations -to be used to calculate mouse offset in world for pick


	public int[] 
			trajFillClrCnst = new int[] {0,120,120,255},		//trajectory default colors - can override
			trajStrkClrCnst = new int[] {0,255,255,255};
	
	public myDrawnSmplTraj tmpDrawnTraj;						//currently drawn curve and all handling code - send to instanced owning screen

	/**
	 * all trajectories in this particular display window - String key is unique identifier for what component trajectory is connected to
	 */
	public TreeMap<Integer,TreeMap<String,ArrayList<myDrawnSmplTraj>>> drwnTrajMap;				
	
	//edit circle quantities for visual cues when grab and smoothen trajectories
	public static final int[] editCrcFillClrs = new int[] {my_procApplet.gui_FaintMagenta, my_procApplet.gui_FaintGreen};			
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
	public static final int 
		debugIDX			= 0,
		canDrawTraj 		= 1,			//whether or not owning window will accept a drawn trajectory
		drawingTraj 		= 2,			//whether a trajectory is being drawn in owning window - all windows handle trajectory input, has different functions in each window
		editingTraj 		= 3,			//whether a trajectory is being edited in owning window
		showTrajEditCrc 	= 4,			//set this when some editing mechanism has taken place - draw a circle of appropriate diameter at mouse and shrink it quickly, to act as visual cue
		smoothTraj 			= 5,			//trajectory has been clicked nearby, time to smooth
		trajDecays 			= 6,			//drawn trajectories eventually/immediately disappear
		trajPointsAreFlat 	= 7;			//trajectory drawn points are flat (for pick, to prevent weird casting collisions	
	
	private static final int numTrajFlags = 8;


	public myTrajManager(my_procApplet _pa, myDispWindow _ownr, boolean _canDrawTraj, boolean _trajIsFlat) {
		pa=_pa; ownr=_ownr; msgObj = ownr.msgObj;
		initFlags();
		setFlags(canDrawTraj, _canDrawTraj);
		setFlags(trajPointsAreFlat, _trajIsFlat);
		initTmpTrajStuff();
	}
	
	/**
	 * build a new trajectory
	 * @param _trajIsFlat
	 * @return
	 */
	protected myDrawnSmplTraj buildTraj(boolean _trajIsFlat) {
		return new myDrawnSmplTraj(pa,ownr, this, myDispWindow.topOffY,trajFillClrCnst, trajStrkClrCnst, _trajIsFlat, !_trajIsFlat);		
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
		drwnTrajMap = new TreeMap<Integer,TreeMap<String,ArrayList<myDrawnSmplTraj>>>();
		TreeMap<String,ArrayList<myDrawnSmplTraj>> tmpTrajMap;
		for(int scr =0;scr<numSubScrInWin; ++scr){
			tmpTrajMap = new TreeMap<String,ArrayList<myDrawnSmplTraj>>();
			for(int traj =0; traj<numTrajInSubScr[scr]; ++traj){
				tmpTrajMap.put(getTrajAraKeyStr(traj), new ArrayList<myDrawnSmplTraj>());			
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
			TreeMap<String,ArrayList<myDrawnSmplTraj>> tmpTreeMap = drwnTrajMap.get(this.curDrnTrajScrIDX);
			if((tmpTreeMap != null) && (tmpTreeMap.size() != 0)) {
				for(int i =0; i<tmpTreeMap.size(); ++i){
					ArrayList<myDrawnSmplTraj> tmpAra = tmpTreeMap.get(getTrajAraKeyStr(i));			
					if(null!=tmpAra){	for(int j =0; j<tmpAra.size();++j){		tmpAra.get(j).reCalcCntlPoints(scale);	}	}
				}	
			}
		}
	}//setRectDimsY
	
	/**
	 * final initialization stuff, after window made, but necessary to make sure window displays correctly - don't copy, use raw so matches owning window values
	 * @param _ctr
	 * @param _baseFcs
	 */
	public void finalTrajValsInit(myPoint _ctr, myVector _baseFcs) {
		sceneFcsVal = new myVector(_baseFcs);		//this will be a copy since this is the permanent, return-to-start fcs val
		sceneCtrVal = _ctr;
		focusTar = _baseFcs;		
	}
	
	/////////////////////
	// traj construction
	
	//drawn trajectory stuff	
	public void startBuildDrawObj(){
		pa.setIsDrawing(true);
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
	
	public void handleMouseRelease_Traj() {
		if (getFlags(editingTraj)){    this.tmpDrawnTraj.endEditObj();}    //this process assigns tmpDrawnTraj to owning window's traj array
		if (getFlags(drawingTraj)){	this.tmpDrawnTraj.endDrawObj(ownr.getMsePoint(pa.Mouse()));}	//drawing curve	
	}
	
	protected boolean handleMouseClick_Traj(boolean keysToDrawClicked, myPoint mse){
		if((!getFlags(canDrawTraj)) || (null==mse)){return false;}
		boolean mod = false;
		if(keysToDrawClicked){					//drawing curve with click+alt - drawing on canvas
			//msgObj.dispInfoMessage("myDispWindow","handleTrajClick","Current trajectory key IDX " + curTrajAraIDX);
			startBuildDrawObj();	
			mod = true;
			//
		} else {
		//	msgObj.dispInfoMessage("myDispWindow","handleTrajClick","Current trajectory key IDX edit " + curTrajAraIDX);
			this.tmpDrawnTraj = findTraj(mse);							//find closest trajectory to the mouse's click location
			
			if ((null != this.tmpDrawnTraj)  && (null != this.tmpDrawnTraj.drawnTraj)) {					//alt key not pressed means we're possibly editing a curve, if it exists and if we click within "sight" of it, or moving endpoints
				//msgObj.dispInfoMessage("myDispWindow","handleTrajClick","Current trajectory ID " + tmpDrawnTraj.ID);
				mod = this.tmpDrawnTraj.startEditObj(mse);
			}
		}
		return mod;
	}//
	
	protected boolean handleMouseDrag_Traj(int mouseX, int mouseY, int pmouseX, int pmouseY, myVector mseDragInWorld, int mseBtn) {
		boolean mod = false;
		if(getFlags(drawingTraj)){ 		//if drawing trajectory has started, then process it
			//msgObj.dispInfoMessage("myDispWindow","handleMouseDrag","drawing traj");
			myPoint pt =  ownr.getMsePoint(mouseX, mouseY);
			if(null==pt){return false;}
			this.tmpDrawnTraj.addPoint(pt);
			mod = true;
		}else if(getFlags(editingTraj)){		//if editing trajectory has started, then process it
			//msgObj.dispInfoMessage("myDispWindow","handleMouseDrag","edit traj");	
			myPoint pt =  ownr.getMsePoint(mouseX, mouseY);
			if(null==pt){return false;}
			mod = this.tmpDrawnTraj.editTraj(mouseX, mouseY,pmouseX, pmouseY,pt,mseDragInWorld);
		}
		return mod;
	}//handleMouseDrag_Traj

	public myDrawnSmplTraj findTraj(myPoint mse){
		TreeMap<String,ArrayList<myDrawnSmplTraj>> tmpTreeMap = drwnTrajMap.get(this.curDrnTrajScrIDX);
		if((tmpTreeMap != null) && (tmpTreeMap.size() != 0)) {
			for(int i =0; i<tmpTreeMap.size(); ++i){
				ArrayList<myDrawnSmplTraj> tmpAra = tmpTreeMap.get(getTrajAraKeyStr(i));			
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
	protected void modTrajStructs(int scrKey, String trajAraKey, boolean del){
		int modMthd = -1;
		if(del){//delete a screen's worth of traj arrays, or a single traj array from a screen 
			if((trajAraKey == null) || (trajAraKey == "") ){		//delete screen map				
				TreeMap<String,ArrayList<myDrawnSmplTraj>> tmpTrajMap = drwnTrajMap.remove(scrKey);
				if(null != tmpTrajMap){			msgObj.dispInfoMessage("myDispWindow","modTrajStructs","Screen trajectory map removed for scr : " + scrKey);				modMthd = 0;}
				else {							msgObj.dispErrorMessage("myDispWindow","modTrajStructs","Error : Screen trajectory map not found for scr : " + scrKey); 	modMthd = -1; }
			} else {												//delete a submap within a screen
				modMthd = 2;					//modifying existing map at this location
				TreeMap<String,ArrayList<myDrawnSmplTraj>> tmpTrajMap = drwnTrajMap.get(scrKey);
				if(null == tmpTrajMap){			msgObj.dispErrorMessage("myDispWindow","modTrajStructs","Error : Screen trajectory map not found for scr : " + scrKey + " when trying to remove arraylist : "+trajAraKey); modMthd = -1;}
				else { 
					ArrayList<myDrawnSmplTraj> tmpTrajAra = drwnTrajMap.get(scrKey).remove(trajAraKey);modMthd = 2;
					if(null == tmpTrajAra){		msgObj.dispErrorMessage("myDispWindow","modTrajStructs","Error : attempting to remove a trajectory array from a screen but trajAra not found. scr : " + scrKey + " | trajAraKey : "+trajAraKey);modMthd = -1; }
				}
			}			 
		} else {													//add
			TreeMap<String,ArrayList<myDrawnSmplTraj>> tmpTrajMap = drwnTrajMap.get(scrKey);
			if((trajAraKey == null) || (trajAraKey == "") ){		//add map of maps - added a new screen				
				if(null != tmpTrajMap){msgObj.dispErrorMessage("myDispWindow","modTrajStructs","Error : attempting to add a new drwnTrajMap where one exists. scr : " + scrKey);modMthd = -1; }
				else {tmpTrajMap = new TreeMap<String,ArrayList<myDrawnSmplTraj>>();	drwnTrajMap.put(scrKey, tmpTrajMap);modMthd = 1;}
			} else {												//add new map of trajs to existing screen's map
				ArrayList<myDrawnSmplTraj> tmpTrajAra = drwnTrajMap.get(scrKey).get(trajAraKey);	
				if(null == tmpTrajMap){msgObj.dispErrorMessage("myDispWindow","modTrajStructs","Error : attempting to add a new trajectory array to a screen that doesn't exist. scr : " + scrKey + " | trajAraKey : "+trajAraKey); modMthd = -1; }
				else if(null != tmpTrajAra){msgObj.dispErrorMessage("myDispWindow","modTrajStructs","Error : attempting to add a new trajectory array to a screen where one already exists. scr : " + scrKey + " | trajAraKey : "+trajAraKey);modMthd = -1; }
				else {	tmpTrajAra = new ArrayList<myDrawnSmplTraj>();			tmpTrajMap.put(trajAraKey, tmpTrajAra);	drwnTrajMap.put(scrKey, tmpTrajMap);modMthd = 2;}
			}			
		}//if del else add
		//rebuild arrays of start loc
		rbldTrnsprtAras(scrKey, modMthd);
	}
	
	//add trajectory to appropriately keyed current trajectory ara in treemap	
	public void processTrajectory(myDrawnSmplTraj drawnTraj){
		TreeMap<String,ArrayList<myDrawnSmplTraj>> tmpTreeMap = drwnTrajMap.get(this.curDrnTrajScrIDX);
		ArrayList<myDrawnSmplTraj> tmpAra;
		if(curTrajAraIDX != -1){		//make sure some trajectory has been selected
			if((tmpTreeMap != null) && (tmpTreeMap.size() != 0) ) {
				tmpAra = tmpTreeMap.get(getTrajAraKeyStr(curTrajAraIDX));	
				//for this application always wish to clear traj
					tmpAra = new ArrayList<myDrawnSmplTraj>();
				//}
				//lastTrajIDX = tmpAra.size();
				tmpAra.add(drawnTraj); 				
			} else {//empty or null tmpTreeMap - tmpAra doesn't exist
				tmpAra = new ArrayList<myDrawnSmplTraj>();
				tmpAra.add(drawnTraj);
				//lastTrajIDX = tmpAra.size();
				if(tmpTreeMap == null) {tmpTreeMap = new TreeMap<String,ArrayList<myDrawnSmplTraj>>();} 
			}
			tmpTreeMap.put(getTrajAraKeyStr(curTrajAraIDX), tmpAra);
			ownr.processTrajIndiv(drawnTraj);
		}	
		//individual traj processing
	}

	public void rebuildAllDrawnTrajs(){
		for(TreeMap<String,ArrayList<myDrawnSmplTraj>> tmpTreeMap : drwnTrajMap.values()){
			if((tmpTreeMap != null) && (tmpTreeMap.size() != 0)) {
				for(int i =0; i<tmpTreeMap.size(); ++i){
					ArrayList<myDrawnSmplTraj> tmpAra = tmpTreeMap.get(getTrajAraKeyStr(i));			
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
	 * may want to move back to myDispWindow if we ever use this
	 * @param modScrKey
	 * @param modVal
	 */
	@SuppressWarnings("unused")
	protected void rbldTrnsprtAras(int modScrKey, int modVal){
		if(modVal == -1){return;}//denotes no mod taken place
		int tmpNumSubScrInWin = drwnTrajMap.size();
		int [] tmpNumTrajInSubScr = new int[tmpNumSubScrInWin];
		float [] tmpVsblStLoc = new float[tmpNumSubScrInWin];
		int [] tmpSeqVisStTime = new int[tmpNumSubScrInWin];
		if(modVal == 0){			//deleted a screen's map
			if(tmpNumSubScrInWin != (numSubScrInWin -1)){msgObj.dispErrorMessage("myDispWindow","rbldTrnsprtAras","Error in rbldTrnsprtAras : screen traj map not removed at idx : " + modScrKey); return;}
			for(int i =0; i< numSubScrInWin; ++i){					
			}			
			
		} else if (modVal == 1){	//added a new screen, with a new map			
		} else if (modVal == 2){	//modified an existing map (new or removed traj ara)			
		}
		numSubScrInWin = tmpNumSubScrInWin;
		numTrajInSubScr = tmpNumTrajInSubScr;
	}//rbldTrnsprtAras
	
	public float calcOffsetScale(double val, float sc, float off){float res =(float)val - off; res *=sc; return res+=off;}
	public double calcDBLOffsetScale(double val, float sc, double off){double res = val - off; res *=sc; return res+=off;}
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
		TreeMap<String,ArrayList<myDrawnSmplTraj>> tmpTreeMap = drwnTrajMap.get(this.curDrnTrajScrIDX);
		//ArrayList<myDrawnSmplTraj> tmpAra;
		if(curTrajAraIDX != -1){		//make sure some trajectory/staff has been selected
			if((tmpTreeMap != null) && (tmpTreeMap.size() != 0) ) {
				tmpTreeMap.put(getTrajAraKeyStr(curTrajAraIDX), new ArrayList<myDrawnSmplTraj>());
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
	protected void drawNotifications(){		
		if(!getFlags(canDrawTraj)) {return;}
		//debug stuff
		pa.pushMatrix();				pa.pushStyle();
		pa.translate(ownr.rectDim[0]+20,ownr.rectDim[1]+ownr.rectDim[3]-70);
		ownr.dispMenuTxtLat("Drawing trajectory curve", pa.getClr((getFlags(drawingTraj) ? my_procApplet.gui_Green : my_procApplet.gui_Red),255), true);
		//pa.show(new myPoint(0,0,0),4, "Drawing curve",new myVector(10,15,0),(getFlags(this.drawingTraj) ? pa.gui_Green : pa.gui_Red));
		//pa.translate(0,-30);
		ownr.dispMenuTxtLat("Editing trajectory curve", pa.getClr((getFlags(editingTraj) ? my_procApplet.gui_Green : my_procApplet.gui_Red),255), true);
		//pa.show(new myPoint(0,0,0),4, "Editing curve",new myVector(10,15,0),(getFlags(this.editingTraj) ? pa.gui_Green : pa.gui_Red));
		pa.popStyle();pa.popMatrix();		
	}//drawNotifications

	
	/**
	 * draw a circle corresponding to a click location
	 */
	public void drawClkCircle(){
		pa.pushMatrix();				pa.pushStyle();
		boolean doneDrawing = true;
		for(int i =0; i<editCrcFillClrs.length;++i){
			if(editCrcCurRads[i] <= 0){continue;}
			pa.setColorValFill(editCrcFillClrs[i],255);
			pa.noStroke();
			pa.circle(editCrcCtrs[i],editCrcCurRads[i]);
			editCrcCurRads[i] -= editCrcMods[i];
			doneDrawing = false;
		}
		if(doneDrawing){setFlags(showTrajEditCrc, false);}
		pa.popStyle();pa.popMatrix();		
	}
	
	/**
	 * draw a trajectory
	 * @param animTimeMod
	 */	
	public void drawTraj_2d(float animTimeMod){
		if(!getFlags(canDrawTraj)) {return;}
		pa.pushMatrix();pa.pushStyle();	
		if(null != tmpDrawnTraj){tmpDrawnTraj.drawMe(animTimeMod);}
		TreeMap<String,ArrayList<myDrawnSmplTraj>> tmpTreeMap = drwnTrajMap.get(this.curDrnTrajScrIDX);
		if((tmpTreeMap != null) && (tmpTreeMap.size() != 0)) {
			for(int i =0; i<tmpTreeMap.size(); ++i){
				ArrayList<myDrawnSmplTraj> tmpAra = tmpTreeMap.get(getTrajAraKeyStr(i));			
				if(null!=tmpAra){	for(int j =0; j<tmpAra.size();++j){tmpAra.get(j).drawMe(animTimeMod);}}
			}	
		}
		pa.popStyle();pa.popMatrix();	
		if(getFlags(showTrajEditCrc)){drawClkCircle();}
	}//drawTraj
	
	/**
	 * instancing window class will manage 3d drawing TODO perhaps modify this?
	 * @param animTimeMod
	 * @param trans
	 */
	public void drawTraj_3d(float animTimeMod, myPoint trans){
		if(!getFlags(canDrawTraj)) {return;}
		pa.pushMatrix();pa.pushStyle();	
		ownr.drawTraj3D(animTimeMod,trans);
		pa.popStyle();pa.popMatrix();	
		if(getFlags(showTrajEditCrc)){drawClkCircle();}
	}//drawTraj
	
	/**
	 * displays point with a name (used for key points in trajectory)
	 * @param a
	 * @param s
	 * @param rad
	 */
	public void showKeyPt(myPoint a, String s, float rad){	pa.show(a,rad, s, new myVector(10,-5,0), my_procApplet.gui_Cyan, getFlags(trajPointsAreFlat));	}	

	//release shift/control/alt keys
	public void endShiftKey(){	}
	public void endAltKey(){
		if(getFlags(drawingTraj)){this.tmpDrawnTraj.endDrawObj(ownr.getMsePoint(pa.Mouse()));}	
		this.tmpDrawnTraj = null;
	}	
	public void endCntlKey(){}	
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
	protected String getTrajAraKeyStr(int i){return trajNameAra[i];}
	/**
	 * get index of traj name
	 * @param str
	 * @return
	 */
	protected int getTrajAraIDXVal(String str){for(int i=0; i<trajNameAra.length;++i){if(trajNameAra[i].equals(str)){return i;}}return -1; }

	/////////////////////
	// state flag handling
	
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
