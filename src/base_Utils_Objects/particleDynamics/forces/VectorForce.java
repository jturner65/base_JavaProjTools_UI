package base_Utils_Objects.particleDynamics.forces;

import base_Utils_Objects.particleDynamics.forces.base.ForceType;
import base_Utils_Objects.particleDynamics.forces.base.baseForce;
import base_Utils_Objects.particleDynamics.particles.myParticle;
import base_Utils_Objects.vectorObjs.myVectorf;


public class VectorForce extends baseForce{
	//vector here means we derive the force as a particle-dependent vector value, like velocity, against some scalar kd
	public VectorForce(String _n, double _k) { super(_n,_k,0, new myVectorf(), ForceType.S_VECTOR);}		//if drag, needs to be negative constant value	

	@Override
	public myVectorf[] calcForceOnParticle(myParticle _p1, myParticle _p2, double d) {
		myVectorf[] result = new myVectorf[]{new myVectorf(),new myVectorf()};
		result[0] = myVectorf._mult(_p1.aVelocity[_p1.curIDX], constVal1);//vector here means we derive the force as a particle-dependent vector value, velocity, against some scalar kd 
		return result;
	}
	@Override
	public String toString(){return super.toString() + "\tForce Scaling Constant :  " + String.format("%.4f",constVal1);}
	
}//vectorForce - vector body-specific quantity multiplied by scalar constant