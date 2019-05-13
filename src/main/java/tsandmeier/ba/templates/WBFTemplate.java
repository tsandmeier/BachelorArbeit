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
public class WBFTemplate extends AbstractFeatureTemplate<WBFTemplate.WBFScope> {

	static class WBFScope
			extends AbstractFactorScope<WBFScope> {

		public String wordAfter;
		public int indexWordOne;
		public int indexWordTwo;

		public EntityType typeOne;
		public EntityType typeTwo;

		public WBFScope(
                AbstractFeatureTemplate<WBFScope> template, int indexOne, int indexTwo, EntityType typeOne, EntityType typeTwo, String token) {
			super(template);
			this.indexWordOne = indexOne;
			this.indexWordTwo = indexTwo;
			this.typeOne = typeOne;
			this.typeTwo = typeTwo;
			this.wordAfter = token;
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
			return indexWordOne == wbfScope.indexWordOne &&
					indexWordTwo == wbfScope.indexWordTwo &&
					Objects.equals(wordAfter, wbfScope.wordAfter) &&
					Objects.equals(typeOne, wbfScope.typeOne) &&
					Objects.equals(typeTwo, wbfScope.typeTwo);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), wordAfter, indexWordOne, indexWordTwo, typeOne, typeTwo);
		}
	}

	@Override
	public List<WBFScope> generateFactorScopes(State state) {
		List<WBFScope> factors = new ArrayList<>();

		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
			for (DocumentLinkedAnnotation annotation2 : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)){
				if(!annotation.equals(annotation2)) {
					factors.add(new WBFScope(this, annotation.relatedTokens.get(annotation.relatedTokens.size() - 1).getDocTokenIndex(), annotation2.relatedTokens.get(0).getDocTokenIndex(), annotation.entityType, annotation2.entityType, state.getInstance().getDocument().tokenList.get(annotation.relatedTokens.get(annotation.relatedTokens.size() - 1).getDocTokenIndex() + 1).getText()));
				}
			}
		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<WBFScope> factor) {
		if(Math.abs(factor.getFactorScope().indexWordTwo-factor.getFactorScope().indexWordOne)>1) {
			factor.getFeatureVector().set(factor.getFactorScope().typeOne + factor.getFactorScope().wordAfter, true);
			factor.getFeatureVector().set(factor.getFactorScope().typeTwo + factor.getFactorScope().wordAfter, true);
		}
	}

}
