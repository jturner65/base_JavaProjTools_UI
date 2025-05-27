package base_UI_Objects.windowUI.drawnTrajectories.base;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import base_Render_Interface.IRenderInterface;
import base_Math_Objects.MyMathUtils;
import base_Math_Objects.vectorObjs.doubles.myCntlPt;
import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_UI_Objects.windowUI.drawnTrajectories.offsets.Normal_Offset;
import base_UI_Objects.windowUI.drawnTrajectories.offsets.base.Base_Offset;

public abstract class Base_DrawnTrajectory {
	public Base_DispWindow win;
	public final double trajDragScaleAmt = 100.0;					//amt of displacement when dragging drawn trajectory to edit
	public final int drawnTrajEditWidth = 10; //TODO make ui component			//width in cntl points of the amount of the drawn trajectory deformed by dragging

	public final int numIntCntlPts = 200, numCntlPts = 6;			//# of control points to use to draw line

	
	public int offsetType;						//1:Q-bspline w/normal offset, 2:Q-bspline w/ball offset, 3:Q-bspline w/radial offset
	private Base_Offset _offset;					//offset used by this stroke to calculate poly loop				

	
	public static double wScale = -1;
	
	public int[] fillClr, strkClr;
	public double len;								//length of object
	protected static final int numReps = 4;				//default number of repetitions of subdivid/tuck/untuck
	
	public static final int trajPtRad = 2;			//radius of points
	
	/**
	 * whether or not the points making up the loop of this stroke have been derived yet
	 */
	private boolean ptsDerived;	
	
	public myVector canvasNorm;							//normal to drawing canvas == normal to plane of poly
	protected myPoint[] origpts;							//originally drawn points making up this curve
	protected myPoint[] pts;								//points making up this curve
	protected double[] dpts;						//array holding distance to each point from beginning
	
	protected myCntlPt[] cntlPts;						//control points describing object, if used		
	protected double[] d_cntlPts;
	protected double cntl_len;
	
	public myPoint COV,									//center of verts, mass
			COM;
	
	/**
	 * Boolean state-managing flag structure
	 */
	public DrawnTrajFlags trajFlags;
	
	/**
	 * flags about type of operation that uses interpolation being done
	 */
	public int lnI_Typ,								//what interpolation type will this curve use for line operations (tuck, find myPoint at ratio of length, etc) 
				sbI_Typ,							//interp type for subdivision
				swI_Typ;							//what kind of interpolation will be used for this curve as it is swemyPoint around axis (if this is a closed sweep-poly)
	
	/**
	 * flags about which interpolation type should be done
	 */
	public final int linear_int = 0,				//denotes linear interpolation 
					ball_int = 1;					//ball-morph interpolation, for sweep morph
	//add more here when we have more 
	
	public final int 	//point processing flags
		_subdivide		=0,
		_tuck			=1,
		_equaldist		=2,
		_resample		=3;

	/**
	 * array of normals, tans, binorms for every control point,
	 */
	protected myVector[] c_nAra, c_tAra, c_bAra;
	
	
	protected double[][] distFromIntAxis;			//keeps the distance to internal axis of stroke			
	protected static final int 
	//idxs in distFromAxis array
			_d = 0,									//index for dist from axis
			_t = 1,									//interpolant along rot axis for proj
			_a = 2,									//idx1 for myPoint in ara for rotational axis - proj lies between these two
			_b = 3;									//idx2 for myPoint in ara for rotational axis
	protected final int nAxisVals = 4;
	
	public Base_DrawnTrajectory(Base_DispWindow _win, myVector _canvNorm) {
		win =_win;
		if(wScale == -1) {			wScale = Base_DispWindow.ri.getFrameRate()/5.0f;		}
		canvasNorm = _canvNorm;		//c.drawSNorm  draw surface normal
		//intialize underlying flags structure
		trajFlags = new DrawnTrajFlags(this);
		lnI_Typ = linear_int;
		swI_Typ = linear_int;
		sbI_Typ = linear_int;
		c_nAra = new myVector[0];c_tAra = new myVector[0];c_bAra = new myVector[0];
		COM = new myPoint();COV = new myPoint();
	    cntlPts = new myCntlPt[0];
	    trajFlags.setInitTrajFlags();
		ptsDerived = false;
		_offset = new Normal_Offset();
	}
	
	/**
	 * Debug mode functionality. Called only from flags structure
	 * @param val whether or not we have started debug mode
	 */
	protected final void handleTrajFlagsDebugMode(boolean val) {
		//TODO	
	}		

	
	/**
	 * initialize point referencing structs - using both for speed concerns.
	 */
	public void startDrawing(){	pts = new myPoint[0];len = 0; dpts = new double[0]; trajFlags.setDrawCntlRad(false);}	
	/**
	 * 
	 * @param p
	 */
	public void addPt(myPoint p){
		ArrayList<myPoint> tmp = new ArrayList<myPoint>(Arrays.asList(pts));
		tmp.add(p);
		setPts(tmp);
	}//setPt
	
	/**
	 * add a control point to the control point structure
	 * @param p
	 */
	public void addCntlPt(myPoint p){
		ArrayList<myCntlPt> tmp = new ArrayList<myCntlPt>(Arrays.asList(cntlPts));
		int i = tmp.size()-1;
		if(i > 0 ){tmp.get(i).w = calcCntlWeight(p,tmp.get(i),tmp.get(i-1));}//previous point's weight 
		myCntlPt tmpPt = new myCntlPt(p);
		tmp.add(tmpPt);
		setCPts(tmp);
	}//
	
	/**
	 * calculate the weight of each point by determining the distance from its two neighbors - radius is inversely proportional to weight
	 * @param a
	 * @param p
	 * @param b
	 * @return
	 */
	public float calcCntlWeight(myPoint a, myPoint p, myPoint b){	return (float)(myPoint._dist(a,p) + myPoint._dist(p,b));}
	
