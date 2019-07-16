package tsandmeier.ba.NutzloseTemplates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.State;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class EmptyTemplate extends AbstractFeatureTemplate<EmptyTemplate.EmptyScope> {



	static class EmptyScope
			extends AbstractFactorScope {




		public EmptyScope(
                AbstractFeatureTemplate<EmptyScope> template) {
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
	public List<EmptyScope> generateFactorScopes(State state) {
		List<EmptyScope> factors = new ArrayList<>();

		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {

			factors.add(new EmptyScope(this));

		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<EmptyScope> factor) {

		// factor.getFeatureVector().set();

	}

}
