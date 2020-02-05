package base_Utils_Objects.interpolants;

import base_Utils_Objects.MyMathUtils;
import base_Utils_Objects.interpolants.base.baseInterpolant;

/**
 * interpolant from 1/2 period of sine
 * @author john
 *
 */
public class Sine_Interpolant extends baseInterpolant {

	public Sine_Interpolant(float _t) {		super(_t);	}
	public Sine_Interpolant(float _t, float _stopTimer) {super(_t,_stopTimer);}

	@Override
	protected float calcInterpolant_Indiv(float _rawt) {	
		return .5f*(1.0f + (float) Math.sin(MyMathUtils.halfPi *((_rawt * 2.0f) - 1.0f)));
	}

}
