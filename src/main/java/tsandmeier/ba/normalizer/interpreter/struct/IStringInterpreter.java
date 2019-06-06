package tsandmeier.ba.normalizer.interpreter.struct;

public interface IStringInterpreter extends ILiteralInterpreter {


	default public boolean isNumeric() {
		return false;
	}
}