	@SuppressWarnings("unchecked")
	protected final <T extends myPoint> void _moveCurveToEndPoints(T[] cntlPts, myPoint startPt, myPoint endPt, boolean flip) {
		int numPoints = cntlPts.length;
		if(numPoints == 0){return;}
		
		//edge params		
		myVector drawnAxis = new myVector(cntlPts[0], cntlPts[numPoints - 1]);
		//angle between these two is the angle to rotate everyone
		myVector edgeAxis =  new myVector(startPt, endPt);		
		
		//transformation params
		//displacement myVectortor between start of drawn curve and edge 1.
		myVector dispToStart = new myVector(cntlPts[0], startPt);
		//displacement myVectortor between start of drawn curve and edge 1.
		double alpha =  -myVector._angleBetween(drawnAxis,edgeAxis);
		//ratio of distance from start to finish of drawn traj to distance between edges - multiply all elements in drawn traj by this
		double scaleRatio = edgeAxis._mag()/drawnAxis._mag();

		//displace to align with start
		movePoints(dispToStart, cntlPts);
		//build displacement vector - flip across axis if flipped curve
		myVector[] dispVecAra = new myVector[numPoints];
		dispVecAra[0] = new myVector();
		for(int myPointItr = 1; myPointItr < numPoints ; ++myPointItr){
			dispVecAra[myPointItr] = new myVector(cntlPts[0],cntlPts[myPointItr]);
		}			
		if((flip) || trajFlags.getIsFlipped()){
			myVector udAxis = myVector._unit(drawnAxis);
			myVector normPt, tanPt;
			for(int myPointItr = 1; myPointItr < numPoints ; ++myPointItr){
				//component in udAxis dir
				tanPt = myVector._mult(udAxis, dispVecAra[myPointItr]._dot(udAxis));
				normPt = myVector._sub(dispVecAra[myPointItr],tanPt);
				normPt._mult(2);
				dispVecAra[myPointItr]._sub(normPt);
			}
			trajFlags.setIsFlipped(flip);
		}

		//displace every point to be scaled distance from start of curve equivalent to scale of edge distances to drawn curve
		for(int myPointItr = 1; myPointItr < numPoints ; ++myPointItr){
			//start point displaced by scaleRatio * myVectortor from start to original location of myPoint
			cntlPts[myPointItr].set(new myPoint(cntlPts[0],scaleRatio, dispVecAra[myPointItr]));
		}
		//rotate every point around destCurve[0] by alpha
		for(int myPointItr = 1; myPointItr < numPoints ; ++myPointItr){
			cntlPts[myPointItr] = (T) cntlPts[myPointItr].rotMeAroundPt(alpha, this.c_bAra[0], this.c_tAra[0],cntlPts[0]);
		}	
	}//_moveCurveToEndPoints
			
	/**
	 * Move control point curve to new end points
	 * @param startPt
	 * @param endPt
	 * @param flip
	 * @return
	 */
	public void moveCntlCurveToEndPoints(myPoint startPt,myPoint endPt, boolean flip){
		_moveCurveToEndPoints(cntlPts, startPt, endPt, flip);
	}//moveCntlCurveToEndPoints
	
		
	/**
	 * subdivide, tuck, respace, etc, cntlpts of this curve
	 * @param numPts
	 * @param numReps
	 */
	public final void processCntlPts(int numPts, int numReps){
		double origLen = cntl_len;
		//makes 1 extra vert  equilspaced between each vert, to increase resolution of curve
		setCPts(procCntlPt(_subdivide, cntlPts, 2, origLen));										
		for(int i = 0; i < numReps; ++i){
			//setCPts(procCntlPt(_subdivide, cntlPts, 2, origLen));
			setCPts(procCntlPt(_tuck, cntlPts, .5f, origLen));
			setCPts(procCntlPt(_tuck, cntlPts, -.5f, origLen));
		}		//smooth curve - J4
		setCPts(procCntlPt(_equaldist, cntlPts, .5f, origLen));
		for(int i = 0; i < numReps; ++i){
			//setCPts(procCntlPt(_subdivide, cntlPts, 2, origLen));
			setCPts(procCntlPt(_tuck, cntlPts, .5f, origLen));
			setCPts(procCntlPt(_tuck, cntlPts, -.5f, origLen));
		}		//smooth curve - J4
		setCPts(procCntlPt(_resample, cntlPts, numPts, origLen));
	}			

	/**
	 * subdivide, tuck, respace, resample, etc. pts of this curve
	 * @param pts
	 * @param numPts
	 * @param numReps
	 */
	public final void processPts(myPoint[] pts, int numPts, int numReps){
		boolean isClosed = trajFlags.getIsClosed();
		//makes 1 extra vert  equilspaced between each vert, to increase resolution of curve
		setPts(procPts(_subdivide, pts, 2, len, isClosed));
		for(int i = 0; i < numReps; ++i){
			setPts(procPts(_subdivide, pts, 2, len, isClosed));
			setPts(procPts(_tuck, pts, .5f, len, isClosed));
			setPts(procPts(_tuck, pts, -.5f, len, isClosed));
		}		//smooth curve - J4
		setPts(procPts(_equaldist, pts, .5f, len, isClosed));
		for(int i = 0; i < numReps; ++i){
			setPts(procPts(_subdivide, pts, 2, len, isClosed));
			setPts(procPts(_tuck, pts, .5f, len, isClosed));
			setPts(procPts(_tuck, pts, -.5f, len, isClosed));
		}		//smooth curve - J4
		setPts(procPts(_resample, pts, numPts, len, isClosed));		
	}	
	
	
	/**
	 * sets required info for points array - points and dist between pts, length, etc
	 * @param tmp
	 */
	protected final void setPts(ArrayList<myPoint> tmp){
		pts = tmp.toArray(new myPoint[0]);
		boolean isClosed = trajFlags.getIsClosed();
		dpts = getPtDist(pts, isClosed);	
		len=length(pts, isClosed);
	}//setPts	
	//make a new point interpolated between either 2 or 3 points in pts ara, described by # of idxs
	public final myPoint makeNewPoint(myPoint[] pts, int[] idxs, double s){	return _Interp(pts[idxs[0]], s, (idxs.length == 2 ? pts[idxs[1]] : _Interp(pts[idxs[1]],.5f,pts[idxs[2]], lnI_Typ)),lnI_Typ );	}
	
