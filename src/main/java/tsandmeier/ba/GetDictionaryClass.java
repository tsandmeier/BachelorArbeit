package tsandmeier.ba;

import de.hterhors.semanticmr.candprov.nerla.INerlaCandidateProvider;
import de.hterhors.semanticmr.crf.structure.EntityType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * The in memory dictionary based candidate provider is the simplest form of
 * providing entity-candidates given free text.
 * 
 * Given an input text, we do a exact string match to entries of the dictionary
 * and return all entity-types to that one or multiple matches exists.
 * 
 * @author hterhors
 *
 */
public class GetDictionaryClass implements INerlaCandidateProvider {

	/**
	 * The dictionary.
	 */
	final private Map<EntityType, Set<String>> dictionary = new HashMap<>();


	Set<EntityType> set = new HashSet<>(Arrays.asList(EntityType.get("Weight"), EntityType.get("Age")));
	/**
	 * The reversed dictionary for fast look up.
	 */
	final private Map<String, Set<EntityType>> reverseDictionary = new HashMap<>();

	/**
	 * The in memory dictionary based candidate provider is the simplest form of
	 * providing entity-candidates given free text.
	 * 
	 * Given an input text, we do a exact string match to entries of the dictionary
	 * and return all entity-types to that one or multiple matches exists.
	 * 
	 * @param dictionaryFile the dictionary file.
	 * @throws IOException
	 */
	public GetDictionaryClass(final File dictionaryFile) {

		/**
		 * TODO: check file contents format.
		 */
		try {
			for (String dictLine : Files.readAllLines(dictionaryFile.toPath())) {

				final String data[] = dictLine.split("\t", 2);

				for (String entry : data[1].split("\\|")) {

					dictionary.putIfAbsent(EntityType.get(data[0]), new HashSet<>());
					dictionary.get(EntityType.get(data[0])).add(entry);
					reverseDictionary.putIfAbsent(entry, new HashSet<>());
					reverseDictionary.get(entry).add(EntityType.get(data[0]));
				}

			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public Set<EntityType> getEntityTypeCandidates(String text) {

//		if(text.matches(".*\\d+.*")){
//			return set;
//		}

		return reverseDictionary.getOrDefault(text, Collections.emptySet());
	}
}
