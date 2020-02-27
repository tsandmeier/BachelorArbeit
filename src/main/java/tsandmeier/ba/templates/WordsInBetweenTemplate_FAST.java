package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;
import tsandmeier.ba.groupnameTemplates.WordsInBetweenGroupNamesTemplate_FAST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * checks for all the words between two mentions
 */
public class WordsInBetweenTemplate_FAST extends AbstractFeatureTemplate<WordsInBetweenTemplate_FAST.AreWordsInBetweenScope> {

    private static final int MAX_NUMBER_OF_WORDS = 6;  //6 words seems to be the most successful

    static class AreWordsInBetweenScope
            extends AbstractFactorScope {

        public String wordAfter;
        public DocumentToken tokenOne;
        public DocumentToken tokenTwo;

        public EntityType typeOne;
        public EntityType typeTwo;

        String subtext;

        public AreWordsInBetweenScope(
                AbstractFeatureTemplate<AreWordsInBetweenScope> template,
                EntityType typeOne, EntityType typeTwo, String subtext) {
            super(template);
            this.typeOne = typeOne;
            this.typeTwo = typeTwo;
            this.subtext = subtext;
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
    public List<AreWordsInBetweenScope> generateFactorScopes(State state) {
        List<AreWordsInBetweenScope> factors = new ArrayList<>();
        Document document = state.getInstance().getDocument();

        List<DocumentLinkedAnnotation> annotations = super.getPredictedAnnotations(state);

        for (int i = 0; i < annotations.size(); i++) {

            int index1 = annotations.get(i).relatedTokens.get(annotations.get(i).relatedTokens.size() - 1).getDocTokenIndex();

            for (int j = i + 1; j < annotations.size(); j++) {

                int index2 = annotations.get(j).relatedTokens.get(0).getDocTokenIndex();

                if (Math.abs(index1 - index2) > MAX_NUMBER_OF_WORDS + 1 || Math.abs(index1-index2) < 2)
                    continue;

                try {
                    if (index2 > index1) {
                        factors.add(new AreWordsInBetweenScope(this, annotations.get(i).entityType, annotations.get(j).entityType,
                                document.getContent(annotations.get(i).relatedTokens.get(annotations.get(i).relatedTokens.size() - 1),
                                        annotations.get(j).relatedTokens.get(0))));
                    } else {
                        factors.add(new AreWordsInBetweenScope(this, annotations.get(j).entityType, annotations.get(i).entityType,
                                document.getContent(annotations.get(j).relatedTokens.get(annotations.get(j).relatedTokens.size() - 1),
                                        annotations.get(i).relatedTokens.get(0))));
                    }
                }catch (StringIndexOutOfBoundsException e){
                    //TODO: Rausfinden woher kommt die Exception, wie kann ich sie verhindern?
                }

            }

        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<AreWordsInBetweenScope> factor) {

        factor.getFeatureVector().set("WORDS BETWEEN: <" + factor.getFactorScope().typeOne.name + ", "
                + factor.getFactorScope().typeTwo.name + ">: "
                + factor.getFactorScope().subtext, true);
    }
}