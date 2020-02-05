package base_Utils_Objects.particleDynamics.forces;

import base_Utils_Objects.particleDynamics.forces.base.ForceType;
import base_Utils_Objects.particleDynamics.forces.base.baseForce;
import base_Utils_Objects.particleDynamics.particles.myParticle;
import base_Utils_Objects.vectorObjs.myVectorf;

//spring force to rest position
public class SpringToRestPos extends baseForce{
	//damped spring
	public SpringToRestPos(String _n, double _k, double _k2,  ForceType _t) {super(_n, _k, _k2, new myVectorf(), _t);	}
	public SpringToRestPos(String _n, double _k,double _k2) {this(_n, _k, _k2, ForceType.DAMPSPRING);}
	
	//_p2 should be null, d should be 0, since we have it hardcoded to be to initPos
	@Override
	public myVectorf[] calcForceOnParticle(myParticle _p1, myParticle _p2, double d) {
		myVectorf[] result; //= new myVectorf[]{new myVectorf(),new myVectorf()};
		myVectorf vecL = new myVectorf(_p1.initPos,_p1.aPosition[_p1.curIDX]);//vector from current position to init position
		if (vecL.magn > epsValCalc) {		
			myVectorf lnorm = myVectorf._normalize(vecL);//unitlength vector of l
			myVectorf lprime = myVectorf._sub(vecL, _p1.vecLOld);		//lprime - time derivative of length, subtract old length vector from new length vector ?
			double KsTerm = constVal1 * (vecL.magn);//-d);
			double KdTerm = constVal2 * (lprime._dot(lnorm));//was _dot(vecL) ->should be component in direction of normal TODO verify

//			double KsTerm = _p1.kskdVals[0] * (vecL.magn);//-d);
//			double KdTerm = _p1.kskdVals[1] * (lprime._dot(lnorm));//was _dot(vecL) ->should be component in direction of normal TODO verify
			double fp = (KsTerm + KdTerm);
			result = new myVectorf[] {myVectorf._mult(lnorm,-fp),myVectorf._mult(lnorm, fp)};
		} else {//only add force if magnitude of distance vector is not 0
			//if disp is very small, move to original position, return 0 force 
			_p1.aPosition[_p1.curIDX].set(_p1.initPos);
			result = new myVectorf[]{new myVectorf(),new myVectorf()};
		}
		_p1.vecLOld.set(vecL);
		return result;
	}	
	@Override
	public String toString(){return super.toString() + "\tSpring Constant :  " + String.format("%.2f",constVal1) + " \tDamping Constant : "+String.format("%.2f",constVal2) ;}	
}//SpringToRestPos