package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
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
 * checks if two mentions belong to the same sentence
 */
public class MentionsInSentenceTemplate_FAST extends AbstractFeatureTemplate<MentionsInSentenceTemplate_FAST.MentionsInSentenceScope> {

    String punctuations = ".:!?";

    public MentionsInSentenceTemplate_FAST(boolean cache){
    	super(cache);
	}

    public MentionsInSentenceTemplate_FAST() {
        super();
    }

    static class MentionsInSentenceScope
			extends AbstractFactorScope {

		String entityNameOne;
		String entityNameTwo;



		public MentionsInSentenceScope(
                AbstractFeatureTemplate<MentionsInSentenceScope> template, String entityNameOne, String entityNameTwo) {
			super(template);
			this.entityNameOne = entityNameOne;
			this.entityNameTwo = entityNameTwo;
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
	public List<MentionsInSentenceScope> generateFactorScopes(State state) {
		List<MentionsInSentenceScope> factors = new ArrayList<>();

            Document document = state.getInstance().getDocument();


		List<DocumentLinkedAnnotation> annotations = super.<DocumentLinkedAnnotation>getPredictedAnnotations(state);

		for (int i = 0; i < annotations.size(); i++) {

			final int a1Index = annotations.get(i).getSentenceIndex();

			for (int j = i + 1; j < annotations.size(); j++) {

				final int a2Index = annotations.get(j).getSentenceIndex();

				if (a1Index != a2Index)
					continue;
				factors.add(new MentionsInSentenceScope(this, annotations.get(i).getSurfaceForm(),
						annotations.get(j).getSurfaceForm()));

			}
		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<MentionsInSentenceScope> factor) {

            factor.getFeatureVector().set("Mentions in Same Sentence: " + factor.getFactorScope().entityNameOne +
                    ", " + factor.getFactorScope().entityNameTwo, true);

	}

}
