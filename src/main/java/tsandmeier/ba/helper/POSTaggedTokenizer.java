package tsandmeier.ba.helper;

import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.tokenizer.RegExTokenizer;
import de.hterhors.semanticmr.tokenizer.SentenceSplitter;
import de.hterhors.semanticmr.tokenizer.Tokenization;
import de.hterhors.semanticmr.tokenizer.Tokenization.Token;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class POSTaggedTokenizer {
    private static Logger log = LogManager.getFormatterLogger(POSTaggedTokenizer.class);

    public POSTaggedTokenizer() {
    }

    public static List<DocumentToken> tokenizeDocumentsContent(String content) {
        log.debug("Tokenize content...");
        List<DocumentToken> list = new ArrayList();
        List<String> sentences = SentenceSplitter.extractSentences(content);
        List<Tokenization> tokens = POSRegExTokenizer.tokenize(sentences);
        Iterator var4 = tokens.iterator();

        while(var4.hasNext()) {
            Tokenization tokenization = (Tokenization)var4.next();
            int senTokenIndex = 0;

            for(Iterator var7 = tokenization.tokens.iterator(); var7.hasNext(); ++senTokenIndex) {
                Token token = (Token)var7.next();
                int sentenceIndex = token.getSentenceIndex();
                int docTokenIndex = token.getIndex();
                int docCharOnset = token.getOnsetCharPosition();
                int senCharOnset = token.getFromSen();
                String text = token.getText();
                list.add(new DocumentToken(sentenceIndex, senTokenIndex, docTokenIndex, senCharOnset, docCharOnset, text));
            }
        }

        log.debug("Number of tokens: " + list.size());
        return list;
    }
}

