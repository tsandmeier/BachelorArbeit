package tsandmeier.ba.normalizer.interpreter.struct;

import tsandmeier.ba.normalizer.interpreter.struct.ILiteralInterpreter;

public interface INumericInterpreter extends ILiteralInterpreter {

	public double getMeanValue();

	public IUnit getUnit();

	default public boolean isNumeric() {
		return true;
	}
}