	/**
	 * process all points using passed algorithm on passed array of points - not all args are used by all algs.
	 * @param _typ type of point processing
	 * @param pts array to be processed
	 * @param val quantity used by variou processing : subdivision-> # of new pts +1, tuck-> amt to be tucked,  resample-> # of new verts
	 * @param len length of segment described by points, including ends if closed
	 * @param wrap whether the point list wraps around or not
	 * @return arraylist of processed points
	 */
	public final ArrayList<myPoint> procPts(int _typ, myPoint[] _pts, double val, double _len, boolean wrap){
		ArrayList<myPoint> tmp = new ArrayList<myPoint>(); // temporary array
		switch(_typ){
			case _subdivide	:{
			    for(int i = 0; i < _pts.length-1; ++i){tmp.add(_pts[i]); for(int j=1;j<val;++j){tmp.add(makeNewPoint(_pts,new int[]{i,i+1}, (j/(val))));}}
			    tmp.add(_pts[_pts.length-1]);				
			    return tmp;}
			case _tuck		:{
				if(wrap){tmp.add(makeNewPoint(_pts,new int[]{0,_pts.length-1,1}, val));} else {tmp.add(0,_pts[0]);}
			    for(int i = 1; i < _pts.length-1; ++i){	tmp.add(i,makeNewPoint(_pts,new int[]{i,i-1,i+1}, val));   }
		    	if(wrap){tmp.add(makeNewPoint(_pts,new int[]{_pts.length-1,_pts.length-2,0}, val));} else {tmp.add(_pts[_pts.length-1]);}			
		    	return tmp;}
			case _equaldist	:{
				//new distance between each vertex, iterative dist travelled so far
				double ratio = _len/(1.0f * _pts.length),curDist = 0;					 			 
				for(int i =0; i<_pts.length; ++i){tmp.add(at(curDist/_len));curDist+=ratio;}	
				tmp.add(_pts[_pts.length-1]);				
				return tmp;}	
			case _resample	:{
				//distance between each vertex
				double ratio = _pts.length/(1.0f * (val-1)),f;							 
				int idx, newIdx=0;		
				for(double i = 0; i<_pts.length-1; i+=ratio){idx = (int)i;	f = i-idx;tmp.add(newIdx++,makeNewPoint(_pts,new int[]{idx,idx+1},f));}
				if(wrap) {
					if(myPoint._dist(tmp.get(newIdx-1), tmp.get(0)) > ratio){	tmp.add(makeNewPoint(new myPoint[]{tmp.get(newIdx-1), tmp.get(0)},new int[]{0,1},.5f));}		//want to only add another point if last 2 points are further than ratio appart
				} else {		tmp.add(_pts[_pts.length-1]);}			//always add another point if open line/loop - want to preserve end point
				break;}	
			default :
		}		
		return tmp;
	}
		
	//CONTROL POINT-RELATED FUNCTIONS
	//build essential orientation vectors for control points
	public void buildCntlFrameVecAras(){
		c_nAra = buildNormals(cntlPts);
		c_tAra = buildTangents(cntlPts, false);
		c_bAra = buildBinormals(cntlPts, c_nAra, c_tAra);		//use these with cntl point radius to build stroke pts
	}//buildCntlFrameVecAras
		
	//sets required info for points array - points and dist between pts, length, etc
	protected void setCPts(ArrayList<myCntlPt> tmp){
		cntlPts = tmp.toArray(new myCntlPt[0]);
		d_cntlPts = getPtDist(cntlPts, false);	
		cntl_len=length(cntlPts, false);
	}//setPts	
	//make a new point interpolated between either 2 or 3 points in pts ara, described by # of idxs
	public myCntlPt makeNewPoint(myCntlPt[] pts, int[] idxs, double s){	return _Interp(pts[idxs[0]], s, (idxs.length == 2 ? pts[idxs[1]] : _Interp(pts[idxs[1]],.5f,pts[idxs[2]], lnI_Typ)),lnI_Typ );	}
	/**
	 * process all points using passed algorithm on passed array of points - not all args are used by all algs.
	 * @param _typ type of point processing
	 * @param pts array to be processed
	 * @param val quantity used by variou processing : subdivision-> # of new pts +1, tuck-> amt to be tucked,  resample-> # of new verts
	 * @param len length of segment described by points, including ends if closed
	 * @param wrap whether the point list wraps around or not
	 * @return arraylist of processed points
	 */	
	public ArrayList<myCntlPt> procCntlPt(int _typ, myCntlPt[] pts, double val, double _len){
		ArrayList<myCntlPt> tmp = new ArrayList<myCntlPt>(); // temporary array
		switch(_typ){
			case _subdivide	:{
			    for(int i = 0; i < pts.length-1; ++i){tmp.add(pts[i]); for(int j=1;j<val;++j){tmp.add(makeNewPoint(pts,new int[]{i,i+1}, (j/(val))));}}
			    tmp.add(pts[pts.length-1]);				
			    return tmp;}
			case _tuck		:{
				tmp.add(0,pts[0]);//no wrap on control points, so no  need to check
			    for(int i = 1; i < pts.length-1; ++i){	tmp.add(i,makeNewPoint(pts,new int[]{i,i-1,i+1}, val));   }
		    	tmp.add(pts[pts.length-1]);			
		    	return tmp;}
			case _equaldist	:{
				double ratio = _len/(1.0f * pts.length),curDist = 0;					 //new distance between each vertex, iterative dist travelled so far			 
				for(int i =0; i<pts.length; ++i){tmp.add(at_C(curDist/_len, pts));curDist+=ratio;}	
				tmp.add(pts[pts.length-1]);				
				return tmp;}	
			case _resample	:{
				double ratio = pts.length/(1.0f * (val-1)),f;					//distance between each vertex		 
				int idx, newIdx=0;		
				for(float i = 0; i<pts.length-1; i+=ratio){idx = (int)i;	f = i-idx;tmp.add(newIdx++,makeNewPoint(pts,new int[]{idx,idx+1},f));}			
				tmp.add(pts[pts.length-1]);
				break;}	
			default :
		}
		return tmp;
	}	
	//end cntlmyPoint related
	//normals, tangents, binormals at each point
	public myVector[] buildNormals(myPoint[] _pts){
		ArrayList<myVector> tmp = new ArrayList<myVector>();
		for(int i =0; i<_pts.length; ++i){tmp.add(canvasNorm._normalized());	}		//make normal the canvas normal
		return tmp.toArray(new myVector[0]);
	}	
		

