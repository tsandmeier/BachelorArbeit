package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.helper.DefaultDocumentTokenizer;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.tokenizer.SentenceSplitter;
import de.hterhors.semanticmr.tokenizer.Tokenization;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.tagger.maxent.TaggerConfig;
import jdk.nashorn.internal.parser.Token;
import tsandmeier.ba.helper.POSRegExTokenizer;
import tsandmeier.ba.helper.POSTaggedTokenizer;

import javax.print.Doc;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Uses Stanford-POS-Tagger to find out what kind of object in the sentence a token is. Not working yet!
 */
public class ML12Template extends AbstractFeatureTemplate<ML12Template.ML12Scope> {

	List<DocumentToken> posTokenizedContent;

	static class ML12Scope
			extends AbstractFactorScope<ML12Scope> {

		DocumentToken taggedToken;


		EntityType type;


		public ML12Scope(
                AbstractFeatureTemplate<ML12Scope> template, DocumentToken token, EntityType type) {
			super(template);
			this.taggedToken = token;
			this.type = type;
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
//			props.setProperty("tagSeparator", "TAG");




			Document doc = state.getInstance().getDocument();

			// set up pipeline properties
//			Properties props = new Properties();
//			// set the list of annotators to run
//			props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");
//			// set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
//			props.setProperty("coref.algorithm", "neural");
//
//			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
//			// create a document object
//			CoreDocument document = new CoreDocument(doc.documentContent);
//			// annnotate the document
//			pipeline.annotate(document);
//			// examples
//
//			writeUsingFileWriter(document.sentences().get(0).text(), "testsatz");


			posTokenizedContent = POSTaggedTokenizer.tokenizeDocumentsContent(doc.documentContent);


//			for(String sentence : SentenceSplitter.extractSentences(doc.documentContent)){
//				String posTaggedSentence = tagger.tagString(sentence);
////				Tokenization posTokenizedSentence = POSRegExTokenizer.tokenize(posTaggedSentence);
////				Tokenization tokenizedSentence = POSRegExTokenizer.tokenize(sentence);
////				List<DocumentToken> posTokenizedSentence = POSTaggedTokenizer.tokenizeDocumentsContent(posTaggedSentence);
//				List<DocumentToken> tokenizedSentence = POSTaggedTokenizer.tokenizeDocumentsContent(sentence);
//
//
////				writeUsingFileWriter(makeString(posTokenizedSentence), "POSSentence");
////				writeUsingFileWriter(makeString(tokenizedSentence), "normalSentence");
//
//			}
		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
			EntityType type = annotation.getEntityType();
			for(DocumentToken token: annotation.relatedTokens){

				List<DocumentToken> taggedTokenList = posTokenizedContent.parallelStream()
						.filter(item -> item.getSentenceIndex() == (token.getSentenceIndex()))
						.filter(item -> item.getSentenceIndex() == (token.getSenTokenIndex()))
						.collect(Collectors.toList());

				if(taggedTokenList.size() > 0) {
					DocumentToken taggedToken = taggedTokenList.get(0);

					factors.add(new ML12Scope(this, taggedToken, type));
				}
			}
		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<ML12Scope> factor) {
		factor.getFeatureVector().set(factor.getFactorScope().type.entityName + " " + getTokenPhrase(factor.getFactorScope().taggedToken),true);
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
		File file = new File("/homes/tsandmeier/Dokumente/BA/BachelorArbeit/testAusgaben/"+filename+".txt");
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

	private String makeString (Tokenization tokenization){
		String output = "";

		int senTokenIndex = 0;

		for(Iterator var7 = tokenization.tokens.iterator(); var7.hasNext(); ++senTokenIndex) {
			Tokenization.Token token = (Tokenization.Token)var7.next();
			output = output + token.getText() + "\n";
		}
		return output;
	}
}
