package tsandmeier.ba.groupnameTemplates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * checks if two mentions belong to the same sentence
 */
public class GroupNamesInSameSentenceTemplate extends AbstractFeatureTemplate<GroupNamesInSameSentenceTemplate.MentionsInSentenceScope> {

    static class MentionsInSentenceScope
            extends AbstractFactorScope {

        public List<DocumentToken> tokenOne;
        public List<DocumentToken> tokenTwo;


        public MentionsInSentenceScope(
                AbstractFeatureTemplate<MentionsInSentenceScope> template, List<DocumentToken> tokenOne,
                List<DocumentToken> tokenTwo) {
            super(template);
            this.tokenOne = tokenOne;
            this.tokenTwo = tokenTwo;
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
            MentionsInSentenceScope that = (MentionsInSentenceScope) o;
            return Objects.equals(tokenOne, that.tokenOne) &&
                    Objects.equals(tokenTwo, that.tokenTwo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), tokenOne, tokenTwo);
        }
    }

    @Override
    public List<MentionsInSentenceScope> generateFactorScopes(State state) {
        List<MentionsInSentenceScope> factors = new ArrayList<>();

        for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
            for (DocumentLinkedAnnotation annotation2 : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
                if (!annotation.equals(annotation2) && annotation.getSentenceIndex() == annotation2.getSentenceIndex()) {
                    factors.add(new MentionsInSentenceScope(this,
                            annotation.relatedTokens,
                            annotation.relatedTokens));

                }
            }
        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<MentionsInSentenceScope> factor) {

        StringBuilder sbOne = new StringBuilder();
        factor.getFactorScope().tokenOne.forEach(p -> sbOne.append(p.getText()+" "));

        StringBuilder sbTwo = new StringBuilder();
        factor.getFactorScope().tokenTwo.forEach(p -> sbTwo.append(p.getText()+" "));


        factor.getFeatureVector().set("GroupNames in Same Sentence: " + sbOne.toString().trim() +
                ", " + sbTwo.toString().trim(), true);
    }

}