	public myVector[] buildTangents(myPoint[] _pts, boolean close){
		ArrayList<myVector> tmp = new ArrayList<myVector>();
		for(int i=0; i<_pts.length-1; ++i){tmp.add(myVector._unit(_pts[i], _pts[i+1]));}
		if(close){tmp.add(myVector._unit(_pts[_pts.length-1], _pts[0]));} 
		else {tmp.add(myVector._unit(_pts[_pts.length-2], _pts[_pts.length-1]));}
		return tmp.toArray(new myVector[0]);
	}	
	public myVector[] buildBinormals(myPoint[] _pts, myVector[] n_ara, myVector[] t_ara){//build last
		ArrayList<myVector> tmp = new ArrayList<myVector>();
		for(int i=0; i<_pts.length; ++i){tmp.add((n_ara[i]._cross(t_ara[i]))._normalize());}
		return tmp.toArray(new myVector[0]);
	}

	//find location of center of verts
	public myPoint calcCOV(){myPoint C = new myPoint();for(int i=0;i<pts.length;++i){C._add(pts[i]);} myPoint Ct = myPoint._mult(C,1.0f/pts.length); COV=new myPoint(Ct);return COV;}
	//find COV of passed verts
	public myPoint calcCOVOfAra(myPoint[] pts){myPoint C = new myPoint();for(int i=0;i<pts.length;++i){C._add(pts[i]);}myPoint Ct = myPoint._mult(C,1.0f/pts.length); return Ct;}

	
	/**
	 * return the interpolated myVectortor between two myPoint's myVectortors given the adjacent idx's of two points in pts and the array of myVectortors
	 * @param idxA, idxB : 2 idxs in myVector aras to be interped between
	 * @param s : interpolant
	 * @param vAra : array to find myVectors in
	 * @return interpolated myVectortor
	 */
	public myVector getInterpVec(int idxA, float s, int idxB, myVector[] vAra, int interpMech){return _Interp(vAra[idxA], s, vAra[idxB], interpMech);}
	public myVector getUInterpVec(int idxA, float s, int idxB, myVector[] vAra, int interpMech){return (_Interp(vAra[idxA], s, vAra[idxB], interpMech))._normalize();}
	
	/**
	 * using arc length parameterisation this will return a point along the curve at a 
	 * particular fraction of the length of the curve (0,1 will return endpoints, .5 will return halfway along curve)
	 * @param t fraction of curve length we are interested in returning a point - should be 0-1
	 * @return point @t along curve
	 */
	//put interpolant between adjacent axis points in s ara if needed
	public myPoint at(double t){return at(t,new double[1], len, pts, dpts);}
	//put interpolant between adjacent axis points in s ara if needed
	public myPoint at(double t, double[] s){return at(t,s, len, pts, dpts);}
	//call directly if wanting interpolant between adj axis points too
	public myPoint at(double t, double[] s, double _len, myPoint[] pts, double[] _dpts){
		if(t<0){System.out.println("In at : t="+t+" needs to be [0,1]");return pts[0];} else if (t>1){System.out.println("In at : t="+t+" needs to be [0,1]");return pts[pts.length-1];}
		double dist = t * _len;
		//built off dpts so that it will get wrap for closed curve
		for(int i=0; i<_dpts.length-1; ++i){										
			if(_dpts[i+1] >= dist){
				//needs to stay between 0 and 1 (since interpolation functions between 
				//pts will be 0-1 based), so normalize by distance dpts[i]
				s[0] = ((dist-_dpts[i])/(_dpts[i+1]-_dpts[i]));		
				//put interpolant between adjacent axis points in s ara if needed
				return makeNewPoint(pts,new int[]{i,((i+1)%pts.length)}, s[0]);		
			}					
		}		
		return pts[0];
	}//at	
	
	public myCntlPt at_C(double t, myCntlPt[] pts){double[] _dpts = this.getPtDist(pts, false);double _len = this.length(pts, false);return at_C(t,new double[1], _len, pts, _dpts);}//put interpolant between adjacent axis points in s ara if needed
	public myCntlPt at_C(double t, double[] s, double _len, myCntlPt[] pts, double[] _dpts){//call directly if wanting interpolant between adj axis points too
		if(t<0){System.out.println("In at : t="+t+" needs to be [0,1]");return pts[0];} else if (t>1){System.out.println("In at : t="+t+" needs to be [0,1]");return pts[pts.length-1];}
		double dist = t * _len;
		for(int i=0; i<_dpts.length-1; ++i){										//built off dpts so that it will get wrap for closed curve
			if(_dpts[i+1] >= dist){
				s[0] = ((dist-_dpts[i])/(_dpts[i+1]-_dpts[i]));					//needs to stay between 0 and 1 (since interpolation functions between pts will be 0-1 based), so normalize by distance dpts[i]
				return makeNewPoint(pts,new int[]{i,((i+1)%pts.length)}, s[0]);		//put interpolant between adjacent axis points in s ara if needed		
			}			
		}		
		return new myCntlPt();
	}//at_C	
	
