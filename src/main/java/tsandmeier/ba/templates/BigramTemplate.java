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


public class BigramTemplate extends AbstractFeatureTemplate<BigramTemplate.BigramScope> {

    public BigramTemplate (boolean cache){
        super(cache);
    }

    public BigramTemplate() {
        super();
    }

    private static final int MAX_NGRAM_SIZE = 5; //2 scheint das beste zu sein
    private static final int MIN_NGRAM_SIZE = 2;

    /**
     *
     */
    class BigramScope extends AbstractFactorScope {


        List<DocumentToken> tokens;
        final public EntityType entityType;

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
    }

    @Override
    public List<BigramScope> generateFactorScopes(State state) {

        /**
         * The list of factors. One factor for each annotation.
         */
        final List<BigramScope> factors = new ArrayList<>();
		/*
		 * Get the current annotations of the state.
		 */
        for (DocumentLinkedAnnotation annotation: super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
            EntityType entityType = annotation.getEntityType();
            factors.add(new BigramScope(this, annotation.relatedTokens, entityType));

        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<BigramScope> factor) {

		/*
		 * This method is called after the factor generation, but only if the factors
		 * were not be cached previously! In this method, we first get the factor scope
		 * and the feature vector and compute features based on the variables of the
		 * scope in order to add them to the feature vector.
		 */

        /**
         * The scope that was previously created. Here, we have access to all previously
         * filled variables.
         */

        EntityType et = factor.getFactorScope().entityType;
        List<DocumentToken> tokens = factor.getFactorScope().tokens;

        for (DocumentToken token : tokens) {
            for(int i = 0; i < token.getText().length(); i++){
                for(int j = MIN_NGRAM_SIZE; j <= MAX_NGRAM_SIZE; j++) {
                    char[] bigramChars = new char[j];
                    if (token.getText().length() >= i + j) {
                        token.getText().getChars(i, i + j, bigramChars, 0);
                        String bigram = new String(bigramChars);
                        factor.getFeatureVector().set(et.entityName + " " + bigram, true);
                    }
                }
            }
        }
    }
}
