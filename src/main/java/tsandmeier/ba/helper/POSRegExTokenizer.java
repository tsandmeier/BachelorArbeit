package tsandmeier.ba.helper;



import de.hterhors.semanticmr.tokenizer.Tokenization;
import de.hterhors.semanticmr.tokenizer.Tokenization.Token;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class POSRegExTokenizer {
    private static Logger log = LogManager.getFormatterLogger(POSRegExTokenizer.class);
//    private static Pattern pattern = Pattern.compile("[a-zA-Z.]+|\\d+|[^\\w\\s]");
//    private static Pattern pattern = Pattern.compile("[\\w\\d,\\.]+_[\\w\\d,\\.]+|[a-zA-Z]+|[\\d]+|[^\\w\\s]");
//    private static Pattern pattern = Pattern.compile("[\\w\\d,.]+_[\\w\\d,.]+|[a-zA-Z]+|[\\d]+|[^\\w\\s]");
//    private static Pattern pattern = Pattern.compile("[\\w\\d]+_[\\w\\d,.]+|[^\\w\\s]_[\\w\\d,.]+|[a-zA-Z]+|[\\d]+|[^\\w\\s]");
    //TODO: PATTERN VERBESSERN, SODASS AUCH MEHR SÄTZE GLEICH SIND(Punkt am Ende des Satzes macht oft Probleme)
    private static Pattern pattern = Pattern.compile("[!\"#$%&'()*+,\\-.\\\\\\/:;<=>?@\\[\\]^_`{|}~§¬{}]+_[!\"#$%&'()*+,\\-.\\\\\\/:;<=>?@\\[\\]^_`{|}~§¬{}]+|[\\w\\d,.]+_[\\w\\d,\\.]+|[^\\w\\s]_[\\w\\d,.]+|[-a-zA-Z]+|[\\d]+|[^\\w\\s]");

    public POSRegExTokenizer() {
    }

    public static List<Tokenization> tokenize(List<String> sentences) {
        log.debug("Tokenize number of sentences: " + sentences.size());
        List<Tokenization> tokenizations = new ArrayList();
        int accumulatedSentenceLength = 0;
        int sentenceIndex = 0;
        int index = 0;
        boolean skipNext = false;

        String sentence;
        for(Iterator var5 = sentences.iterator(); var5.hasNext(); accumulatedSentenceLength += sentence.length()) {
            sentence = (String)var5.next();
            Matcher matcher = pattern.matcher(sentence);
            ArrayList tokens;
            for(tokens = new ArrayList(); matcher.find(); ++index) {
                String text = matcher.group();
                int from = matcher.start();
                int to = matcher.end();
//                if(!skipNext) {
                    tokens.add(new Token(sentenceIndex, index, accumulatedSentenceLength + from, accumulatedSentenceLength + to, text, from));
//                }

                skipNext = text.equals(".");
            }
            skipNext = false;


            ++sentenceIndex;
            Tokenization tokenization = new Tokenization(tokens, sentence, accumulatedSentenceLength);
            tokenizations.add(tokenization);
        }

        return tokenizations;
    }

    public static Tokenization tokenize(String sentence) {
        int sentenceIndex = 0;
        int index = 0;
        Matcher matcher = pattern.matcher(sentence);

        ArrayList tokens;
        for(tokens = new ArrayList(); matcher.find(); ++index) {
            String text = matcher.group();
            int from = matcher.start();
            int to = matcher.end();
            tokens.add(new Token(sentenceIndex, index, from, to, text, from));
        }

        int var8 = sentenceIndex + 1;
        Tokenization tokenization = new Tokenization(tokens, sentence, 0);
        return tokenization;
    }

    static {
        log.debug("Tokenization pattern = \"[a-zA-Z]+|\\d+|[^\\w\\s]\"");
    }
}