	/**
	 * this will conduct the appropriate interpolation on the two passed points, based on what type is set for the interpolation requested
	 * as more interpolation schemes are implemented, add cases
	 * @param A, B : points to be interpolated
	 * @param s : interpolant
	 * @param _typ : what is being interpolated - smoothing a line, sweeping the curve around the axis, etc.  for each _typ, a value should be set for this curve (i.e.smInterpTyp)
	 * @return : resultant point
	 */
	protected myPoint _Interp(myPoint A, double s, myPoint B, int _typ){
		switch (_typ){
			case linear_int : {	return new myPoint(A, s, B);}
			//add more cases for different interpolation		
			default : {	return new myPoint(A, s, B);}			//defaults to linear
		}	
	}//_Interp
	/**
	 * same as above, but with myVectors
	 * @param A
	 * @param s
	 * @param B
	 * @param _typ
	 * @return
	 */
	protected myVector _Interp(myVector A, double s, myVector B, int _typ){
		switch (_typ){
			case linear_int : {	return new myVector(A, s, B);}
			//add more cases for different interpolation		
			default : {	return new myVector(A, s, B);}			//defaults to linear
		}	
	}//_Interp
	
	/**
	 * same as above but with doubles
	 * @param A
	 * @param s
	 * @param B
	 * @param _typ
	 * @return
	 */
	protected double _Interp(double A, double s, double B, int _typ){
		switch (_typ){
			case linear_int : {	return (1-s)*A + (s*B);}
			//add more cases for different interpolation		
			default : {	return (1-s)*A + (s*B);}			//defaults to linear
		}	
	}//_Interp

	/**
	 * same as above but with myCntlPts
	 * @param A
	 * @param s
	 * @param B
	 * @param _typ
	 * @return
	 */
	protected myCntlPt _Interp(myCntlPt A, double s, myCntlPt B, int _typ){
		switch (_typ){
			case linear_int : {	return myCntlPt.L(A, s, B);}
			//add more cases for different interpolation		
			default : {	return myCntlPt.L(A, s, B);}			//defaults to linear
		}	
	}//_Interp	

	/**
	 * draw currently selected control point
	 * @param ri
	 * @param i
	 */
	public void drawSelPoint(IRenderInterface ri, int i){
		drawSelPoint(ri, i, new int[] {255,255,0});
	}
	/**
	 * draw currently selected control point with given highlight color
	 * @param ri
	 * @param i
	 * @param clr highlight color (first 3 idxs)
	 */
	public void drawSelPoint(IRenderInterface ri, int i, int[] clr){
		ri.pushMatState();
		ri.setStroke(clr,255);
		if(trajFlags.getUsesCntlPts()){ri.showPtAsSphere(cntlPts[i], 3.0f, 5, IRenderInterface.gui_Black, IRenderInterface.gui_Black);} 
		else {ri.showPtAsSphere(pts[i], 3.0f, 5, IRenderInterface.gui_Black, IRenderInterface.gui_Black);}
		ri.popMatState();
	}
	
	
	private void buildPtsFromCntlPts(){
		ArrayList<myPoint> tmp =  _offset.calcOffset(cntlPts, c_bAra, c_tAra) ;
		pts = tmp.toArray(new myPoint[0]);		
		ptsDerived = true;		
	}//buildPtsFromCntlPts

	//calculate initial weights for last few points of drawn cntlpoint line
	private void finalizeCntlW(){
		if(cntlPts.length == 0){return ;}
		cntlPts[0].w = cntlPts.length < 2 ? 1 : cntlPts[1].w;
		if(cntlPts.length == 1){return ;}
		cntlPts[cntlPts.length-2].w = cntlPts.length < 3 ? cntlPts[0].w : calcCntlWeight(cntlPts[cntlPts.length-3],cntlPts[cntlPts.length-2],cntlPts[cntlPts.length-3]);
		cntlPts[cntlPts.length-1].w = cntlPts.length < 2 ? cntlPts[0].w : cntlPts[cntlPts.length-2].w;
	}
	
	
	/**
	 * build poly loop points using offsets from control points radii
	 */
	public void rebuildPolyPts(){
		trajFlags.setRecalcPoints(false);
		//buildPointsUsingOffset(true,1);
		buildPtsFromCntlPts();
		trajFlags.setIsMade(true);
	}

	/**
	 * makes a copy of the points in order
	 * @param pts
	 * @return
	 */
	public myPoint[] cpyPoints(myPoint[] pts){myPoint[] tmp = new myPoint[pts.length]; for(int i=0; i<pts.length; ++i){	tmp[i]=new myPoint(pts[i]);}	return tmp;}//cpyPoints
	/**
	 * makes a copy of the points in order
	 * @param pts
	 * @return
	 */
	public myCntlPt[] cpyPoints(myCntlPt[] pts){myCntlPt[] tmp = new myCntlPt[pts.length]; for(int i=0; i<pts.length; ++i){	tmp[i]=new myCntlPt(pts[i]);}	return tmp;}//cpyPoints
	
	/**
	 * move points by the passed myVector
	 * @param <T> type of points
	 * @param move vector to displace by
	 * @param pts points to displace
	 */
	protected <T extends myPoint> void movePoints(myVector move, T[] pts) {for(int i =0; i<pts.length; ++i){	pts[i]._add(move);	}}	
	//set this object's points to be passed points, for copying
	public void setPtsToArrayList(myCntlPt[] pts){ArrayList<myCntlPt> tmp = new ArrayList<myCntlPt>(Arrays.asList(pts));setCPts(tmp);}	
	//set this object's points to be passed points, for copying
	public void setPtsToArrayList(myPoint[] pts){ArrayList<myPoint> tmp = new ArrayList<myPoint>(Arrays.asList(pts));setPts(tmp);}	


