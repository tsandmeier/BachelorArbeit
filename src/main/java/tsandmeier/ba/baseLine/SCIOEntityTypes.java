package tsandmeier.ba.baseLine;

import de.hterhors.semanticmr.crf.structure.EntityType;

public class SCIOEntityTypes {

	public static final EntityType compoundTreatment = getLazy("CompoundTreatment");
	public static final EntityType definedExperimentalGroup = getLazy("DefinedExperimentalGroup");
	public static final EntityType compound = getLazy("Compound");
	public static final EntityType treatment = getLazy("Treatment");
	public static final EntityType species = getLazy("OrganismSpecies");
	public static final EntityType organismModel = getLazy("OrganismModel");
	public static final EntityType injury = getLazy("Injury");
	public static final EntityType vehicle = getLazy("Vehicle");
	public static final EntityType groupName = getLazy("GroupName");;

	private static EntityType getLazy(String name) {
		try {
			return EntityType.get(name);
		} catch (Exception e) {
		}
		return null;
	}
}
