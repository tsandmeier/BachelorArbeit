package tsandmeier.ba.groupnameTemplates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * WBF: first word in between if more than two words in between
 * WBL: last word in between if more than two words in between
 */
public class WBLGroupNamesTemplate_FAST extends AbstractFeatureTemplate<WBLGroupNamesTemplate_FAST.WBScope> {

    private static boolean WBFACTIVE = false;
    private static boolean WBLACTIVE = true;

    static class WBScope
            extends AbstractFactorScope {

        public final String annoOne;
        public final String annoTwo;

        public final String word;

        public final boolean first;

        public WBScope(AbstractFeatureTemplate<?> template, String annoOne, String annoTwo, String word,
                       boolean first) {
            super(template);
            this.annoOne = annoOne;
            this.annoTwo = annoTwo;
            this.word = word;
            this.first = first;
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
            WBScope wbScope = (WBScope) o;
            return first == wbScope.first &&
                    Objects.equals(annoOne, wbScope.annoOne) &&
                    Objects.equals(annoTwo, wbScope.annoTwo) &&
                    Objects.equals(word, wbScope.word);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), annoOne, annoTwo, word, first);
        }
    }

    @Override
    public List<WBLGroupNamesTemplate_FAST.WBScope> generateFactorScopes(State state) {
        List<WBLGroupNamesTemplate_FAST.WBScope> factors = new ArrayList<>();
        Document document = state.getInstance().getDocument();

        List<DocumentLinkedAnnotation> annotations = super.<DocumentLinkedAnnotation>getPredictedAnnotations(state);

        for (int i = 0; i < annotations.size(); i++) {

            final int a1Index = annotations.get(i).relatedTokens.get(annotations.get(i).relatedTokens.size() - 1)
                    .getDocTokenIndex();

            for (int j = i + 1; j < annotations.size(); j++) {

                final int a2Index = annotations.get(j).relatedTokens.get(0).getDocTokenIndex();

                if (Math.abs(a1Index - a2Index) < 3 || Math.abs(a1Index - a2Index) > 20)
                    continue;

                if (a2Index < a1Index) {
                    if (WBFACTIVE) {
                        factors.add(new WBLGroupNamesTemplate_FAST.WBScope(this, annotations.get(i).getSurfaceForm(),
                                annotations.get(j).getSurfaceForm(), document.tokenList.get(a2Index + 1).getText(),
                                true));
                    }
                    if (WBLACTIVE) {
                        factors.add(new WBLGroupNamesTemplate_FAST.WBScope(this, annotations.get(i).getSurfaceForm(),
                                annotations.get(j).getSurfaceForm(), document.tokenList.get(a1Index - 1).getText(),
                                false));
                    }
                } else {
                    // swap annotations
                    if (WBFACTIVE) {
                        factors.add(new WBLGroupNamesTemplate_FAST.WBScope(this, annotations.get(j).getSurfaceForm(),
                                annotations.get(i).getSurfaceForm(), document.tokenList.get(a1Index + 1).getText(),
                                true));
                    }
                    if (WBLACTIVE) {
                        factors.add(new WBLGroupNamesTemplate_FAST.WBScope(this, annotations.get(j).getSurfaceForm(),
                                annotations.get(i).getSurfaceForm(), document.tokenList.get(a2Index - 1).getText(),
                                false));
                    }
                }

            }
        }

        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<WBScope> factor) {

        factor.getFeatureVector()
                .set((factor.getFactorScope().first ? "First" : "Last") + " word inbetween: <\""
                        + factor.getFactorScope().annoOne + "\", \"" + factor.getFactorScope().annoTwo + "\"> "
                        + factor.getFactorScope().word, true);

    }
}
