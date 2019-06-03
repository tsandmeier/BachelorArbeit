package tsandmeier.ba.normalization;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.normalization.AbstractNormalizationFunction;

public class WeightNormalization extends AbstractNormalizationFunction {

	public WeightNormalization() {
		super(EntityType.get("Weight"));
	}

	@Override
	public String normalize(String annotation) {

		return new SemanticWeight.Builder().interprete(annotation).build().normalize().asFormattedString();

//		final String[] parts = annotation.toLowerCase().split("\\W");
//		StringBuilder normalizedString = new StringBuilder();
//		for (String part : parts) {
//			normalizedString.append(part);
//			normalizedString.append(" ");
//		}
//		return normalizedString.toString().trim();
	}

}
