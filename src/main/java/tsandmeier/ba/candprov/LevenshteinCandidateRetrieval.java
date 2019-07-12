package tsandmeier.ba.candprov;

import de.hterhors.semanticmr.candprov.nerla.INerlaCandidateProvider;
import de.hterhors.semanticmr.crf.structure.EntityType;
import edu.stanford.nlp.util.ArraySet;
import org.apache.commons.lang3.StringUtils;
import java.util.Set;


public class LevenshteinCandidateRetrieval implements INerlaCandidateProvider {

	public LevenshteinCandidateRetrieval() {
	}

	@Override
	public Set<EntityType> getEntityTypeCandidates(String text) {

		Set<EntityType> entityTypes = new ArraySet<>();



		for(EntityType type: EntityType.getEntityTypes()){
			for(String token: text.split("\\W"))
				if(token.length()>1) {
					if (StringUtils.getLevenshteinDistance(token, type.entityName) == 0) {
						entityTypes.add(type);
						break;
					}
				}
		}



		return entityTypes;
	}

	private static LevenshteinCandidateRetrieval instance;

	public static INerlaCandidateProvider getInstance() {
		if (instance == null)
			instance = new LevenshteinCandidateRetrieval();



		return instance;
	}
}
