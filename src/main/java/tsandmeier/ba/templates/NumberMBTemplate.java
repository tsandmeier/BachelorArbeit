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
public class NumberMBTemplate extends AbstractFeatureTemplate<NumberMBTemplate.NumberMBScope> {



	static class NumberMBScope
			extends AbstractFactorScope<NumberMBScope> {

		int offsetOne;
		int offsetTwo;

		EntityType typeOne;
		EntityType typeTwo;

		List<DocumentLinkedAnnotation> annotationList;


		public NumberMBScope(
                AbstractFeatureTemplate<NumberMBScope> template, List<DocumentLinkedAnnotation> annotationList, int offsetOne, int offsetTwo,
				EntityType typeOne, EntityType typeTwo) {
			super(template);
			this.annotationList = annotationList;
			this.offsetOne = offsetOne;
			this.offsetTwo = offsetTwo;
			this.typeOne = typeOne;
			this.typeTwo = typeTwo;
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
			NumberMBScope that = (NumberMBScope) o;
			return offsetOne == that.offsetOne &&
					offsetTwo == that.offsetTwo &&
					Objects.equals(typeOne, that.typeOne) &&
					Objects.equals(typeTwo, that.typeTwo) &&
					Objects.equals(annotationList, that.annotationList);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), offsetOne, offsetTwo, typeOne, typeTwo, annotationList);
		}

		@Override
		public boolean implementEquals(Object obj) {
			// TODO Auto-generated method stub
			return false;
		}

	}

	@Override
	public List<NumberMBScope> generateFactorScopes(State state) {
		List<NumberMBScope> factors = new ArrayList<>();

		List<DocumentLinkedAnnotation> annotationList = super.<DocumentLinkedAnnotation>getPredictedAnnotations(state);

		for (DocumentLinkedAnnotation annotation : annotationList) {
			for (DocumentLinkedAnnotation annotation2 : annotationList) {
				int offsetOne = annotation.getStartDocCharOffset();
				int offsetTwo = annotation2.getStartDocCharOffset();
				if(offsetTwo>offsetOne) {
					EntityType typeOne = annotation.entityType;
					EntityType typeTwo = annotation2.entityType;
					factors.add(new NumberMBScope(this, annotationList, offsetOne, offsetTwo, typeOne, typeTwo));
				}
			}
		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<NumberMBScope> factor) {
		int mentionCounter = 0;
		for(DocumentLinkedAnnotation annotation : factor.getFactorScope().annotationList){
			if(annotation.getStartDocCharOffset()>factor.getFactorScope().offsetOne &&
					annotation.getStartDocCharOffset()<factor.getFactorScope().offsetTwo){
				mentionCounter++;
			}
		}
		factor.getFeatureVector().set(factor.getFactorScope().typeOne.entityName+", "+factor.getFactorScope().typeTwo.entityName+ " "+
				mentionCounter,true);

	}

}
