package base_UI_Objects.windowUI.drawnTrajectories.curves;

import java.util.ArrayList;
import java.util.TreeMap;

import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;

/**
 * This class describes a collection of sequential myPoints describing some kind of curve
 */
public class myCurve {
    /**
     * The points that make up this curve
     */
    protected ArrayList<myPoint> _points;
    /**
     * The overall length of the curve
     */
    protected double _len;
    /**
     * The distance from the first point to each subsequent point along the curve.
     */
    protected double[] _ptDists;
    
    /**
     * Array of normals for curve at each vertex
     */
    protected myVector[] _normals;
    /**
     * Array of tangents for curve at each vertex
     */
    protected myVector[] _tangents;
    /**
     * Array of binormals for curve at each vertex
     */
    protected myVector[] _binormals;
    
    /**
     * Map keyed by distance along trajectory, value is index of point at that distance
     */
    protected TreeMap<Double, Integer> _distsToIdx;
    
    /**
     * Whether or not this curve should be closed (linked last point to first) TODO replace with flags structure
     */
    protected boolean _isClosed;
    
    /**
     * Normal to the canvas this curve has been drawn upon
     */
    protected myVector _canvasNorm;
    
    public myCurve(myVector canvasNorm, boolean isClosed) {
        _canvasNorm = new myVector(canvasNorm);
        _isClosed = isClosed;
        _points = new ArrayList<myPoint>();
        _len = 0;
        _ptDists = new double[0];
        _distsToIdx = new TreeMap<Double, Integer>();
        _normals = new myVector[0];
        _tangents = new myVector[0];
        _binormals = new myVector[0];
    }
    
    /**
     * Add a point to this trajectory at the end of the list
     * @param p
     */
    public final void addPoint(myPoint p) {                 _points.add(p);         updateTrajVals();}
    
    /**
     * Insert an element at a specified location in the list
     * @param idx location to insert point. All subsequent points will be moved over
     * @param p point to insert
     */
    public final void insertPoint(int idx, myPoint p) {     _points.add(idx, p);    updateTrajVals();}
    
    /**
     * Update the first-to-x'th point dist array and length of the curve values
     */
    public final void updateTrajVals() {
        boolean isClosed = isClosed();
        //update distances
        int numPts = _points.size();
        _ptDists = new double[numPts+1];
        _distsToIdx = new TreeMap<Double, Integer>();
        _ptDists[0] = 0;
        _distsToIdx.put(0.0,  0);
        for(int i=1;i<numPts;++i) {
            //strictly non-decreasing
            double dist = _ptDists[i-1]+myPoint._dist(_points.get(i-1), _points.get(i));
            _ptDists[i] = dist;
            _distsToIdx.put(dist,  i);            
        }
        if(isClosed){
            double dist = _ptDists[numPts-1] + myPoint._dist(_points.get(numPts-1), _points.get(0)); 
            _ptDists[numPts] = dist;
            // record the first idx as the one that is at this distance (numPts will equal 0 after modulo)
            _distsToIdx.put(dist,  numPts);
        } 
        else {
            // not a wrap around, so only duplicate distance for final entry
            _ptDists[numPts] = _ptDists[numPts-1];        
        } 
        //update length
        _len = _ptDists[numPts];
        
        _normals = new myVector[numPts];
        for(int i=0; i<numPts; ++i){            _normals[i] = _canvasNorm._normalized();}
        
        _tangents = new myVector[numPts];
        // take care of tangents for wrap traj and non-wrap traj
        _tangents[0] = myVector._unit((isClosed ? _points.get(numPts-1) : _points.get(0)), _points.get(1));
        for(int i=1; i<numPts-1; ++i) {         _tangents[i] = myVector._unit(_points.get(i-1), _points.get(i+1));}
        _tangents[numPts-1] = myVector._unit(_points.get(numPts-2), (isClosed ? _points.get(0) : _points.get(numPts-1)));
        
        _binormals = new myVector[numPts];
        for(int i=0; i<numPts; ++i){            _binormals[i] = (_normals[i]._cross(_tangents[i]))._normalize();} 
    }//updateTrajVals()
    /**
     * Make a new point interpolated between either 2 points or between the first point and the average of the 2nd and 3rd points, @ s [0,1]
     * @param idxs either 2 or 3 idxs in the _points array to interpolate between
     * @param s amount to interpolate (usually between 0 and 1)
     * @return the point result of the interpolation
     */
    public final myPoint makeNewPoint( int[] idxs, double s){    
        return new myPoint(_points.get(idxs[0]), s, (idxs.length == 2 ? _points.get(idxs[1]) : new myPoint(_points.get(idxs[1]),.5f,_points.get(idxs[2]))));    
    }
    /**
     * Using arc length parameterisation this will return a point along the curve at a 
     * particular fraction of the length of the curve (0,1 will return endpoints, .5 will return halfway along curve)
     * @param t fraction of curve length we are interested in returning a point - should be 0-1
     * @return point @t along curve
     */
    public final myPoint getPointAlongCurve(double t) {
        // get copy of first point if t at beginning of curve
        if(t <= 0) {return new myPoint(_points.get(0));}
        // get copy of final point if t at end of curve, either last point of curve if not closed, or first if closed.
        if(t >= 1) {return new myPoint(_points.get(_distsToIdx.get(_len)%_points.size()));}
        double ttlDist = t * _len;
        // entry with greatest distance less than ttlDist. Idxs we want are between distIdx.value and distIdx.value+1
        var distIdx = _distsToIdx.floorEntry(ttlDist);
        Integer idx = distIdx.getValue();
        // calculate interpolant
        double interp = (ttlDist-_ptDists[idx])/(_ptDists[idx+1]-_ptDists[idx]);
        return new myPoint(_points.get(idx), interp, _points.get((idx+1)%_points.size()));
    }//getPointAlongCurve
     
