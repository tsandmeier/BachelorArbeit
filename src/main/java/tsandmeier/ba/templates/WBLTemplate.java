package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
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
 * last word inbetween if more than two words in betweeen
 */
public class WBLTemplate extends AbstractFeatureTemplate<WBLTemplate.WordsInBetweenScope> {

	static class WordsInBetweenScope
			extends AbstractFactorScope<WordsInBetweenScope> {

		String wordAfter;
		DocumentToken tokenOne;
		DocumentToken tokenTwo;

		EntityType typeOne;
		EntityType typeTwo;

		public Document document;

		public WordsInBetweenScope(
				AbstractFeatureTemplate<WordsInBetweenScope> template, DocumentToken tokenOne, DocumentToken tokenTwo,
				EntityType typeOne, EntityType typeTwo, Document document) {
			super(template);
			this.tokenOne = tokenOne;
			this.tokenTwo = tokenTwo;
			this.typeOne = typeOne;
			this.typeTwo = typeTwo;
			this.document = document;
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
			WordsInBetweenScope wordsInBetweenScope = (WordsInBetweenScope) o;
			return tokenOne == wordsInBetweenScope.tokenOne &&
					tokenTwo == wordsInBetweenScope.tokenTwo &&
					Objects.equals(wordAfter, wordsInBetweenScope.wordAfter) &&
					Objects.equals(typeOne, wordsInBetweenScope.typeOne) &&
					Objects.equals(typeTwo, wordsInBetweenScope.typeTwo);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), wordAfter, tokenOne, tokenTwo, typeOne, typeTwo);
		}
	}

	@Override
	public List<WordsInBetweenScope> generateFactorScopes(State state) {
		List<WordsInBetweenScope> factors = new ArrayList<>();
		Document document = state.getInstance().getDocument();

		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
			for (DocumentLinkedAnnotation annotation2 : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)){
				if(!annotation.equals(annotation2)) {
					try {
						factors.add(new WordsInBetweenScope(this,
								document.getTokenByCharOffset(annotation.documentPosition.docCharOffset),
								document.getTokenByCharOffset(annotation2.documentPosition.docCharOffset),
								annotation.entityType, annotation2.entityType,
								document));
					} catch (DocumentLinkedAnnotationMismatchException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<WordsInBetweenScope> factor) {
		String subtext;


		if (factor.getFactorScope().tokenOne.getDocCharOffset()<factor.getFactorScope().tokenTwo.getDocCharOffset()) {

			//get all words between the mentions
			subtext = factor.getFactorScope().document.getContent(
					factor.getFactorScope().tokenOne, factor.getFactorScope().tokenTwo
			);
			String [] tokenizedSubtext = tokenizeString(subtext);


				if(tokenizedSubtext.length >= 2){
					factor.getFeatureVector().set(factor.getFactorScope().typeOne.entityName + " "
							+ factor.getFactorScope().typeTwo.entityName + " " + tokenizedSubtext[tokenizedSubtext.length-1], true);
				}


		}
	}

	private String[] tokenizeString(String text){
		return text.split("\\s+");
	}
}
