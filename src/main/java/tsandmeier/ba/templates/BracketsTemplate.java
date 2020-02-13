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
 * Checks if Annotation is in Brackets, and how far those brackets are from the mention
 */

public class BracketsTemplate extends AbstractFeatureTemplate<BracketsTemplate.BracketsScope> {


	private static final int MAXRANGE = 7;

	static class BracketsScope
			extends AbstractFactorScope {

		private final EntityType type;
		List<DocumentToken> tokens;
		Document document;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;
			BracketsScope that = (BracketsScope) o;
			return Objects.equals(type, that.type) &&
					Objects.equals(tokens, that.tokens) &&
					Objects.equals(document, that.document);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), type, tokens, document);
		}

		public BracketsScope(AbstractFeatureTemplate template, EntityType type, List<DocumentToken> tokens, Document document) {
			super(template);
			this.type = type;
			this.tokens = tokens;
			this.document = document;
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
	public List<BracketsScope> generateFactorScopes(State state) {
		List<BracketsScope> factors = new ArrayList<>();

		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {

			factors.add(new BracketsScope(this, annotation.entityType, annotation.relatedTokens, annotation.document));

		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<BracketsScope> factor) {
		List<DocumentToken> tokens = factor.getFactorScope().tokens;
		int firstIndex = tokens.get(0).getDocTokenIndex();
		int lastIndex = tokens.get(tokens.size()-1).getDocTokenIndex();
		Document doc = factor.getFactorScope().document;
		for(int i = 0; i < MAXRANGE; i++){
			if(firstIndex-MAXRANGE-1>=0&&lastIndex+MAXRANGE+1<doc.tokenList.size()-1){
				String textBefore = doc.getContent(doc.tokenList.get(firstIndex-i-1), doc.tokenList.get(firstIndex-1));
				String textAfter = doc.getContent(doc.tokenList.get(lastIndex+1), doc.tokenList.get(lastIndex+i+1));

				if(hasOpenBracket(textBefore)&&hasClosedBracket(textAfter)){
					factor.getFeatureVector().set("In Brackets for Range "+i+": <"+ factor.getFactorScope().type.name +
							">",true);
				}
			}
		}
	}

	private boolean hasClosedBracket(String text) {
		return (text.contains(")")|| text.contains("}"));
	}

	private boolean hasOpenBracket(String text) {
		return (text.contains("(")||text.contains("{"));
	}

	private boolean isBracket (char letter){
		return (letter == '(' ||
				letter == ')' ||
				letter == '{' ||
				letter == '}');
	}
}