    /**
     * Build a new array of numPts equalSpaced points based on current _points array
     * @param numPts number of new points desired
     * @param wrap whether the point array wraps around/is a loop or not
     * @return new array of numPts equalspaced points.
     */
    private final ArrayList<myPoint> _buildEqualSpacedList(int numPts, boolean wrap){
        ArrayList<myPoint> tmp = new ArrayList<myPoint>(); // temporary array
        // We want to account for final space between last point and first point if wrap around.
        double numSpaces = 1.0 * (wrap ? numPts : numPts - 1);
        //new equally-spaced distance between each vertex
        double ratio = _len/numSpaces;
        // iterative dist travelled so far
        double curDist = 0;                
        while(curDist < _len) {
            tmp.add(getPointAlongCurve(curDist/_len));
            curDist+=ratio;
        }    
        tmp.add(_points.get(numPts-1)); 
        return tmp;
    }
    
    /**
     * Process all points in this curve using passed algorithm.
     * @param _type Type of algorithm to execute
     * @param _val Quantity used by different processes based on the algorithm : 
     *          subdivide : # of new points between each point
     *          tuck : amount to be 'tucked' (toward or away from average of 2 neighbors)
     *          resample : # of new points total for curve
     */
    public final void processThisCurve(CurveProcessing _type, double _val) {
        boolean wrap = isClosed();
        int numPts = _points.size();
        ArrayList<myPoint> tmp = new ArrayList<myPoint>(); // temporary array
        switch(_type){
            case subdivide    :{
                for(int i = 0; i < numPts-1; ++i){tmp.add(_points.get(i)); for(int j=1;j<_val;++j){tmp.add(makeNewPoint(new int[]{i,i+1}, (j/(_val))));}}
                tmp.add(_points.get(numPts-1));                
                break;}
            case tuck        :{
                if(wrap){tmp.add(makeNewPoint(new int[]{0,numPts-1,1}, _val));} else {tmp.add(0,_points.get(0));}
                for(int i = 1; i < numPts-1; ++i){    tmp.add(i,makeNewPoint(new int[]{i,i-1,i+1}, _val));   }
                if(wrap){tmp.add(makeNewPoint(new int[]{numPts-1,numPts-2,0}, _val));} else {tmp.add(_points.get(numPts-1));}            
                break;}
            case equalDist    :{
                tmp = _buildEqualSpacedList(numPts, wrap);               
                break;}    
            case resample    :{
                tmp = _buildEqualSpacedList((int) _val, wrap);
                break;}    
            default :
        } 
        _points = tmp;
        updateTrajVals();        
    }//processThisCurve    
    
    /**
     * Set whether this curve is closed
     * @param isClosed
     */
    public final void setIsClosed(boolean isClosed) {
        boolean oldClosed = _isClosed;
        _isClosed = isClosed;
        //recalc traj values based on whether closed has changed or not
        if(oldClosed != _isClosed) {        updateTrajVals();      }
    }
    
    /**
     * Get whether this curve is closed
     * @return
     */
    public final boolean isClosed() {return _isClosed;}
    
    /**
     * Set the normal to the canvas that this curve is being drawn upon
     * @param canvasNorm
     */
    public void setCanvasNorm(myVector canvasNorm){
        _canvasNorm = new myVector(canvasNorm);
    }
    
    
}//class myCurve
