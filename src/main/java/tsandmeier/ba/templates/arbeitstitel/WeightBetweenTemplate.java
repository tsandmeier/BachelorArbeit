package tsandmeier.ba.templates.arbeitstitel;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;
import tsandmeier.ba.normalizer.interpreter.WeightInterpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author hterhors
 * @date Nov 15, 2017
 */
public class WeightBetweenTemplate extends AbstractFeatureTemplate<WeightBetweenTemplate.WeightBetweenScope> {


    static class WeightBetweenScope
            extends AbstractFactorScope {

        Document doc;

        EntityType typeOne;
        EntityType typeTwo;

        DocumentToken tokenOne;
        DocumentToken tokenTwo;


        public WeightBetweenScope(AbstractFeatureTemplate template, Document doc, EntityType typeOne, EntityType typeTwo, DocumentToken tokenOne, DocumentToken tokenTwo) {
            super(template);
            this.doc = doc;
            this.typeOne = typeOne;
            this.typeTwo = typeTwo;
            this.tokenOne = tokenOne;
            this.tokenTwo = tokenTwo;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            WeightBetweenScope that = (WeightBetweenScope) o;
            return Objects.equals(doc, that.doc) &&
                    Objects.equals(typeOne, that.typeOne) &&
                    Objects.equals(typeTwo, that.typeTwo) &&
                    Objects.equals(tokenOne, that.tokenOne) &&
                    Objects.equals(tokenTwo, that.tokenTwo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), doc, typeOne, typeTwo, tokenOne, tokenTwo);
        }
    }

    @Override
    public List<WeightBetweenScope> generateFactorScopes(State state) {
        List<WeightBetweenScope> factors = new ArrayList<>();

        for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
            for (DocumentLinkedAnnotation annotation2 : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
                if (!annotation.equals(annotation2)) {
                    EntityType typeOne = annotation.getEntityType();
                    EntityType typeTwo = annotation2.entityType;

                    DocumentToken firstToken = annotation.relatedTokens.get(annotation.relatedTokens.size() - 1);
                    DocumentToken secondToken = annotation2.relatedTokens.get(0);
                    if (firstToken.getDocTokenIndex() + 2 == secondToken.getDocTokenIndex())
                        factors.add(new WeightBetweenScope(this, state.getInstance().getDocument(), typeOne, typeTwo, firstToken, secondToken));
                }
            }
        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<WeightBetweenScope> factor) {

        WeightInterpreter wi = new WeightInterpreter(factor.getFactorScope().doc.tokenList.get(
                factor.getFactorScope().tokenOne.getDocTokenIndex()+1
        ).getText());

        factor.getFeatureVector().set("IS WEIGHT BETWEEN: <" + factor.getFactorScope().typeOne.entityName + ", " +
                factor.getFactorScope().typeTwo.entityName + ">: " + wi.isInterpretable(), true);

    }

}
