package tsandmeier.ba.specs;

import de.hterhors.semanticmr.init.reader.ISpecificationsReader;
import de.hterhors.semanticmr.init.reader.csv.CSVDataStructureReader;

import java.io.File;

public class NERLASpecsGroupName {

	/**
	 * The file that contains specifications about the entities. This file is the
	 * only specification file which is necessary for NERLA as it contains basically
	 * a list of entities that need to be found.
	 */
	private final static File entities = new File("src/main/resources/examples/nerla/group_name/specs/csv/entities.csv");

	/**
	 * Specification file that contains information about slots. This file is
	 * internally not used for NERLA as it is not necessary. However, additional
	 * information can be exploited during feature generation.
	 **/
	private final static File slots = new File("src/main/resources/examples/nerla/group_name/specs/csv/slots.csv");

	/**
	 * Specification file that contains information about slots of entities. This
	 * file is internally not used for NERLA as it is not necessary. However,
	 * additional information can be exploited during feature generation.
	 **/
	private final static File structures = new File("src/main/resources/examples/nerla/group_name/specs/csv/structures.csv");

	/**
	 * Specification file of entity hierarchies. This is not necessary for NERLA but
	 * might be helpful for feature generation.
	 */
	private final static File hierarchies = new File("src/main/resources/examples/nerla/group_name/specs/csv/hierarchies.csv");

	public static final ISpecificationsReader csvSpecsReader = new CSVDataStructureReader(NERLASpecsGroupName.entities,
			NERLASpecsGroupName.hierarchies, NERLASpecsGroupName.slots, NERLASpecsGroupName.structures);
}
