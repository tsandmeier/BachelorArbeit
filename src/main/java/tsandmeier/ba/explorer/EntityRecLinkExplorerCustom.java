package tsandmeier.ba.explorer;

import de.hterhors.semanticmr.crf.exploration.EntityRecLinkExplorer;
import de.hterhors.semanticmr.crf.exploration.IExplorationStrategy;
import de.hterhors.semanticmr.crf.exploration.constraints.HardConstraintsProvider;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tsandmeier.ba.tools.AutomatedSectionification;

import java.util.ArrayList;
import java.util.List;

public class EntityRecLinkExplorerCustom implements IExplorationStrategy {
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

	public int MAX_WINDOW_SIZE = 10;
	public int MIN_WINDOW_SIZE = 1;

	@Override
	public List<State> explore(State currentState) {

		final List<State> proposalStates = new ArrayList<>(averageNumberOfNewProposalStates);

		addNewAnnotation(proposalStates, currentState);
		removeAnnotation(proposalStates, currentState);

		updateAverage(proposalStates);

		return proposalStates;

	}

	private void removeAnnotation(List<State> proposalStates, State currentState) {

		for (int annotationIndex = 0; annotationIndex < currentState.getCurrentPredictions().getAnnotations()
				.size(); annotationIndex++) {
			proposalStates.add(currentState.deepRemoveCopy(annotationIndex));
		}

	}

	private void addNewAnnotation(final List<State> proposalStates, State currentState) {
		final List<DocumentToken> tokens = currentState.getInstance().getDocument().tokenList;

		AutomatedSectionification sectionification = new AutomatedSectionification(currentState.getInstance());

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

				for (EntityType entityType : currentState.getInstance().getEntityTypeCandidates(text)) {

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

	private void updateAverage(final List<State> proposalStates) {
		averageNumberOfNewProposalStates += proposalStates.size();
		averageNumberOfNewProposalStates /= 2;
	}

}
