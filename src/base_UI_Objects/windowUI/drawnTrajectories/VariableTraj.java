package base_UI_Objects.windowUI.drawnTrajectories;

import java.util.ArrayList;

import base_Math_Objects.vectorObjs.doubles.myPoint;
import base_Math_Objects.vectorObjs.doubles.myVector;
import base_Render_Interface.IGraphicsAppInterface;
import base_UI_Objects.windowUI.base.Base_DispWindow;
import base_UI_Objects.windowUI.drawnTrajectories.base.Base_DrawnTrajectory;

/**
 * Trajectory curve
 * @author John Turner
 *
 */
public class VariableTraj extends Base_DrawnTrajectory {
 
    protected final int numVerts = 200;                            

    public myPoint[] drawnCntlPts;                        //control point locations - start point +  vel myVectortor (scaled tangent), repeat
    public double[] d_drwnCntlPts;                    //distance between each drawnCntlPts points
    public double drwnCntlLen;                        //len of arc of drawnCntlPts pts

    public double[] cntlPtIntrps;                //interpolants for each control point as part of total, based upon radius of controlpoint - larger will be bigger. 
                                                //for each cntl myPoint, this value will be cntl point rad / total control point rads.
    //interpolated drawn curve, weighted by drawn speed
    public myPoint[] interpCntlPts;                    //interpolated control point locations - start point +  vel myVectortor (scaled tangent), repeat
    public double[] d_interpPts;                    //distance between interpolated points
    public double interpLen;                        //len of interp pts
    
    public VariableTraj(Base_DispWindow _win, myVector _canvNorm, int[] _fillClr, int[] _strkClr) {
        super(_win, _canvNorm);
        fillClr = _fillClr;
        strkClr= _strkClr;
        interpCntlPts = new myPoint[0];
    }

    /**
     * as drawing, add points to -cntlPoints-, not pts array.
     */
    @Override
    public void addPt(myPoint p){
        addCntlPt(p);
    }//
    
    /**
     * Instance-class specific finalizing
     * @param procPts
     */    
    @Override
    public void finalizeDrawing_Priv(boolean procPts){
        buildInterpAra();
    }//finalize
    
    /**
     * build interpolants based upon weight of each cntl point.  these can be used to determine how far the edge moves (velocity) 
     * and how much it rotates per frame. when this is done, we have multipliers to apply to each tangent myVectortor to 
     * determine displacement for each of the frames
     */
    public void buildInterpAra(){
        cntlPtIntrps = new double[numIntCntlPts];            //interpolant from each control point - weights
        
        double[] cmyPointInterps = new double[numIntCntlPts];
        interpCntlPts = new myPoint[numIntCntlPts];
        drawnCntlPts = new myPoint[numIntCntlPts];
        float sumWts = 0;
        for(int i=0;i<cntlPts.length;++i){sumWts += cntlPts[i].w;cmyPointInterps[i] = cntlPts[i].w;cntlPtIntrps[i]=sumWts;}
        //for(int i=0;i<cntlPts.length;++i){sumWts += cntlPts[i].w;cntlPtIntrps[i]=cntlPts[i].w;}
        //System.out.println("total weight = " + sumWts);
        for(int i=0;i<cntlPts.length;++i){cntlPtIntrps[i]/=sumWts;cmyPointInterps[i]/=sumWts;}
        //smooth interpolants now
        cntlPtIntrps = dualDoubles(cntlPtIntrps);
        cmyPointInterps = dualDoubles(cmyPointInterps);
        
        interpCntlPts[0] = new myPoint(cntlPts[0]);            //set first point
        drawnCntlPts[0] = new myPoint(cntlPts[0]);
        double distStToEnd = myPoint._dist(cntlPts[0], cntlPts[cntlPts.length-1]);            //distance from first to last point
        
        for(int i=1;i<cntlPts.length;++i){
            interpCntlPts[i] = new myPoint(interpCntlPts[i-1],myVector._mult( c_tAra[i],distStToEnd * cmyPointInterps[i]));        
            drawnCntlPts[i] = new myPoint(cntlPts[i]);
        }    
        for(int i= cntlPts.length; i<numIntCntlPts; ++i){
            interpCntlPts[i] = new myPoint(interpCntlPts[i-1],myVector._mult(c_tAra[c_tAra.length-1],distStToEnd * cmyPointInterps[i]));        
            drawnCntlPts[i] = new myPoint(cntlPts[cntlPts.length-1]);
            
        }
        d_interpPts = getPtDist(interpCntlPts, false);    
        interpLen = length(interpCntlPts, false);
        d_drwnCntlPts = getPtDist(drawnCntlPts, false);    
        drwnCntlLen = length(drawnCntlPts, false);        
        //smooth/equi-space interpolated cntl points
        processInterpPts( numIntCntlPts, numReps);
    }    
    
    /**
     * subdivide, tuck, respace, resample, etc. pts of this curve
     * @param numPts
     * @param numReps
     */
    public void processInterpPts(int numPts, int numReps){
        //setInterpPts(procInterpPts(_subdivide, interpCntlPts, 2, interpLen));                                        //makes 1 extra vert  equilspaced between each vert, to increase resolution of curve
        for(int i = 0; i < numReps; ++i){
            if(i % 2 == 0){setInterpPts(procPts(_subdivide, interpCntlPts, 2, interpLen, false));}
            setInterpPts(procPts(_tuck, interpCntlPts, .5f, interpLen, false));
            setInterpPts(procPts(_tuck, interpCntlPts, -.5f, interpLen, false));
        }        //smooth curve - J4
        setInterpPts(procPts(_equaldist, interpCntlPts, .5f, interpLen, false));
        for(int i = 0; i < numReps; ++i){
            //if(i % 2 == 0){setInterpPts(procInterpPts(_subdivide, interpCntlPts, 2, interpLen));}
            setInterpPts(procPts(_tuck, interpCntlPts, .5f, interpLen, false));
            setInterpPts(procPts(_tuck, interpCntlPts, -.5f, interpLen, false));
        }        //smooth curve - J4
        setInterpPts(procPts(_resample, interpCntlPts, numPts, interpLen, false));    
    }    
    