	/**
	 * rotate points around axis that is xprod of canvas norm and the lstroke cov to the rstroke 
	 * cov myVector at stroke cov. should make mirror image of pts.
	 * 
	 * @param _pts
	 * @param angle
	 * @param cov
	 * @param _canvasNorm need to pass canvas norm since it cannot be static
	 * @param _covNorm
	 * @return
	 */

	@SuppressWarnings("unchecked")
	private <T extends myPoint> T[] _rotPtsAroundCOV(Class<T> cls, T[] _pts, float angle, myPoint cov, myVector _canvasNorm, myVector _covNorm) {
		T[] tmp = (T[]) Array.newInstance(cls, _pts.length);
		//for(int i=0; i<pts.length; ++i){tmp.add(ri.R(pts[i], angle, new myVector(_canvasNorm), _covNorm, cov));}
		for(int i=0; i<_pts.length; ++i){tmp[i] = (T) _pts[i].rotMeAroundPt(angle, new myVector(_canvasNorm), _covNorm, cov);}		
		return tmp;
	}
	
	/**
	 * rotate points around axis that is xprod of canvas norm and the lstroke cov to the rstroke 
	 * cov myVector at stroke cov. should make mirror image of pts.
	 * 
	 * @param _pts
	 * @param angle
	 * @param cov
	 * @param _canvasNorm need to pass canvas norm since it cannot be static
	 * @param _covNorm
	 * @return
	 */
	public myPoint[] rotPtsAroundCOV(myPoint[] _pts, float angle, myPoint cov, myVector _canvasNorm, myVector _covNorm){
		return _rotPtsAroundCOV(myPoint.class, _pts,angle, cov, _canvasNorm, _covNorm);
	}//	
	/**
	 * rotate points around axis that is xprod of canvas norm and the lstroke cov to the rstroke cov myVector at stroke cov. 
	 * should make mirror image of pts
	 * @param cpts
	 * @param angle
	 * @param cov
	 * @param _canvasNorm
	 * @param _covNorm
	 * @return
	 */
	public myCntlPt[] rotPtsAroundCOV(myCntlPt[] cpts, float angle, myPoint cov, myVector _canvasNorm, myVector _covNorm){//need to pass canvas norm since it cannot be static
		return _rotPtsAroundCOV(myCntlPt.class, cpts,angle, cov, _canvasNorm, _covNorm);		
	}//	
	
	@SuppressWarnings("unchecked")
	private <T extends myPoint> T[] _rotPtsAroundCOV(Class<T> cls, T[] _pts, double angle, myPoint cov, myVector _canvasNorm, myVector _covNorm) {
		T[] tmp = (T[]) Array.newInstance(cls, _pts.length);
		//for(int i=0; i<pts.length; ++i){tmp.add(ri.R(pts[i], angle, new myVector(_canvasNorm), _covNorm, cov));}
		for(int i=0; i<_pts.length; ++i){tmp[i] = (T) _pts[i].rotMeAroundPt(angle, new myVector(_canvasNorm), _covNorm, cov);}		
		return tmp;
	}
	/**
	 * rotate points around axis that is xprod of canvas norm and the lstroke cov to the rstroke cov vec at stroke cov.
	 * should make mirror image of pts
	 * @param _pts
	 * @param angle
	 * @param cov
	 * @param _canvasNorm
	 * @param _covNorm
	 * @return
	 */
	public myPoint[] rotPtsAroundCOV(myPoint[] _pts, double angle, myPoint cov, myVector _canvasNorm, myVector _covNorm){//need to pass canvas norm since it cannot be static
		return _rotPtsAroundCOV(myPoint.class, _pts,angle, cov, _canvasNorm, _covNorm);			
	}//	
	/**
	 * rotate points around axis that is xprod of canvas norm and the lstroke cov to the rstroke cov vec at stroke cov.
	 * should make mirror image of pts
	 * @param _pts
	 * @param angle
	 * @param cov
	 * @param _canvasNorm
	 * @param _covNorm
	 * @return
	 */
	public myCntlPt[] rotPtsAroundCOV(myCntlPt[] _pts, double angle, myPoint cov, myVector _canvasNorm, myVector _covNorm){//need to pass canvas norm since it cannot be static
		return _rotPtsAroundCOV(myCntlPt.class, _pts,angle, cov, _canvasNorm, _covNorm);
	}//		
	
	/**
	 * finds index of point with largest projection on passed myVectortor in passed myPoint ara
	 * @param v
	 * @param c
	 * @param pts
	 * @return
	 */
	protected int findLargestProjection(myVector v, myPoint c, myPoint[] pts){
		double prjLen = -1, d;
		int res = -1;
		for(int i=0; i<pts.length; ++i){d = myVector._dot(v,new myVector(c, pts[i]));if(d > prjLen){prjLen = d;res = i;}}	
		return res;
	}//findLargestProjection : largest projection on passed myVectortor in passed myPoint ara		
	
	//overriding base class for cntrl-point driven constructs
	public int findClosestPt(myPoint p, double[] d){	
		return findClosestPt(p, d,cntlPts);
	}
	/**
	 * finds closest point to p in sPts - put dist in d
	 * @param p
	 * @param d
	 * @param _pts
	 * @return
	 */
	protected final int findClosestPt(myPoint p, double[] d, myPoint[] _pts){
		int res = -1;
		double mindist = 99999999, _d;
		for(int i=0; i<_pts.length; ++i){_d = myPoint._dist(p,_pts[i]);if(_d < mindist){mindist = _d; d[0]=_d;res = i;}}	
		return res;
	}
	/**
	 * reorder verts so that they start at newStIdx - only done for closed  meshes, so should wrap around. 
	 * should be called by implementing class
	 * @param newStIdx
	 * @param pts
	 * @return
	 */
	protected ArrayList<myPoint> reorderPts(int newStIdx, myPoint[] pts){
		ArrayList<myPoint> tmp = new ArrayList<myPoint>(Arrays.asList(pts));
		for(int i = 0; i<pts.length; ++i){tmp.set(i, pts[(i+newStIdx)%pts.length]);	}
		return tmp;
	}		
	
