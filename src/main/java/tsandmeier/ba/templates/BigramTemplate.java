package tsandmeier.ba.templates;


import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.State;

import java.util.ArrayList;
import java.util.List;


public class BigramTemplate extends AbstractFeatureTemplate<BigramTemplate.BigramScope> {

    public BigramTemplate (boolean cache){
        super(false);
    }

    private static final int MAX_NGRAM_SIZE = 2; //2 scheint das beste zu sein
    private static final int MIN_NGRAM_SIZE = 2;

    /**
     * The scope of a template defines the variables that can be used for the
     * feature generation. <br>
     * <br>
     *
     * <b> Important: Each scope needs the inherit from FactorScope and needs to
     * call the super method passing all important variables as parameters for
     * caching. If variables are not passed to the super constructor caching might
     * not work properly resulting in failure or undetermined behavior! Note that,
     * when working with multiple super root search types the search type needs to
     * be a parameter, too. </b>
     *
     * @author hterhors
     *
     */
    class BigramScope extends AbstractFactorScope {

        /**
         * The variable that stores a surface that is used for feature generation later.
         */
        final public String surfaceForm;

        /**
         * The variable that stores the corresponding class label.
         */
        final public EntityType entityType;

        public BigramScope(AbstractFeatureTemplate<BigramScope> template, final EntityType entityType,
                           final String text) {
            /**
             * Good practice is to call super constructor always with the search root class.
             * TODO: Do not forget to add all variables.
             */
            super(template);
            this.surfaceForm = text;
            this.entityType = entityType;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((surfaceForm == null) ? 0 : surfaceForm.hashCode());
            result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            BigramScope other = (BigramScope) obj;
            if (surfaceForm == null) {
                if (other.surfaceForm != null)
                    return false;
            } else if (!surfaceForm.equals(other.surfaceForm))
                return false;
            if (entityType == null) {
                if (other.entityType != null)
                    return false;
            } else if (!entityType.equals(other.entityType))
                return false;
            return true;
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
			/*
			 * If the entity annotation should be null continue loop.
			 */
            EntityType entityType = annotation.getEntityType();
            String sf = annotation.getSurfaceForm();
            if (sf.isEmpty())
                continue;
			/*
			 * Add a new factor to the list.
			 */
            factors.add(new BigramScope(this, entityType, sf));

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
        String surfaceForm = factor.getFactorScope().surfaceForm;

		/*
		 * Features are added via the set method. The first feature simply concatenates
		 * the class label and the surface form of the annotation.
		 */
        //featureVector.set(classLabel + surfaceForm, true);

		/*
		 * The next set of features concatenates single tokens (split by none-words)
		 * of the surface form with the class label.
		 */

		//TODO: WARUM NICHT GLEICH TOKENLIST ÜBERGEBEN, STATT SPÄTER ZU SPLITTEN?

        for (String token : surfaceForm.split("\\W")) {
            for(int i = 0; i < token.length(); i++){
                for(int j = MIN_NGRAM_SIZE; j <= MAX_NGRAM_SIZE; j++) {
                    char[] bigramChars = new char[j];
                    if (token.length() >= i + j) {
                        token.getChars(i, i + j, bigramChars, 0);
                        String bigram = new String(bigramChars);
                        factor.getFeatureVector().set(et.entityName + " " + bigram, true);
                    }
                }
            }
        }
    }
}
