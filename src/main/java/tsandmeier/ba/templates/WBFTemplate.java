package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.exce.DocumentLinkedAnnotationMismatchException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class WBFTemplate extends AbstractFeatureTemplate<WBFTemplate.WBFScope> {

	static class WBFScope
			extends AbstractFactorScope<WBFScope> {

		public String wordAfter;
		public DocumentToken tokenOne;
		public DocumentToken tokenTwo;

		public EntityType typeOne;
		public EntityType typeTwo;

		public Document document;

		public WBFScope(
				AbstractFeatureTemplate<WBFScope> template, DocumentToken tokenOne, DocumentToken tokenTwo,
				EntityType typeOne, EntityType typeTwo, Document document) {
			super(template);
			this.tokenOne = tokenOne;
			this.tokenTwo = tokenTwo;
			this.typeOne = typeOne;
			this.typeTwo = typeTwo;
			this.document = document;
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
			WBFScope wbfScope = (WBFScope) o;
			return tokenOne == wbfScope.tokenOne &&
					tokenTwo == wbfScope.tokenTwo &&
					Objects.equals(wordAfter, wbfScope.wordAfter) &&
					Objects.equals(typeOne, wbfScope.typeOne) &&
					Objects.equals(typeTwo, wbfScope.typeTwo);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), wordAfter, tokenOne, tokenTwo, typeOne, typeTwo);
		}
	}

	@Override
	public List<WBFScope> generateFactorScopes(State state) {
		List<WBFScope> factors = new ArrayList<>();
		Document document = state.getInstance().getDocument();

		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
			for (DocumentLinkedAnnotation annotation2 : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)){
				if(!annotation.equals(annotation2)) {
					try {
						factors.add(new WBFScope(this,
								document.getTokenByCharOffset(annotation.documentPosition.docCharOffset),
								document.getTokenByCharOffset(annotation2.documentPosition.docCharOffset),
								annotation.entityType, annotation2.entityType,
								document));
					} catch (DocumentLinkedAnnotationMismatchException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<WBFScope> factor) {
		String subtext;
		if (factor.getFactorScope().tokenOne.getDocCharOffset()<factor.getFactorScope().tokenTwo.getDocCharOffset()) {
			subtext = factor.getFactorScope().document.getContent(
					factor.getFactorScope().tokenOne, factor.getFactorScope().tokenTwo
			);

			//factor.getFeatureVector().set(factor.getFactorScope().typeOne.entityName + " " + factor.getFactorScope().typeTwo.entityName + " " + numberOfWords(subtext), true);
			if (numberOfWords(subtext)<=5) {
				factor.getFeatureVector().set(factor.getFactorScope().typeOne.entityName + " "
						+ factor.getFactorScope().typeTwo.entityName + " "
						+subtext, true);
			}
		}
	}

	public int numberOfWords(String input) {
		if (input == null || input.isEmpty()) {
			return 0;
		}

		String[] words = input.split("\\s+");
		return words.length;
	}
}