	/**
	 * remake drawn trajectory after edit
	 */
	public void remakeDrawnTraj(boolean useVels){
		for(int i = 0; i < 10; ++i){
			//setInterpPts(procInterpPts(_subdivide, interpCntlPts, 2, interpLen));
			setCPts(procCntlPt(_tuck, cntlPts, .5f, cntlPts.length));
			setCPts(procCntlPt(_tuck, cntlPts, -.49f, cntlPts.length));
		}		//smooth curve - J4
		setCPts(procCntlPt(_equaldist, cntlPts, .5f, cntlPts.length));
		setCPts(procCntlPt(_resample, cntlPts, numIntCntlPts, cntlPts.length));
			
	}//remakeDrawnTraj

	/**
	 * run at the end of drawing a curve - will set appropriate flags, execute smoothing, subdivision, resampling, etc
	 * @param procPts
	 */
	public final void finalizeDrawing(boolean procPts){
		//by here we have the drawn points from user input we want use the offset to determine the actual points of the curve we want to put this in a function so that any changes to the 
		//cntlpoints can be cascaded down to the actual loop		
		buildPointsUsingOffset(procPts, numReps);
		//calculate line points from control points
		//find loop around stroke line by cntl points' radii once loop is built, treat as poly loop
		trajFlags.setFinalizeDrawing();
		//build array of weights from 
		finalizeDrawing_Priv(procPts);
	}//finalize
	
	/**
	 * build pts array using cntlpoints and offset chosen
	 * @param procPts
	 * @param repCnt
	 */
	public final void buildPointsUsingOffset(boolean procPts, int repCnt){
		if(procPts){
		    finalizeCntlW();
		    boolean CntlInvRadWt = trajFlags.getCntlWtInvRad();
		    for(int i=0;i<cntlPts.length;++i){cntlPts[i].calcRadFromWeight(cntl_len/cntlPts.length, CntlInvRadWt, wScale);}           //sets all radii based on weights
		    processCntlPts(trajFlags.getInterpStroke() ? numIntCntlPts : numCntlPts, repCnt);
	    }
		buildCntlFrameVecAras();
		buildPtsFromCntlPts();
	}//buildPointsUsingOffset
	
	
	/**
	 * Instance-class specific finalizing
	 * @param procPts
	 */	
	protected abstract void finalizeDrawing_Priv(boolean procPts);
	

	public void drawMe(IRenderInterface ri) {
		ri.pushMatState();
		ri.setFill(fillClr,255);
		ri.setStroke(strkClr,255);
			ri.setStrokeWt(1.0f);
//			if(flags[useProcCurve]){ri.show(pts);} 
//			else {			
				ri.catmullRom2D(pts);
				//}
		ri.popMatState();
//		if(flags[drawNorms] && (nAra != null)&& (tAra != null)&& (bAra != null)){drawNorms(pts, nAra,tAra,bAra,20);}
//		drawCOV();
	}
	
	/**
	 * Draw this variable trajectory
	 * @param ri
	 * @param useDrawnVels
	 * @param flat
	 */
	public void drawMe(IRenderInterface ri, boolean useDrawnVels, boolean flat){
		ri.pushMatState();
			ri.setFill(fillClr,255);
			ri.setStroke(strkClr,255);
			ri.setStrokeWt(1.0f);
			if(flat) {
				for(int i = 0; i < cntlPts.length; ++i){
					ri.showPtAsCircle(cntlPts[i],1.0*trajPtRad,fillClr,strkClr);
				}					
			} else {
				for(int i = 0; i < cntlPts.length; ++i){
					ri.showPtAsSphere(cntlPts[i],1.0*trajPtRad,5,fillClr,strkClr);
				}				
			}
			if(trajFlags.getDrawCntlRad()){_offset.drawCntlPts(ri, cntlPts, c_bAra, c_tAra, ptsDerived);}
			ri.popMatState();		
	}//
	
	
	/**
	 * drag point represented by passed idx in passed array - either point or cntl point
	 */
	public void dragPicked(myVector disp, int idx) {dragPicked(disp, idx, cntlPts);}
	/**
	 * drag all points by finding displacement to mouse for COV and moving all points by same dislacement
	 */
	public void dragAll(myVector disp) {dragAll(disp, cntlPts);}	
	//move COV to location pointed at by mouse, and move all points by same displacement
	//drag point represented by passed idx in passed array - either point or cntl point
	protected void dragPicked(myVector dispVec, int idx, myPoint[] pts) {if((-1 != idx) && (pts[idx] != null)){pts[idx]._add(dispVec);trajFlags.setRecalcPoints(true);}}
	protected void dragAll(myVector dispVec, myPoint[] pts){if((pts == null)||(pts.length == 0)){return;}for(int i=0;i<pts.length;++i){pts[i]._add(dispVec);}trajFlags.setRecalcPoints(true);}

