package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
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
public class BracketsTemplate extends AbstractFeatureTemplate<BracketsTemplate.BracketsScope> {



	static class BracketsScope
			extends AbstractFactorScope {

		private final EntityType type;
		String surface;
		char firstChar;
		char lastChar;


		public BracketsScope(
                AbstractFeatureTemplate<BracketsScope> template, char firstChar, char lastChar, String surface, EntityType type) {
			super(template);
			this.firstChar = firstChar;
			this.lastChar = lastChar;
			this.surface = surface;
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
			BracketsScope that = (BracketsScope) o;
			return firstChar == that.firstChar &&
					lastChar == that.lastChar &&
					Objects.equals(type, that.type) &&
					Objects.equals(surface, that.surface);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), type, surface, firstChar, lastChar);
		}
	}

	@Override
	public List<BracketsScope> generateFactorScopes(State state) {
		List<BracketsScope> factors = new ArrayList<>();

		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {

			String surface = annotation.getSurfaceForm();
			EntityType type = annotation.getEntityType();

			if(annotation.document.documentContent.length() > annotation.getEndDocCharOffset()+1 && annotation.getStartDocCharOffset()>0) {
				char firstChar = annotation.document.documentContent.charAt(annotation.getStartDocCharOffset() - 1);
				char lastChar = annotation.document.documentContent.charAt(annotation.getEndDocCharOffset());

				factors.add(new BracketsScope(this, firstChar, lastChar, surface, type));
			}
		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<BracketsScope> factor) {

		boolean isInBrackets = isBracket(factor.getFactorScope().firstChar) && isBracket(factor.getFactorScope().lastChar);


		factor.getFeatureVector().set("In Brackets: " + factor.getFactorScope().type.entityName + " " +
				isInBrackets, true);
	}

	private boolean isBracket (char letter){
		return (letter == '(' ||
				letter == ')' ||
				letter == '{' ||
				letter == '}');
	}

}
