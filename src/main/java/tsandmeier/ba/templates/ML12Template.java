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

import javax.print.Doc;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class ML12Template extends AbstractFeatureTemplate<ML12Template.ML12Scope> {

	Document taggedDocument;
	boolean docIsTagged = false;

	MaxentTagger tagger = new MaxentTagger(

			"src/main/java/tsandmeier/ba/tagger/english-left3words-distsim.tagger");

	static class ML12Scope
			extends AbstractFactorScope<ML12Scope> {

		DocumentToken taggedToken;
		DocumentToken phraseToken;

		EntityType type;


		public ML12Scope(
                AbstractFeatureTemplate<ML12Scope> template, DocumentToken token, DocumentToken phraseToken, EntityType type) {
			super(template);
			this.taggedToken = token;
			this.type = type;
			this.phraseToken = phraseToken;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;
			ML12Scope ml12Scope = (ML12Scope) o;
			return Objects.equals(taggedToken, ml12Scope.taggedToken) &&
					Objects.equals(type, ml12Scope.type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), taggedToken, type);
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
		if(!docIsTagged) {
			Document doc = state.getInstance().getDocument();
			String tagged = tagger.tagString(doc.documentContent);
//			System.out.println(".");
			taggedDocument = new Document("taggedDoc", tagged);
			//System.out.println(taggedDocument.documentContent);
			docIsTagged = true;
		}
		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
			EntityType type = annotation.getEntityType();
			for(DocumentToken token: annotation.relatedTokens){
					if((token.getDocTokenIndex()*2)+1<=taggedDocument.tokenList.size()) {
						DocumentToken taggedToken = taggedDocument.tokenList.get(token.getDocTokenIndex() * 2);
						DocumentToken phrase = taggedDocument.tokenList.get((token.getDocTokenIndex() * 2)+1);
						factors.add(new ML12Scope(this, taggedToken, phrase, type));
					}
			}
		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<ML12Scope> factor) {
		factor.getFeatureVector().set(factor.getFactorScope().type.entityName + " " +
				factor.getFactorScope().taggedToken.getText() + " " + factor.getFactorScope().phraseToken.getText(),true);

	}

	private String getTokenPhrase (DocumentToken token){
		String text = token.getText();
		String[] splitted = text.split("_");
		String phrase = splitted[splitted.length-1];
		return phrase;
	}

}
