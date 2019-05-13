package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.State;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class MentionsInSentenceTemplate extends AbstractFeatureTemplate<MentionsInSentenceTemplate.MentionsInSentenceScope> {

	static class MentionsInSentenceScope
			extends AbstractFactorScope<MentionsInSentenceScope> {


		public MentionsInSentenceScope(
				AbstractFeatureTemplate<MentionsInSentenceScope> template) {
			super(template);
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
	public List<MentionsInSentenceScope> generateFactorScopes(State state) {
		List<MentionsInSentenceScope> factors = new ArrayList<>();

		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {

			factors.add(new MentionsInSentenceScope(this));

		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<MentionsInSentenceScope> factor) {

		factor.getFeatureVector().set("EMPTY", true);

	}

}
