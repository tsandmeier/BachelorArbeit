package tsandmeier.ba.helper;

import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.tokenizer.RegExTokenizer;
import de.hterhors.semanticmr.tokenizer.SentenceSplitter;
import de.hterhors.semanticmr.tokenizer.Tokenization;
import de.hterhors.semanticmr.tokenizer.Tokenization.Token;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class POSTaggedTokenizer {
    private static Logger log = LogManager.getFormatterLogger(POSTaggedTokenizer.class);



    public POSTaggedTokenizer() {

    }

    public static List<DocumentToken> tokenizeDocumentsContent(String content) {
        log.debug("Tokenize content...");


        MaxentTagger tagger = new MaxentTagger(
                "src/main/java/tsandmeier/ba/tagger/english-left3words-distsim.tagger");

        List<DocumentToken> list = new ArrayList();
        List<String> sentences = SentenceSplitter.extractSentences(content);
//        List<String> taggedSentences = SentenceSplitter.extractSentences(tagger.tagString(content));
        List<Tokenization> tokens = POSRegExTokenizer.tokenize(sentences);
//        List<Tokenization> taggedTokens = POSRegExTokenizer.tokenize(taggedSentences);
        Iterator var4 = tokens.iterator();

        int sentenceIndex = 0;

        while(var4.hasNext()) {
            Tokenization tokenization = (Tokenization)var4.next();
            Tokenization taggedtokens = POSRegExTokenizer.tokenize(tagger.tagString(sentences.get(sentenceIndex)));

            if(tokenization.tokens.size() == taggedtokens.tokens.size()) {
                int senTokenIndex = 0;

                for (Iterator var7 = taggedtokens.tokens.iterator(); var7.hasNext(); ++senTokenIndex) {
                    Token token = (Token) var7.next();
                    int tokenSentenceIndex = token.getSentenceIndex();
                    int docTokenIndex = token.getIndex();
                    int docCharOnset = token.getOnsetCharPosition();
                    int senCharOnset = token.getFromSen();
                    String text = token.getText();
                    list.add(new DocumentToken(sentenceIndex, senTokenIndex, docTokenIndex, senCharOnset, docCharOnset, text));
                }
            }
            sentenceIndex++;
        }

        log.debug("Number of tokens: " + list.size());
        return list;
    }
}

