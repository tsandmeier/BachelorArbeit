package tsandmeier.ba.normalization;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.normalization.AbstractNormalizationFunction;

public class AgeNormalization extends AbstractNormalizationFunction {

	public AgeNormalization() {
		super(EntityType.get("Age"));
	}

	public String normalize(String annotation) {
		final String[] parts = annotation.toLowerCase().split("\\W");
		StringBuilder normalizedString = new StringBuilder();
		for (String part : parts) {
			normalizedString.append(part);
			normalizedString.append(" ");
		}
		return normalizedString.toString().trim();
	}

	@Override
	public String interprete(String s) {
		return null;
	}
}
