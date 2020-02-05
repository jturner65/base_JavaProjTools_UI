package base_Utils_Objects.particleDynamics.forces.base;

import base_Utils_Objects.particleDynamics.particles.myParticle;
import base_Utils_Objects.vectorObjs.myVectorf;

public abstract class baseForce {
	public final float epsValCalc = .00000001f;
	public static int ID_gen = 0;
	public int ID;
	public String name;
	public double constVal1;				//multiplicative constant to be applied to mass to find force/ ks
	public double constVal2;
	public myVectorf constVec;				//vector constant quantity, for use with gravity
	public ForceType ftype;

	public baseForce(String _n, double _k1, double _k2, myVectorf _constVec, ForceType _t){
		ID = ++ID_gen;
		name = new String(_n);
		constVal1 = _k1; 
		constVal2 = _k2;
		constVec = _constVec;		//torque-result force
		ftype = _t;
	}
	public baseForce(String _n, double _k1, double _k2) {this( _n, _k1, _k2, new myVectorf(), ForceType.DAMPSPRING);}
	public baseForce(String _n, double _k) {this( _n, _k * (_k>0 ? 1 : -1), 0, new myVectorf(), (_k>0) ? ForceType.REPL : ForceType.ATTR); ID = -1;}
	
	public void setConstVal1(double _c) {constVal1 = _c;}
	public void setConstVal2(double _c) {constVal2 = _c;}	
	
	public abstract myVectorf[] calcForceOnParticle(myParticle _p1, myParticle _p2, double d);// {S_SCALAR,S_VECTOR, ATTR, SPRING};
	@Override
	public String toString(){return "Force Name : " + name + " ID : " + ID + " Type : " + ftype.getName();}
}//myForce class