	/**
	 * Called when mouse is dragged
	 * @param dispVec
	 * @param drawnTrajPickedIdx
	 */
	public final void handleMouseDrag(myVector dispVec, int drawnTrajPickedIdx) {
		if((drawnTrajPickedIdx == 0) || (drawnTrajPickedIdx == pts.length-1)) {return;}	//rough bounds checking
		myPoint[] pts = getDrawnPtAra(false);
		int minBnd = MyMathUtils.max(drawnTrajPickedIdx - drawnTrajEditWidth, 0),
			maxBnd = MyMathUtils.min(drawnTrajPickedIdx + drawnTrajEditWidth, pts.length-1);		
		//win.getMsgObj().dispInfoMessage("Base_DrawnTrajectory", "handleMouseDrag","Drag in drag zone inside disp calc -> idx bounds : " + minBnd + " | " + maxBnd);
		float modAmt, invdistLow = 1.0f/(drawnTrajPickedIdx - minBnd), invdistHigh = 1.0f/(maxBnd - drawnTrajPickedIdx);
		for(int idx = minBnd; idx < maxBnd; ++idx){
			float divMultVal = (idx > drawnTrajPickedIdx) ? invdistHigh:invdistLow;
			modAmt = (float) (trajDragScaleAmt* Math.cos((idx-drawnTrajPickedIdx) * MyMathUtils.HALF_PI * divMultVal));//trajDragScaleAmt/abs(1 + (idx-drawnTrajPickedIdx));
			//modAmt *= modAmt;
			pts[idx]._add(myVector._mult(dispVec,modAmt));
		}
	}//handleMouseDrag
	
	/**
	 * return appropriate ara of points based on using velocities or not
	 * @param useVels
	 * @return
	 */
	public abstract myPoint[] getDrawnPtAra(boolean useVels);
	
	/**
	 * scale the points - for when the window is resized 
	 * @param useDrawnVels
	 * @param scAmtY
	 * @param borderOffset
	 */
	public void scaleMeY(boolean useDrawnVels, float scAmtY, float borderOffset){
		myPoint[] pts = getDrawnPtAra(useDrawnVels);
  			for(int i = 0; i < pts.length; ++i){
				pts[i].y -= borderOffset;
				pts[i].y *= scAmtY;
				pts[i].y += borderOffset;
    	}
	}//scaleMeY 
	
	
	/**
	 * scale points to be a scaleAmt * current distance from line of myPoint a -> myPoint b
	 * @param a
	 * @param b
	 * @param perpVec
	 * @param scaleAmt
	 */
	public void scalePointsAboveAxis(myPoint a, myPoint b, myVector perpVec, double scaleAmt){
		myPoint[] pts = getDrawnPtAra(false);//, newPts = new myPoint[pts.length];\
		int numPoints = pts.length;
		//myPoint[] newPts = new myPoint[numPoints];
		double dist;
		//win.getMsgObj().dispInfoMessage("Base_DrawnTrajectory","scalePointsAboveAxis","cntlPts size at scalePointsAboveAxis : " + pts.length,true);
		for(int i =0; i<numPoints; ++i){
			dist = MyMathUtils.distToLine(pts[i], a,b);
			//if(Double.isNaN(dist)){dist = 0;}
			myPoint pointOnLine = MyMathUtils.projectionOnLine(pts[i], a,b);
			myVector resVec = myVector._mult(myVector._unit(pointOnLine,pts[i]),dist*scaleAmt);
			//win.getMsgObj().dispInfoMessage("Base_DrawnTrajectory","scalePointsAboveAxis","cntlPts : st : dist*scale : "+ (dist*scaleAmt)+" dist : "+ (dist)+" scale : "+ (scaleAmt)+" stPoint : " + pts[i].toStrBrf() + " | linePt : " + pointOnLine.toStrBrf() + " | resVec : " +resVec.toStrBrf() ,true);
			pts[i].set(new myPoint(pointOnLine, resVec));
		}
	}
	
	/**
	 * returns array of distances to each point from beginning - needs to retain dist from last vert to first if closed
	 * @param pts
	 * @param wrap
	 * @return
	 */
	public final double[] getPtDist(myPoint[] pts, boolean wrap){
		double[] res = new double[pts.length+1];
		res[0]=0;
		for(int i=1; i<pts.length; ++i){
			//System.out.println("i : "+i);
			res[i] = res[i-1] + myPoint._dist(pts[i-1],pts[i]);
			}
		if(wrap){
			//System.out.println("wrap");
			res[pts.length] = res[pts.length-1] + myPoint._dist(pts[pts.length-1],pts[0]);
		} else {
			//System.out.println("no wrap");
			
			res[pts.length] = 0;
		}
		
		return res;}
	//returns length of curve, including endpoint if closed
	public final double length(myPoint[] pts, boolean closed){double res = 0;for(int i =0; i<pts.length-1; ++i){res += myPoint._dist(pts[i],pts[i+1]);}if(closed){res+=myPoint._dist(pts[pts.length-1],pts[0]);}return res;}


	public int getNumCntlPts() {return cntlPts.length;}
	
	public void drawCOV(IRenderInterface ri){		
		if(COV == null) {return;}		
		ri.pushMatState();
		ri.setStroke(255,0,255,255);		
		ri.showPtAsSphere(COV, 3.0f, 5, IRenderInterface.gui_Black, IRenderInterface.gui_Black);		
		ri.popMatState();	
	}
	
	public myPoint getPt(int i){return pts[i];}

	/**
	 * Debug : print out all trajectory point locations for debugging
	 * @param useDrawnVels
	 */
	public void dbgPrintAllPoints(boolean useDrawnVels){
		win.getMsgObj().dispInfoMessage("Base_DrawnTrajectory", "dbgPrintAllPoints","Drawn Traj :\n");
		for(int i = 0; i < cntlPts.length; ++i){
			win.getMsgObj().dispInfoMessage("Base_DrawnTrajectory", "dbgPrintAllPoints","\tpt " + i +" : " + cntlPts[i].toStrBrf());
		}   
	}//dbgPrintAllPoints
	
	public String toString(){
		String res = "Offset calc : "+ _offset+ " | #pts : "+pts.length+" len : "+ len ;
		return res;
	}

}//myDrawnObject
