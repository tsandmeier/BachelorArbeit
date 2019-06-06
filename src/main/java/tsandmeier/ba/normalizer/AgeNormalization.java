package tsandmeier.ba.normalizer;

import de.hterhors.semanticmr.crf.structure.EntityType;
import tsandmeier.ba.normalizer.interpreter.AgeInterpreter;
import tsandmeier.ba.normalizer.interpreter.struct.ILiteralInterpreter;

public class AgeNormalization extends SCIONormalization {

	public AgeNormalization() {
		super(EntityType.get("Age"));
	}

	@Override
	public ILiteralInterpreter getInterpreter(String surfaceForm) {
		return new AgeInterpreter(surfaceForm);
	}

}
