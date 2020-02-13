package tsandmeier.ba.candprov;

import de.hterhors.semanticmr.crf.structure.EntityType;

import java.util.Set;

/**
 * An exhaustive candidate retrieval for named entity recognition and linking.
 * Given any surface form from a document, it always returns all existing entity
 * types of the current system scope.
 * 
 * @author hterhors
 *
 */
public class ExhaustiveCandidateRetrieval {

	/**
	 * An exhaustive candidate retrieval for named entity recognition and linking.
	 * Given any surface form from a document, it always returns all existing entity
	 * types of the current system scope.
	 */
	public ExhaustiveCandidateRetrieval() {
	}

	public Set<EntityType> getEntityTypeCandidates(String text) {
		return EntityType.getEntityTypes();
	}

	private static tsandmeier.ba.candprov.ExhaustiveCandidateRetrieval instance;

	public static ExhaustiveCandidateRetrieval getInstance() {
		if (instance == null)
			instance = new tsandmeier.ba.candprov.ExhaustiveCandidateRetrieval();

		return instance;
	}

}
