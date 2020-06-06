package tsandmeier.ba.explorer;

import de.hterhors.semanticmr.crf.exploration.EntityRecLinkExplorer;
import de.hterhors.semanticmr.crf.exploration.IExplorationStrategy;
import de.hterhors.semanticmr.crf.exploration.constraints.HardConstraintsProvider;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.tools.specifications.AutomatedSectionifcation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tsandmeier.ba.tools.AutomatedSectionification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class EntityRecLinkExplorerCustom implements IExplorationStrategyCustom {
    private static Logger log = LogManager.getFormatterLogger(EntityRecLinkExplorer.class);

    final private HardConstraintsProvider hardConstraintsProvider;

//	public EntityRecLinkExplorer(HardConstraintsProvider hardConstraintsProvder) {
//		this.hardConstraintsProvider = hardConstraintsProvder;
//	}

    public EntityRecLinkExplorerCustom() {
        this.hardConstraintsProvider = null;
    }

    /**
     * Average number of new explored proposal states. This variable is used as
     * initial size of the next new proposal state list.
     */
    private int averageNumberOfNewProposalStates = 16;

    public int MAX_WINDOW_SIZE = 5;
    public int MIN_WINDOW_SIZE = 1;

    private int sentenceIndex;

    public HashMap<EntityType, Integer> windowSizeEntities = new HashMap<>();

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
//        final List<DocumentToken> tokens = currentState.getInstance().getDocument().tokenList;

//        AutomatedSectionification sectionification = new AutomatedSectionification(currentState.getInstance());
//
//        AutomatedSectionification.ESection section = sectionification.getSection(sentenceIndex);
//        if (!(section.equals(AutomatedSectionification.ESection.METHODS) || section.equals(AutomatedSectionification.ESection.ABSTRACT)
//                || section.equals(AutomatedSectionification.ESection.RESULTS) || section.equals(AutomatedSectionification.ESection.UNKNOWN)))
//            return;

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

//				AutomatedSectionification.ESection section = sectionification.getSection(fromToken.getSentenceIndex());
//				if(!(section.equals(AutomatedSectionification.ESection.METHODS)||section.equals(AutomatedSectionification.ESection.ABSTRACT)
//						||section.equals(AutomatedSectionification.ESection.RESULTS)||section.equals(AutomatedSectionification.ESection.UNKNOWN)))
//					continue;

                if (fromToken == toToken && currentState.containsAnnotationOnTokens(fromToken))
                    continue;
                else if (currentState.containsAnnotationOnTokens(fromToken, toToken))
                    continue;

                final String text = currentState.getInstance().getDocument().getContent(fromToken, toToken);

                fillWindowSizeEntities(currentState);   //specified max windowSize for each entity

                EntityType specificEntity = getSpecificEntity(toToken, text);
                if (specificEntity != null && specificEntity.equals(EntityType.get("Weight"))) { //extra rules for some cases. if a token fits the rule, it's only annotated with one entity
                    try {
                        AbstractAnnotation newCurrentPrediction = AnnotationBuilder.toAnnotation(
                                currentState.getInstance().getDocument(), specificEntity, text,
                                fromToken.getDocCharOffset());
                        proposalStates.add(currentState.deepAddCopy(newCurrentPrediction));
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                } else {
                    Set<EntityType> entityTypeCandidates = currentState.getInstance().getEntityTypeCandidates(text);

                    if (specificEntity == null || !specificEntity.equals(EntityType.get("Age"))) {
                        entityTypeCandidates.remove(EntityType.get("Age"));
                    }

                    for (EntityType entityType : entityTypeCandidates) {
//                for(EntityType entityType : currentState.getInstance().getEntityTypeCandidates(text)){
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
                }


            }
        }
    }

    private EntityType getSpecificEntity(DocumentToken toToken, String text) { //TODO: schöner machen, nur Text übergeben?
        if (toToken.getText().equals("g") || toToken.getText().equals("gm") || toToken.getText().equals(")")) {
            return (EntityType.get("Weight"));
        } else if (Pattern.compile("[0-9]").matcher(text).find()) {
            return EntityType.get("Age");
        } else {
            return null;
        }
    }

    private void fillWindowSizeEntities(State currentState) { //TODO: Automate. Maybe occurence with most words in all gold Annotations?
        windowSizeEntities.putIfAbsent(EntityType.get("Weight"), 6);
        windowSizeEntities.putIfAbsent(EntityType.get("Age"), 5);
        windowSizeEntities.putIfAbsent(EntityType.get("SpragueDawleyRat"), 5);
        windowSizeEntities.putIfAbsent(EntityType.get("LongEvansRat"), 4);
        windowSizeEntities.putIfAbsent(EntityType.get("WistarRat"), 4);
        windowSizeEntities.putIfAbsent(EntityType.get("Gender"), 3);
        windowSizeEntities.putIfAbsent(EntityType.get("Mixed"), 3);
        windowSizeEntities.putIfAbsent(EntityType.get("FischerRat"), 3);
        windowSizeEntities.putIfAbsent(EntityType.get("C57_BL6_Mouse"), 4);
        windowSizeEntities.putIfAbsent(EntityType.get("ListerHoodedRat"), 3);
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
}