    /**
     * return appropriate ara of points based on using velocities or not
     * @param useVels
     * @return
     */
    public myPoint[] getDrawnPtAra(boolean useVels){    return (useVels ? interpCntlPts : cntlPts);}
    
    /**
     * remake drawn trajectory after edit
     */
    public void remakeDrawnTraj(boolean useVels){
        if(useVels){
            for(int i = 0; i < 10; ++i){
                if(i % 2 == 0){setInterpPts(procPts(_subdivide, interpCntlPts, 2, interpLen, false));}
                setInterpPts(procPts(_tuck, interpCntlPts, .5f, interpLen, false));
                setInterpPts(procPts(_tuck, interpCntlPts, -.49f, interpLen, false));
            }        //smooth curve - J4
            setInterpPts(procPts(_equaldist, interpCntlPts, .5f, interpLen, false));
            setInterpPts(procPts(_resample, interpCntlPts, numIntCntlPts, interpLen, false));    
        } else {
            super.remakeDrawnTraj(useVels);
        }    
    }//remakeDrawnTraj
    
    /**
     * sets required info for points array - points and dist between pts, length, etc
     * @param tmp
     */
    protected void setInterpPts(ArrayList<myPoint> tmp){
        interpCntlPts = tmp.toArray(new myPoint[0]);
        d_interpPts = getPtDist(interpCntlPts, false);    
        interpLen=length(interpCntlPts, false);
    }//setPts    

    /**
     * tuck untuck double values
     * @param src
     * @return
     */
    public double[] dualDoubles(double[] src){
        double[] res = new double[src.length],res1 = new double[src.length];
        res1[0]=src[0];
        res1[src.length-1]=src[src.length-1];
        for(int i=1; i<src.length-1;++i){
            res1[i]=_Interp(src[i],.5,_Interp(src[i-1],.5,src[i+1],lnI_Typ),lnI_Typ);
        }
        res[0]=res1[0];
        res[src.length-1]=res1[src.length-1];
        for(int i=1; i<res1.length-1;++i){
            res[i]=_Interp(res1[i],-.5,_Interp(res1[i-1],.5,res1[i+1],lnI_Typ),lnI_Typ);
        }            
        return res;        
    }

    public myPoint at_I(double t){return at(t,new double[1], interpLen, interpCntlPts, d_interpPts);}//put interpolant between adjacent points in s ara if needed
    public myPoint at_I(double t, double[] s){    return at(t,s, interpLen, interpCntlPts, d_interpPts);}//put interpolant between adjacent points in s ara if needed    

    
    /**
     * Debug : print out all trajectory point locations for debugging
     * @param useDrawnVels
     */
    @Override
    public void dbgPrintAllPoints(boolean useDrawnVels){
        if(useDrawnVels){
            win.getMsgObj().dispInfoMessage("myVariStroke", "dbgPrintAllPoints","Drawn Vels Traj :\n");
            for(int i = 0; i < interpCntlPts.length; ++i){
                win.getMsgObj().dispInfoMessage("myVariStroke", "dbgPrintAllPoints","\tpt " + i +" : " + interpCntlPts[i].toStrBrf());
            }
        } else {
            super.dbgPrintAllPoints(useDrawnVels);
        }
    }
    
    /**
     * Draw this variable trajectory
     * @param ri
     * @param useDrawnVels
     * @param flat
     */
    @Override
    public void drawMe(IGraphicsAppInterface ri, boolean useDrawnVels, boolean flat){
        ri.pushMatState();
            ri.setFill(fillClr,255);
            ri.setStroke(strkClr,255);
            ri.setStrokeWt(1.0f);
            if(useDrawnVels){
                if(flat) {
                    for(int i = 0; i < interpCntlPts.length; ++i){
                        ri.showPtAsCircle(interpCntlPts[i],trajPtRad,-1,-1);
                    }                
                } else {
                    for(int i = 0; i < interpCntlPts.length; ++i){
                        ri.showPtAsSphere(interpCntlPts[i],trajPtRad,5,-1,-1);
                    }
                }                
                if(trajFlags.getDrawCntlRad()){
                    for(int i = 0; i < interpCntlPts.length; ++i){
                        ri.drawCircle3D(interpCntlPts[i], cntlPts[i].r,c_bAra[i], c_tAra[i],20);
                    }
                }            
            } else {
                super.drawMe(ri, useDrawnVels, flat);
            }
            ri.popMatState();        
    }//
    
    /**
     * Move velocity curve to new end points
     * @param startPt
     * @param endPt
     * @param flip
     */
    public void moveVelCurveToEndPoints(myPoint startPt, myPoint endPt, boolean flip){
        _moveCurveToEndPoints(interpCntlPts, startPt, endPt, flip);
    }//moveVelCurveToEndPoints
    


    
    public String toString(){
        String res = "Interpolating Spline Stroke : ";
        res += "\n"+super.toString();
        return res;    
    }
    
}//myVariStroke
