package base_UI_Objects.windowUI.drawnTrajectories.base;

import base_Utils_Objects.tools.flags.Base_BoolFlags;

/**
 * @author John Turner
 *
 */
public class DrawnTrajFlags extends Base_BoolFlags {
    
    private final Base_DrawnTrajectory owner;
    //flag idxs
    public static final int 
        //construction
            isClosedIDX         = _numBaseFlags,            //object is a closed poly
            isMadeIDX           = _numBaseFlags + 1,         //whether or not the object is finished being drawn
            isFlippedIDX        = _numBaseFlags + 2,        //points being displayed are flipped (reflected)
            usesCntlPtsIDX      = _numBaseFlags + 3,        //if this curve is governed by control points (as opposed to just drawn freehand)
        //calculation    
            reCalcPointsIDX     = _numBaseFlags + 4,        //recalculate points from cntl point radii - use if radius is changed on stroke from user input
            cntlWInvRadIDX      = _numBaseFlags + 5,        //whether the weight impact on cntl radius is inverse or direct - inverse has slow drawing be wider, direct has slow drawing be narrower
            interpStrokeIDX     = _numBaseFlags + 6,        //whether or not control-point based strokes are interpolating or not
        //display
            showCntlPntsIDX     = _numBaseFlags + 7,        //show this object's cntl points
            useVertNormsIDX     = _numBaseFlags + 8,        //use vertex normals to shade curve
            drawNormsIDX        = _numBaseFlags + 9,        //display normals for this object as small arrows
            drawCntlRadIDX      = _numBaseFlags + 10,
            useProcCurveIDX     = _numBaseFlags + 11;        //toggles whether we use straight lines in vertex building or processing's curve vertex        
    private static final int _numStateFlags = _numBaseFlags + 12;
    
    /**
     * @param _owner Owning trajectory
     */
    public DrawnTrajFlags(Base_DrawnTrajectory _owner) {
        super(_numStateFlags);
        owner = _owner;        
    }

    
    /**
     * Set the initial flags for the variable trajectory upon construction
     */
    public final void setInitTrajFlags() {
        setFlag(isClosedIDX,false);    
        setFlag(drawCntlRadIDX, true);
        setFlag(usesCntlPtsIDX, true);
        setFlag(interpStrokeIDX, true);
        setFlag(cntlWInvRadIDX, false);
    }
    
    /**
     * Set the appropriate flags when the owning trajectory is finalized
     */
    public final void setFinalizeDrawing() {
        setFlag(isMadeIDX, true);
        setFlag(drawCntlRadIDX, false);
    }

    
    /**
     * Whether or not object is a closed poly
     * @return
     */
    public final boolean getIsClosed() {return getFlag(isClosedIDX);}    
    /**
     * Whether or not object is a closed poly
     * @param _val
     */
    public final void setIsClosed(boolean _val) {setFlag(isClosedIDX, _val);}
    
    /**
     * Whether or not object is finished being drawn
     * @return
     */
    public final boolean getIsMade() {return getFlag(isMadeIDX);}    
    /**
     * Set whether or not object is finished being drawn
     * @param _val
     */
    public final void setIsMade(boolean _val) {setFlag(isMadeIDX, _val);}
    
    /**
     * Whether or not points being displayed are flipped (reflected)
     * @return
     */
    public final boolean getIsFlipped() {return getFlag(isFlippedIDX);}    
    /**
     * Whether or not points being displayed are flipped (reflected)
     * @param _val
     */
    public final void setIsFlipped(boolean _val) {setFlag(isFlippedIDX, _val);}
    
    /**
     * Whether or not this curve is governed by control points (as opposed to just drawn freehand)
     * @return
     */
    public final boolean getUsesCntlPts() {return getFlag(usesCntlPtsIDX);}    
    /**
     * Whether or not this curve is governed by control points (as opposed to just drawn freehand)
     * @param _val
     */
    public final void setUsesCntlPts(boolean _val) {setFlag(usesCntlPtsIDX, _val);}
    
    /**
     * Whether or not we should recalculate points from cntl point radii - use if radius is changed on stroke from user input
     * @return
     */
    public final boolean getRecalcPoints() {return getFlag(reCalcPointsIDX);}    
    /**
     * Whether or not we should recalculate points from cntl point radii - use if radius is changed on stroke from user input
     * @param _val
     */
    public final void setRecalcPoints(boolean _val) {setFlag(reCalcPointsIDX, _val);}
    
