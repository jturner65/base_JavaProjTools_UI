package base_Utils_Objects.particleDynamics.forces;

import base_Utils_Objects.particleDynamics.forces.base.ForceType;
import base_Utils_Objects.particleDynamics.forces.base.baseForce;
import base_Utils_Objects.particleDynamics.particles.myParticle;
import base_Utils_Objects.vectorObjs.myVectorf;


public class TwoBodyForce extends baseForce{
	//attractive/repulsive force
	public TwoBodyForce(String _n, double _k,  ForceType _t) {
		super(_n, _k, 0, new myVectorf(), _t);
	}
	public TwoBodyForce(String _n, double _k) {//passed k > 0 is repulsive force, k < 0 is attractive force
		this(_n, Math.abs(_k), (_k>0) ? ForceType.REPL : ForceType.ATTR);
	}
	@Override
	public myVectorf[] calcForceOnParticle(myParticle _p1, myParticle _p2, double d) {
		myVectorf[] result = new myVectorf[]{new myVectorf(),new myVectorf()};
		myVectorf vecL;
		vecL = new myVectorf(_p2.aPosition[_p2.curIDX],_p1.aPosition[_p1.curIDX]);//vector from 2 to 1
		if (vecL.magn > epsValCalc) {		
			double m1 = _p1.mass, m2 = _p2.mass;
			myVectorf lnorm = myVectorf._normalize(vecL);			//unitlength vector of l
			double fp = constVal1 * m1 * m2 / (vecL.sqMagn);		//from 2 to 1 if constVal > 0 (repulsive force)
			result[0] = myVectorf._mult(lnorm, fp);				//force applied to p1
			result[1] = myVectorf._mult(lnorm, -fp);				//force applied to p2
		}//only add force if magnitude of distance vector is not 0
		return result;
	}	
	@Override
	public String toString(){return super.toString() + "\tForce Scaling Constant :  " + String.format("%.4f",constVal1);}	
}