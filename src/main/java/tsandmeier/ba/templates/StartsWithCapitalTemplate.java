package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class StartsWithCapitalTemplate extends AbstractFeatureTemplate<StartsWithCapitalTemplate.StartsWithCapitalScope> {



	static class StartsWithCapitalScope
			extends AbstractFactorScope<StartsWithCapitalScope> {


		final public String surfaceForm;
		final public EntityType type;


		public StartsWithCapitalScope(
                AbstractFeatureTemplate<StartsWithCapitalScope> template, EntityType type, String surfaceForm) {
			super(template);
			this.surfaceForm = surfaceForm;
			this.type = type;
		}

		@Override
		public int implementHashCode() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean implementEquals(Object obj) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;
			StartsWithCapitalScope that = (StartsWithCapitalScope) o;
			return Objects.equals(surfaceForm, that.surfaceForm) &&
					Objects.equals(type, that.type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), surfaceForm, type);
		}
	}

	@Override
	public List<StartsWithCapitalScope> generateFactorScopes(State state) {
		List<StartsWithCapitalScope> factors = new ArrayList<>();

		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {

			String surfaceForm = annotation.getSurfaceForm();
			EntityType type = annotation.getEntityType();
			factors.add(new StartsWithCapitalScope(this, type, surfaceForm));

		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<StartsWithCapitalScope> factor) {

		String surfaceForm = factor.getFactorScope().surfaceForm;
		EntityType type = factor.getFactorScope().type;

		if(surfaceForm.length()>=1) {
			factor.getFeatureVector().set(type.entityName + " " + Character.isUpperCase(surfaceForm.charAt(0)), true);
		}
	}

}
