package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * first word of a mention
 */
public class HMTemplate extends AbstractFeatureTemplate<HMTemplate.HMScope> {

	public HMTemplate (boolean cache){
		super(cache);
	}



	static class HMScope
			extends AbstractFactorScope {

		EntityType type;
		DocumentToken headword;


		public HMScope(
                AbstractFeatureTemplate<HMScope> template, EntityType type, DocumentToken token) {
			super(template);
			this.type = type;
			this.headword = token;
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
			HMScope hmScope = (HMScope) o;
			return Objects.equals(type, hmScope.type) &&
					Objects.equals(headword, hmScope.headword);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), type, headword);
		}
	}

	@Override
	public List<HMScope> generateFactorScopes(State state) {
		List<HMScope> factors = new ArrayList<>();

		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {

			factors.add(new HMScope(this, annotation.entityType, annotation.relatedTokens.get(0)));

		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<HMScope> factor) {
		factor.getFeatureVector().set(factor.getFactorScope().type.entityName + " " + factor.getFactorScope().headword.getText(), true);

	}

}
