package tsandmeier.ba.normalizer;

import de.hterhors.semanticmr.crf.structure.EntityType;
import tsandmeier.ba.normalizer.interpreter.WeightInterpreter;
import tsandmeier.ba.normalizer.interpreter.struct.ILiteralInterpreter;

public class WeightNormalization extends SCIONormalization {

	public WeightNormalization() {
		super(EntityType.get("Weight"));
	}

	@Override
	public ILiteralInterpreter getInterpreter(String surfaceForm) {
		return new WeightInterpreter(surfaceForm);
	}

}
