package tsandmeier.ba.explorer;

import de.hterhors.semanticmr.crf.exploration.EntityRecLinkExplorer;
import de.hterhors.semanticmr.crf.exploration.constraints.HardConstraintsProvider;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import de.uni.bielefeld.sc.hterhors.psink.scio.semanticmr.literal_normalization.interpreter.*;
import de.uni.bielefeld.sc.hterhors.psink.scio.semanticmr.literal_normalization.interpreter.struct.AbstractInterpreter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

//import de.hterhors.semanticmr.tools.specifications.AutomatedSectionifcation;

public class EntityRecLinkExplorerInjury implements IExplorationStrategyCustom {
    private static Logger log = LogManager.getFormatterLogger(EntityRecLinkExplorer.class);

    final private HardConstraintsProvider hardConstraintsProvider;

//	public EntityRecLinkExplorer(HardConstraintsProvider hardConstraintsProvder) {
//		this.hardConstraintsProvider = hardConstraintsProvder;
//	}

    public EntityRecLinkExplorerInjury() {
        this.hardConstraintsProvider = null;
    }

    public int getAverageNumberOfNewProposalStates() {
        return averageNumberOfNewProposalStates;
    }

    /**
     * Average number of new explored proposal states. This variable is used as
     * initial size of the next new proposal state list.
     */
    private int averageNumberOfNewProposalStates = 16;

    public int MAX_WINDOW_SIZE = 10;
    public int MIN_WINDOW_SIZE = 1;

    private int sentenceIndex;

    private HashMap<EntityType, Integer> windowSizeEntities = new HashMap<>();

    @Override
    public List<State> explore(State currentState) {

        final List<State> proposalStates = new ArrayList<>(averageNumberOfNewProposalStates);

        addNewAnnotation(proposalStates, currentState);
        removeAnnotation(proposalStates, currentState);

        updateAverage(proposalStates);

//        System.out.println("Proposal States: " + proposalStates.size());
//        System.out.println("Average Number of Proposal States: " + averageNumberOfNewProposalStates);

        return proposalStates;

    }

    @Override
    public void set(State state) {
    }

    private void removeAnnotation(List<State> proposalStates, State currentState) {

        for (int annotationIndex = 0; annotationIndex < currentState.getCurrentPredictions().getAnnotations()
                .size(); annotationIndex++) {
            proposalStates.add(currentState.deepRemoveCopy(annotationIndex));
        }

    }

    private void addNewAnnotation(final List<State> proposalStates, State currentState) {

        final List<DocumentToken> tokens = currentState.getInstance().getDocument().getSentenceByIndex(sentenceIndex);

        for (int windowSize = MIN_WINDOW_SIZE; windowSize <= MAX_WINDOW_SIZE; windowSize++) {

            for (int runIndex = 0; runIndex < tokens.size() - windowSize; runIndex++) {

                final DocumentToken fromToken = tokens.get(runIndex); // including
                final DocumentToken toToken = tokens.get(runIndex + windowSize - 1); // including


                /*
                 * Check some basic constraints.
                 */

                if (fromToken.isStopWord())
                    continue;
                if (fromToken.isPunctuation())
                    continue;

                /*
                 * TODO: Might check tokens in between.
                 */

                if (toToken.isStopWord())
                    continue;

                if (toToken.isPunctuation())
                    continue;

                if (fromToken.getSentenceIndex() != toToken.getSentenceIndex())
                    continue;

                if (fromToken == toToken && currentState.containsAnnotationOnTokens(fromToken))
                    continue;
                else if (currentState.containsAnnotationOnTokens(fromToken, toToken))
                    continue;

                final String text = currentState.getInstance().getDocument().getContent(fromToken, toToken);

                fillWindowSizeEntities(currentState);   //specified max windowSize for each entity


                Set<EntityType> entityTypeCandidates = currentState.getInstance().getEntityTypeCandidates(text);

                EntityType specificType;

                if((specificType = getSpecificEntity(text)) == null){
                    for (EntityType entityType : entityTypeCandidates) {
                        if (windowSizeEntities.get(entityType) >= windowSize) {
                            try {
                                AbstractAnnotation newCurrentPrediction = AnnotationBuilder.toAnnotation(
                                        currentState.getInstance().getDocument(), entityType, text,
                                        fromToken.getDocCharOffset());
                                proposalStates.add(currentState.deepAddCopy(newCurrentPrediction));
                            } catch (RuntimeException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else{
                        try {
                            AbstractAnnotation newCurrentPrediction = AnnotationBuilder.toAnnotation(
                                    currentState.getInstance().getDocument(), specificType, text,
                                    fromToken.getDocCharOffset());
                            proposalStates.add(currentState.deepAddCopy(newCurrentPrediction));
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                }
            }
        }
    }

    private EntityType getSpecificEntity(String text) {
//        if (new ThicknessInterpreter(text).isInterpretable()) {
//            return EntityType.get("Thickness");
//        } else if (new VolumeInterpreter(text).isInterpretable()) {
//            return EntityType.get("Volume");
//        } else if (new LengthInterpreter(text).isInterpretable()) {
//            return EntityType.get("Length");
//        } else if (new WeightInterpreter(text).isInterpretable()) {
//            return EntityType.get("Weight");
//        } else if (new LightIntensityInterpreter(text).isInterpretable()) {
//            return EntityType.get("LightIntensity");
//        }
//        else if (new DurationInterpreter(text).isInterpretable()) {
//            return EntityType.get("Duration");
//        } else if (new ForceInterpreter(text).isInterpretable()) {
//            return EntityType.get("Force");
//        } else if (new DistanceInterpreter(text).isInterpretable()) {
//            return EntityType.get("Distance");
//        } else if (new PressureInterpreter(text).isInterpretable()) {
//            return EntityType.get("Pressure");
//        } else if (new VolumeInterpreter(text).isInterpretable()) {
//            return EntityType.get("Volume");
//        }
        return null;
    }

    private void fillWindowSizeEntities(State currentState) { //TODO: Automate. Maybe occurence with most words in all gold Annotations?
        for (EntityType entityType : currentState.getInstance().getEntityTypeCandidates("")) {
            windowSizeEntities.putIfAbsent(entityType, 2);
        }
    }

    private void updateAverage(final List<State> proposalStates) {
        averageNumberOfNewProposalStates += proposalStates.size();
        averageNumberOfNewProposalStates /= 2;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public State next() {
        return null;
    }

    @Override
    public void setSentenceIndex(int sentenceIndex) {
        this.sentenceIndex = sentenceIndex;
    }

    public void fillWindowSizeEntitiesFromGoldAnnotaions(List<Instance> instances){
        for (Instance instance: instances){
            Annotations goldAnnos = instance.getGoldAnnotations();

            for (EntityType entityType : instance.getEntityTypeCandidates("")) {
                windowSizeEntities.putIfAbsent(entityType, 2);
            }

            for (AbstractAnnotation abstractAnno : instance.getGoldAnnotations().getAnnotations()){
                DocumentLinkedAnnotation goldAnno = (DocumentLinkedAnnotation) abstractAnno;
                windowSizeEntities.putIfAbsent(goldAnno.getEntityType(), 0);
                if (goldAnno.relatedTokens.size() > windowSizeEntities.get(goldAnno.getEntityType())){
                    windowSizeEntities.put(goldAnno.getEntityType(), goldAnno.relatedTokens.size());
                }
            }
        }
    }


}
