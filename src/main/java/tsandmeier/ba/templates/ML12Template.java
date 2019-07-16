package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import tsandmeier.ba.helper.POSTaggedTokenizer;

import java.util.*;

/**
 * Uses Stanford-POS-Tagger to find out what kind of object in the sentence a token is.
 */
public class ML12Template extends AbstractFeatureTemplate<ML12Template.ML12Scope> {


	static class ML12Scope
			extends AbstractFactorScope {

		List<DocumentToken> tokens;
		Instance instance;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;
			ML12Scope ml12Scope = (ML12Scope) o;
			return Objects.equals(tokens, ml12Scope.tokens) &&
					Objects.equals(instance, ml12Scope.instance) &&
					Objects.equals(type, ml12Scope.type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), tokens, instance, type);
		}

		EntityType type;

		public ML12Scope(Instance instance, AbstractFeatureTemplate<ML12Scope> template, List<DocumentToken> tokens, EntityType type) {
			super(template);
			this.instance = instance;
			this.type = type;
			this.tokens = tokens;
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


		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
			EntityType type = annotation.getEntityType();

					factors.add(new ML12Scope(state.getInstance(), this, annotation.relatedTokens, type));
				}
		return factors;
	}


	Map<Instance,List<DocumentToken>> cache = new HashMap<	>();

	@Override
	public void generateFeatureVector(Factor<ML12Scope> factor) {

		Document doc = factor.getFactorScope().instance.getDocument();
		List<DocumentToken>	posTokenizedContent ;


		if((posTokenizedContent = cache .get(factor.getFactorScope().instance))==null)
		{
			posTokenizedContent =  POSTaggedTokenizer.tokenizeDocumentsContent(doc.documentContent);
			cache.put(factor.getFactorScope().instance,posTokenizedContent);
		}

		for(DocumentToken token: factor.getFactorScope().tokens) {
			for (DocumentToken posTaggedDT : posTokenizedContent) {
				if (posTaggedDT.getSentenceIndex() == token.getSentenceIndex()) {
					if (posTaggedDT.getSenTokenIndex() == token.getSenTokenIndex()) {
						factor.getFeatureVector().set(factor.getFactorScope().type.entityName + " " + getTokenPhrase(posTaggedDT), true);
						break;
					}
				}
			}
		}
	}

	private String getTokenPhrase (DocumentToken token){
		String text = token.getText();
		String[] splitted = text.split("_");
		String phrase = splitted[splitted.length-1];
		return phrase;
	}

	private String[] tokenizeText(String text){
		String[] tokenized = text.split("[^a-zA-Z0-9_\\.\\,]");
		return tokenized;
	}

}