    /**
     * Whether the weight impact on cntl radius is inverse or direct - inverse has slow drawing be wider, direct has slow drawing be narrower
     * @return
     */
    public final boolean getCntlWtInvRad() {return getFlag(cntlWInvRadIDX);}    
    /**
     * Whether the weight impact on cntl radius is inverse or direct - inverse has slow drawing be wider, direct has slow drawing be narrower
     * @param _val
     */
    public final void setCntlWtInvRad(boolean _val) {setFlag(cntlWInvRadIDX, _val);}
    
    /**
     * Whether or not control-point based strokes are interpolating
     * @return
     */
    public final boolean getInterpStroke() {return getFlag(interpStrokeIDX);}    
    /**
     * Whether or not control-point based strokes are interpolating
     * @param _val
     */
    public final void setInterpStroke(boolean _val) {setFlag(interpStrokeIDX, _val);}
    
    /**
     * Whether or not to show this object's cntl points
     * @return
     */
    public final boolean getShowCntlPts() {return getFlag(showCntlPntsIDX);}    
    /**
     * Whether or not to show this object's cntl points
     * @param _val
     */
    public final void setShowCntlPts(boolean _val) {setFlag(showCntlPntsIDX, _val);}
    
    /**
     * Whether or not to use vertex normals to shade curve
     * @return
     */
    public final boolean getUseVertNorms() {return getFlag(useVertNormsIDX);}    
    /**
     * Whether or not to use vertex normals to shade curve
     * @param _val
     */
    public final void setUseVertNorms(boolean _val) {setFlag(useVertNormsIDX, _val);}
    
    /**
     * Whether or not to display normals for this object as small arrows
     * @return
     */
    public final boolean getDrawNorms() {return getFlag(drawNormsIDX);}    
    /**
     * Whether or not to display normals for this object as small arrows
     * @param _val
     */
    public final void setDrawNorms(boolean _val) {setFlag(drawNormsIDX, _val);}
    
    /**
     * Whether or not to draw cntrl point rad
     * @return
     */
    public final boolean getDrawCntlRad() {return getFlag(drawCntlRadIDX);}    
    /**
     * Whether or not to draw cntrl point rad
     * @param _val
     */
    public final void setDrawCntlRad(boolean _val) {setFlag(drawCntlRadIDX, _val);}
    
    /**
     * Whether or not we use straight lines in vertex building or processing's curve vertex
     * @return
     */
    public final boolean getUseProcCurve() {return getFlag(useProcCurveIDX);}    
    /**
     * Whether or not we use straight lines in vertex building or processing's curve vertex
     * @param _val
     */
    public final void setUseProcCurve(boolean _val) {setFlag(useProcCurveIDX, _val);}
    
    /**
     * Set or clear debug functionality for flag owner
     */
    @Override
    protected void handleSettingDebug(boolean val) {owner.handleTrajFlagsDebugMode(val);        }

    @Override
    protected void handleFlagSet_Indiv(int idx, boolean val, boolean oldVal) {
        switch(idx){
            case isClosedIDX         : {break;}            //object is a closed poly
            case isMadeIDX             : {break;}            //whether or not the object is finished being drawn
            case isFlippedIDX        : {break;}            //points being displayed are flipped (reflected)
            case usesCntlPtsIDX     : {break;}            //if this curve is governed by control points (as opposed to just drawn freehand)
            case reCalcPointsIDX    : {break;}            //recalculate points from cntl point radii - use if radius is changed on stroke from user input
            case cntlWInvRadIDX        : {break;}            //whether the weight impact on cntl radius is inverse or direct - inverse has slow drawing be wider, direct has slow drawing be narrower
            case interpStrokeIDX    : {break;}            //whether or not control-point based strokes are interpolating or not
            case showCntlPntsIDX     : {break;}            //show this object's cntl points
            case useVertNormsIDX     : {break;}            //use vertex normals to shade curve
            case drawNormsIDX         : {break;}            //display normals for this object as small arrows
            case drawCntlRadIDX     : {break;}
            case useProcCurveIDX    : {break;}            //toggles whether we use straight lines in vertex building or processing's curve vertex            
        }            
    }

}
