package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * bag of words
 */
public class WMTemplate extends AbstractFeatureTemplate<WMTemplate.EmptyScope> {



	static class EmptyScope
			extends AbstractFactorScope<EmptyScope> {

		EntityType type;
		List<DocumentToken> tokens;




		public EmptyScope(
				AbstractFeatureTemplate<EmptyScope> template, EntityType type, List<DocumentToken> tokens) {
			super(template);
			this.type = type;
			this.tokens = tokens;
		}

		@Override
		public int implementHashCode() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;
			EmptyScope that = (EmptyScope) o;
			return Objects.equals(type, that.type) &&
					Objects.equals(tokens, that.tokens);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), type, tokens);
		}

		@Override
		public boolean implementEquals(Object obj) {
			// TODO Auto-generated method stub
			return false;
		}

	}

	@Override
	public List<EmptyScope> generateFactorScopes(State state) {
		List<EmptyScope> factors = new ArrayList<>();

		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {

			factors.add(new EmptyScope(this, annotation.getEntityType(), annotation.relatedTokens));

		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<EmptyScope> factor) {


		factor.getFeatureVector().set(factor.getFactorScope().type.entityName+ " " +
				makeString(factor.getFactorScope().tokens),true);

	}

	private String makeString (List<DocumentToken> tokens){
		String output = "";
		for (DocumentToken token : tokens){
			if(!(token.isPunctuation()))
			output = output + token.getText() + " ";
		}
		return output.trim();
	}

}
