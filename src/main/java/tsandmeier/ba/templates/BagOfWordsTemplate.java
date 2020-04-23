package tsandmeier.ba.templates;


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
 * checks all the single words in a mention
 */

public class BagOfWordsTemplate extends AbstractFeatureTemplate<BagOfWordsTemplate.BigramScope> {

    public BagOfWordsTemplate(boolean cache) {
        super(cache);
    }

    public BagOfWordsTemplate() {
        super();
    }

    class BigramScope extends AbstractFactorScope {

        List<DocumentToken> tokens;
        final public EntityType entityType;

        public BigramScope(AbstractFeatureTemplate template, List<DocumentToken> tokens, EntityType entityType) {
            super(template);
            this.tokens = tokens;
            this.entityType = entityType;
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
            BigramScope that = (BigramScope) o;
            return Objects.equals(tokens, that.tokens) &&
                    Objects.equals(entityType, that.entityType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), tokens, entityType);
        }
    }

    @Override
    public List<BigramScope> generateFactorScopes(State state) {
        final List<BigramScope> factors = new ArrayList<>();
        for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
            EntityType entityType = annotation.getEntityType();
            factors.add(new BigramScope(this, annotation.relatedTokens, entityType));

        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<BigramScope> factor) {
        List<DocumentToken> tokens = factor.getFactorScope().tokens;

        for (DocumentToken token : tokens) {
            factor.getFeatureVector().set("\""+token.getText()+"\" in: " + factor.getFactorScope().entityType.name,true);
        }
    }
}
