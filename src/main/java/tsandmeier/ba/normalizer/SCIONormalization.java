package tsandmeier.ba.normalizer;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.normalization.AbstractNormalizationFunction;
import tsandmeier.ba.normalizer.interpreter.struct.ILiteralInterpreter;

abstract public class SCIONormalization extends AbstractNormalizationFunction {

	public SCIONormalization(EntityType entityType) {
		super(entityType);
	}

	@Override
	public String interprete(String annotation) {

		final ILiteralInterpreter wi = getInterpreter(annotation);

		if (wi.isInterpretable())
			return wi.normalize().asFormattedString();
		else
			return annotation;

	}

	abstract public ILiteralInterpreter getInterpreter(String surfaceForm);

}
