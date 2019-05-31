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
import jdk.nashorn.internal.parser.Token;
import tsandmeier.ba.helper.POSTaggedTokenizer;

import javax.print.Doc;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Uses Stanford-POS-Tagger to find out what kind of object in the sentence a token is. Not working yet!
 */
public class ML12Template extends AbstractFeatureTemplate<ML12Template.ML12Scope> {

	Document taggedDocument;
	boolean docIsTagged = false;

	MaxentTagger tagger = new MaxentTagger(

			"src/main/java/tsandmeier/ba/tagger/english-left3words-distsim.tagger");

	static class ML12Scope
			extends AbstractFactorScope<ML12Scope> {

		String taggedToken;
		DocumentToken phraseToken;

		EntityType type;


		public ML12Scope(
                AbstractFeatureTemplate<ML12Scope> template, String token, EntityType type) {
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
					Objects.equals(phraseToken, ml12Scope.phraseToken) &&
					Objects.equals(type, ml12Scope.type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), taggedToken, phraseToken, type);
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
			List<DocumentToken> taggedTokens = POSTaggedTokenizer.tokenizeDocumentsContent(tagged);
			String [] tokenized = tokenizeText(tagged);
//			System.out.println(".");
			taggedDocument = new Document("taggedDoc", tagged);
			//System.out.println(taggedDocument.documentContent);

			writeUsingFileWriter(makeString(doc.tokenList), "original");
			writeUsingFileWriter(tagged, "tagged");
			writeUsingFileWriter(makeString(taggedTokens), "tokenized");
			docIsTagged = true;
		}
		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
			EntityType type = annotation.getEntityType();
			for(DocumentToken token: annotation.relatedTokens){
						String taggedToken = tagger.getTag(token.getDocTokenIndex());
//						DocumentToken taggedToken = taggedDocument.tokenList.get(token.getDocTokenIndex() * 2);
//						DocumentToken phrase = taggedDocument.tokenList.get((token.getDocTokenIndex() * 2)+1);
						factors.add(new ML12Scope(this, taggedToken, type));
			}
		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<ML12Scope> factor) {
		factor.getFeatureVector().set(factor.getFactorScope().type.entityName + " " + factor.getFactorScope().taggedToken,true);

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

	private static void writeUsingFileWriter(String data, String filename) {
		File file = new File("/home/tobias/Projekte/BachelorArbeit/testAusgaben/"+filename+".txt");
		FileWriter fr = null;
		try {
			fr = new FileWriter(file);
			fr.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			//close resources
			try {
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String makeString (String[] tokens){
		String output = "";
		for (String token : tokens){
			output = output + token + "\n";
		}
		return output;
	}

	private String makeString (List<DocumentToken> tokens){
		String output = "";
		for (DocumentToken token : tokens){
			output = output + token.getText() + "\n";
		}
		return output;
	}

}
