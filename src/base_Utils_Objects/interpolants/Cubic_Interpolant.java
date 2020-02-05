package base_Utils_Objects.interpolants;

import base_Utils_Objects.interpolants.base.baseInterpolant;

/**
 * hermitian blending func with continuous vel
 * @author john
 *
 */
public class Cubic_Interpolant extends baseInterpolant {

	public Cubic_Interpolant(float _t) {super(_t);}
	public Cubic_Interpolant(float _t, float _stopTimer) {super(_t,_stopTimer);}

	@Override
	protected float calcInterpolant_Indiv(float _rawt) {	return (_rawt * _rawt * (3.0f - (2.0f*_rawt)));}

}//class Hermite_Interpolant
