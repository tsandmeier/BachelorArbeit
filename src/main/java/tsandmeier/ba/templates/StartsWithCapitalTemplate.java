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
 * Checks if a mention starts with a capital, if it's not in the beginning of a sentence
 */
public class StartsWithCapitalTemplate extends AbstractFeatureTemplate<StartsWithCapitalTemplate.StartsWithCapitalScope> {

    public StartsWithCapitalTemplate(boolean cache){super(cache);}

    static class StartsWithCapitalScope
            extends AbstractFactorScope {


        final public DocumentToken token;
        final public EntityType type;


        public StartsWithCapitalScope(AbstractFeatureTemplate<?> template, DocumentToken token, EntityType type) {
            super(template);
            this.token = token;
            this.type = type;
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
            StartsWithCapitalScope that = (StartsWithCapitalScope) o;
            return Objects.equals(token, that.token) &&
                    Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), token, type);
        }
    }

    @Override
    public List<StartsWithCapitalScope> generateFactorScopes(State state) {
        List<StartsWithCapitalScope> factors = new ArrayList<>();

        for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {

            DocumentToken token = annotation.relatedTokens.get(0);
            EntityType type = annotation.getEntityType();
            factors.add(new StartsWithCapitalScope(this, token, type));

        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<StartsWithCapitalScope> factor) {

        DocumentToken token = factor.getFactorScope().token;
        EntityType type = factor.getFactorScope().type;

        if (token.getLength() >= 1 && token.getSentenceIndex()>0) {
            if (Character.isUpperCase(token.getText().charAt(0)))
                factor.getFeatureVector().set("Starts with Capital: <" + type.name + ">", true);
        }
    }

}
