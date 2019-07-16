package tsandmeier.ba.NutzloseTemplates;

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
 * checks wether the surfaceForm of an Annotation coould contain an abbreviation of the EntityName
 *
 */

public class AbbreviationTemplate extends AbstractFeatureTemplate<AbbreviationTemplate.EmptyScope> {

	public AbbreviationTemplate(boolean cache){
		super(cache);
	}

	static class EmptyScope
			extends AbstractFactorScope {

		EntityType type;
		List<DocumentToken> tokens;

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

		public EmptyScope(AbstractFeatureTemplate template, EntityType type, List<DocumentToken> tokens) {
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

		for(DocumentToken token: factor.getFactorScope().tokens){
			if(token.getLength() <= (factor.getFactorScope().type.entityName.length()/3)){
				factor.getFeatureVector().set("<" + factor.getFactorScope().type.entityName +"> contains abbreviation: " + token.getText(), true);
			}
		}

	}

}
