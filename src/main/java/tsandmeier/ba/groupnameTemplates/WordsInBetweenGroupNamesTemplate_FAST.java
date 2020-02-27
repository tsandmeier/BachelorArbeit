package tsandmeier.ba.groupnameTemplates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * checks for all the words between two mentions
 */
public class WordsInBetweenGroupNamesTemplate_FAST extends AbstractFeatureTemplate<WordsInBetweenGroupNamesTemplate_FAST.AreWordsInBetweenScope> {

    private static final int MAX_NUMBER_OF_WORDS = 6;  //6 words seems to be the most successful

    static class AreWordsInBetweenScope
            extends AbstractFactorScope {

        String surfaceAnnoOne;
        String surfaceAnnoTwo;

        String subtext;


        public AreWordsInBetweenScope(
                AbstractFeatureTemplate<AreWordsInBetweenScope> template, String annoOne, String annoTwo,
                String subtext) {
            super(template);

            this.surfaceAnnoOne = annoOne;
            this.surfaceAnnoTwo = annoTwo;

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            AreWordsInBetweenScope that = (AreWordsInBetweenScope) o;
            return Objects.equals(surfaceAnnoOne, that.surfaceAnnoOne) &&
                    Objects.equals(surfaceAnnoTwo, that.surfaceAnnoTwo) &&
                    Objects.equals(subtext, that.subtext);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), surfaceAnnoOne, surfaceAnnoTwo, subtext);
        }
    }

    @Override
    public List<AreWordsInBetweenScope> generateFactorScopes(State state) {
        List<AreWordsInBetweenScope> factors = new ArrayList<>();
        Document document = state.getInstance().getDocument();

        List<DocumentLinkedAnnotation> annotations = super.getPredictedAnnotations(state);

        for(int i = 0; i < annotations.size(); i++) {

            int index1 = annotations.get(i).relatedTokens.get(annotations.get(i).relatedTokens.size()-1).getDocTokenIndex();

            for(int j = i+1; j < annotations.size(); j++){

                int index2 = annotations.get(j).relatedTokens.get(0).getDocTokenIndex();

                if(Math.abs(index1 - index2) > MAX_NUMBER_OF_WORDS+1)
                    continue;

                if(index2 > index1){
                    factors.add(new AreWordsInBetweenScope(this, annotations.get(i).getSurfaceForm(), annotations.get(j).getSurfaceForm(),
                            document.getContent(annotations.get(i).relatedTokens.get(annotations.get(i).relatedTokens.size()-1),
                                    annotations.get(j).relatedTokens.get(0))));
                } else{
                    factors.add(new AreWordsInBetweenScope(this, annotations.get(j).getSurfaceForm(), annotations.get(i).getSurfaceForm(),
                            document.getContent(annotations.get(j).relatedTokens.get(annotations.get(j).relatedTokens.size()-1),
                                    annotations.get(i).relatedTokens.get(0))));
                }

            }
        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<AreWordsInBetweenScope> factor) {

        factor.getFeatureVector().set("WORDS BETWEEN: <\"" + factor.getFactorScope().surfaceAnnoOne + "\", \""
                    + factor.getFactorScope().surfaceAnnoTwo + "\">: "
                    + factor.getFactorScope().subtext, true);
    }
}