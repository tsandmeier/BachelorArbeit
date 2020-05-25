package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * checks the number of words between two mentions
 */
public class NumberWBTemplate_FAST extends AbstractFeatureTemplate<NumberWBTemplate_FAST.NumberWBScope> {

	public NumberWBTemplate_FAST(boolean cache){super(cache);}

	static class NumberWBScope
			extends AbstractFactorScope {

		String entityNameOne;
		String entityNameTwo;

		int wordsBetween;



		public NumberWBScope(
                AbstractFeatureTemplate<NumberWBScope> template, String entityNameOne, String entityNameTwo, int wordsBetween) {
			super(template);
			this.entityNameOne = entityNameOne;
			this.entityNameTwo = entityNameTwo;

			this.wordsBetween = wordsBetween;
		}

		@Override
		public int implementHashCode() {
			return 0;
		}


		@Override
		public boolean implementEquals(Object obj) {
			return false;
		}
	}

	@Override
	public List<NumberWBScope> generateFactorScopes(State state) {

		List<NumberWBScope> factors = new ArrayList<>();


		List<DocumentLinkedAnnotation> annotationList = super.getPredictedAnnotations(state);


		for(int i = 0; i < annotationList.size(); i++) {

			int indexOne = annotationList.get(i).relatedTokens.get(annotationList.get(i).relatedTokens.size() - 1).getDocTokenIndex();

			for (int j = i + 1; j < annotationList.size(); j++) {

				int indexTwo = annotationList.get(i).relatedTokens.get(0).getDocTokenIndex();

				if (indexTwo > indexOne) {

					factors.add(new NumberWBTemplate_FAST.NumberWBScope(this, annotationList.get(i).getEntityType().name,
							annotationList.get(j).entityType.name, indexTwo - indexOne));

				} else {

					factors.add(new NumberWBTemplate_FAST.NumberWBScope(this, annotationList.get(j).getEntityType().name,
							annotationList.get(i).entityType.name, indexOne - indexTwo));

				}

			}
		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<NumberWBScope> factor) {


			factor.getFeatureVector().set("Words between <" + factor.getFactorScope().entityNameOne + ", "+
					factor.getFactorScope().entityNameTwo + "> : " + factor.getFactorScope().wordsBetween, true);

	}
}