package base_Utils_Objects.particleDynamics.particles;

import base_Utils_Objects.particleDynamics.solvers.SolverType;
import base_Utils_Objects.particleDynamics.solvers.Solver;
import base_Utils_Objects.vectorObjs.myVector;
import base_Utils_Objects.vectorObjs.myVectorf;

public class myParticle {
	public final int ID;
	public static int IDgen = 0;

	public myVectorf initPos, initVel;
	//used for spring forces - last iteration's distance vector
	public myVectorf vecLOld;
	//reference to ks and kd values for spring attached to this particle
	public double[] kskdVals;
	//private boolean isSetKsKd;//make sure lowest-frequency zone dominates in ks/kd setting
	//array to keep around 
	public myVectorf[] aPosition, aVelocity, aForceAcc ,aOldPos, aOldVel,aOldForceAcc;

	public int curIDX;
	public static int curNext, curPrev;
	public int colVal;						//collision check value
	
	public static final int szAcc = 2;		//size of accumulator arrays
	
	public float mass = 1,origMass;
	public SolverType solveType;		//{GROUND, EXP_E, MIDPOINT, RK3, RK4, IMP_E, etc }
	public Solver solver;

	public static myVector gravVec = new myVector(0, 0, -9.8);		//derp z is up/down in 3d world TODO move this to some global

	public static final float partRad = 1.0f, tenPartRads = 10.0f * partRad;

	public myParticle(myVectorf _iPos, myVectorf _iVel, myVectorf _iFrc, SolverType _styp) {
		ID = IDgen++;
		init(_iPos, _iVel, _iFrc, _styp);	
	}
	
	private void init(myVectorf _pos, myVectorf _velocity, myVectorf _forceAcc, SolverType _solv) {
		curIDX = 0;									//cycling ptr to idx in arrays of current sim values
		curNext = 1; 
		curPrev = 0;
		aPosition = new myVectorf[szAcc];
		aVelocity = new myVectorf[szAcc];
		aForceAcc = new myVectorf[szAcc];
		aOldPos = new myVectorf[szAcc];
		aOldVel = new myVectorf[szAcc];
		aOldForceAcc = new myVectorf[szAcc];
		for(int i=0;i<szAcc;++i){
			aPosition[i] = new myVectorf();
			aVelocity[i] = new myVectorf();
			aForceAcc[i] = new myVectorf();
			aOldPos[i] = new myVectorf();
			aOldVel[i] = new myVectorf();
			aOldForceAcc[i]	= new myVectorf();	
		}
		aPosition[0].set(_pos);
		aVelocity[0].set(_velocity);
		aForceAcc[0].set(_forceAcc);
		aOldPos[0].set(_pos);
		aOldVel[0].set(_velocity);
		aOldForceAcc[0].set(_forceAcc);
		//vector from old position to rest position
		vecLOld = new myVectorf(0,0,0);
		setOrigMass(mass);
		initPos = new myVectorf(_pos);
		initVel = new myVectorf(_velocity);
		solveType = _solv;
		solver = new Solver( _solv);
		//isSetKsKd = false;
	}

	protected void setOrigMass(float _m) {
		mass = _m;
		origMass = _m;
	}
	//reset position to start position
	public void reset() {
		for(int i=0;i<szAcc;++i){
			aPosition[i].set(initPos);
			aVelocity[i].set(initVel);
			aForceAcc[i].set(0,0,0);
			aOldPos[i].set(initPos);
			aOldVel[i].set(initVel);
			aOldForceAcc[i].set(0,0,0);
		}
	}
	
//	public void setKsKdVals(double[] _zoneKsKdVals) {
//		if(!isSetKsKd) {
//		kskdVals = _zoneKsKdVals;
//		isSetKsKd = true;
//		}
//	}
	
	
//	public static void updateCurPtrs(){
//		curNext = (curIDX + 1) % szAcc; 
//		curPrev = 0;
//		
//	}
	public myVectorf springForce = new myVectorf(0,0,0);
	public void applyForce(myVectorf _force) {aForceAcc[curIDX]._add(_force);}//applyforce
	public void integAndAdvance(double deltaT){		
		//idxs 2 and 3 of tSt hold last iteration's pos and vel
		myVectorf[] tSt = new myVectorf[]{ aPosition[curIDX], aVelocity[curIDX], aOldPos[curIDX], aOldVel[curIDX]};	
		//idxs 2 and 3 of tStDot hold last iteration's vel and frc
		if(springForce.magn >0) {
			applyForce(springForce);
			springForce.set(0,0,0);
		}
		myVectorf[] tStDot = (mass == 1.0f ? 
				new myVectorf[]{ tSt[1],aForceAcc[curIDX],tSt[3], aOldForceAcc[curIDX]} : 
				new myVectorf[]{ tSt[1],myVectorf._div(aForceAcc[curIDX],mass),tSt[3], myVectorf._div(aOldForceAcc[curIDX],mass)});
		myVectorf[] tNSt = solver.Integrate(deltaT, tSt, tStDot);
		
		int oldTopIDX = curIDX;
		curIDX = (curIDX + 1) % szAcc; 
		aOldPos[curIDX].set(aPosition[oldTopIDX]);
		aPosition[curIDX].set(tNSt[0]);
		
		aOldVel[curIDX].set(aVelocity[oldTopIDX]);
		aVelocity[curIDX].set(tNSt[1]);
		
		aOldForceAcc[curIDX].set(aForceAcc[oldTopIDX]);
		aForceAcc[curIDX].set(0,0,0);			//clear out new head of force acc	
		//isSetKsKd = false;
	}
	
	//distance from rest
	public myVectorf delPos() {return myVectorf._sub(aPosition[curIDX], initPos);}
	public float[] delPosAra() {return new float[] {aPosition[curIDX].x - initPos.x, aPosition[curIDX].y - initPos.y, aPosition[curIDX].z - initPos.z};}
	//delta between two particle distances and velocities
	public myVectorf delPos(myParticle b) {return myVectorf._sub(aPosition[curIDX], b.aPosition[curIDX]);}
	public myVectorf delVel(myParticle b) {return myVectorf._sub(aVelocity[curIDX], b.aVelocity[curIDX]);}
	
	@Override
	public String toString(){
		String res = "ID : " + ID + "\tMass:"+mass+"\n";
		res +="\tPosition:"+aPosition[curIDX].toStrBrf()+"\n";
		res +="\tVelocity:"+aVelocity[curIDX].toStrBrf()+"\n";
		res +="\tCurrentForces:"+aForceAcc[curIDX].toStrBrf()+"\n";
		
		return res;		
	}
}//myParticle