package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class ML12Template extends AbstractFeatureTemplate<ML12Template.ML12Scope> {


	MaxentTagger tagger = new MaxentTagger(

			"src/main/java/tsandmeier/ba/tagger/english-left3words-distsim.tagger");

	static class ML12Scope
			extends AbstractFactorScope<ML12Scope> {


		public ML12Scope(
                AbstractFeatureTemplate<ML12Scope> template) {
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
	public List<ML12Scope> generateFactorScopes(State state) {
		List<ML12Scope> factors = new ArrayList<>();
		Document doc = state.getInstance().getDocument();
		String tagged = tagger.tagString(doc.documentContent);
		System.out.println(tagged);
//		Document taggedDoc = new Document("taggedDoc", "bal bla");

		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
			EntityType type = annotation.getEntityType();
			for(DocumentToken token: annotation.relatedTokens){
				token.getDocTokenIndex();
			}
			factors.add(new ML12Scope(this));
		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<ML12Scope> factor) {

		// factor.getFeatureVector().set();

	}

}
