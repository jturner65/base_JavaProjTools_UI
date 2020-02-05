package base_Utils_Objects.particleDynamics.forces;

import base_Utils_Objects.particleDynamics.forces.base.ForceType;
import base_Utils_Objects.particleDynamics.forces.base.baseForce;
import base_Utils_Objects.particleDynamics.particles.myParticle;
import base_Utils_Objects.vectorObjs.myVectorf;

public class ScalarForce extends baseForce{
	// "scalar" force here means we derive the force by a particle-dependent scalar value, in this case mass against gravity vec 
	public ScalarForce(String _n, myVectorf _G) { super(_n, 0 ,0, new myVectorf(_G), ForceType.S_SCALAR);}	//	

	@Override
	//array returns up to 2 forces, one on p1, one on p2
	public myVectorf[] calcForceOnParticle(myParticle _p1, myParticle _p2, double d) {
		myVectorf[] result = new myVectorf[]{new myVectorf(),new myVectorf()};
		result[0] = myVectorf._mult(constVec,_p1.mass);
		return result;
	}
	@Override
	public String toString(){return super.toString() + "\tForce Vector :  " + constVec.toString();}
	
}//scalarForce - scalar body-specific multiple of vector force