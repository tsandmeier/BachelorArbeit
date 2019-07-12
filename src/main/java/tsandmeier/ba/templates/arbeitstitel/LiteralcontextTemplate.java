package tsandmeier.ba.templates.arbeitstitel;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public class LiteralcontextTemplate extends AbstractFeatureTemplate<LiteralcontextTemplate.LiteralContextScope> {


    static class LiteralContextScope
            extends AbstractFactorScope {

        EntityType type;
        List<DocumentToken> tokens;

        public LiteralContextScope(AbstractFeatureTemplate template, EntityType type, List<DocumentToken> tokens) {
            super(template);
            this.type = type;
            this.tokens = tokens;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            LiteralContextScope that = (LiteralContextScope) o;
            return Objects.equals(type, that.type) &&
                    Objects.equals(tokens, that.tokens);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), type, tokens);
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
    public List<LiteralContextScope> generateFactorScopes(State state) {
        List<LiteralContextScope> factors = new ArrayList<>();

        for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {

            if (annotation.getEntityType().isLiteral) {
                factors.add(new LiteralContextScope(this, annotation.getEntityType(), annotation.relatedTokens));

            }

        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<LiteralContextScope> factor) {
        boolean firstFound = false;
        boolean secondFound = false;
        boolean[] found = new boolean[10];
        int numbersFound = 0;
        int[] numberIndices = new int[10];
        int counter = 0;
        List<DocumentToken> tokens = factor.getFactorScope().tokens;

        for (DocumentToken token : factor.getFactorScope().tokens) {
            if (isNumber(token.getText())) {
                found[counter] = true;
                numberIndices[counter] = tokens.indexOf(token);
                numbersFound++;
            }
        }

        if (numbersFound == 1){
            factor.getFeatureVector().set("Words before Number of Entity: <"+factor.getFactorScope().type.entityName +
                    "> "+ getSubtext(tokens, 0, numberIndices[0]),true);
            factor.getFeatureVector().set("Words after Number of Entity: <"+factor.getFactorScope().type.entityName +
                    "> "+ getSubtext(tokens, numberIndices[0], tokens.size()-1),true);
            if(numberIndices[0]>0) {
                System.out.println("Words before Number of Entity: <" + factor.getFactorScope().type.entityName +
                        "> " + getSubtext(tokens, 0, numberIndices[0]));
            }
            if(numberIndices[0] < tokens.size()-1) {
                System.out.println("Words after Number of Entity: <" + factor.getFactorScope().type.entityName +
                        "> " + getSubtext(tokens, numberIndices[0] + 1, tokens.size() - 1));
            }
        }

    }

    private boolean isNumber(String text) {
        return text.matches("[0-9]+");
    }

    private String getSubtext(List<DocumentToken> tokens, int firstIndex, int lastIndex) {
        List<DocumentToken> subList = tokens.subList(firstIndex, lastIndex);
        StringBuilder subText = new StringBuilder();
        for (DocumentToken token : subList) {
            subText.append(token.getText()).append(" ");
        }
        return subText.toString().trim();
    }

}
