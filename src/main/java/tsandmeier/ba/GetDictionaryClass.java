package tsandmeier.ba;

import de.hterhors.semanticmr.candprov.nerla.INerlaCandidateProvider;
import de.hterhors.semanticmr.crf.structure.EntityType;
import tsandmeier.ba.normalizer.interpreter.AgeInterpreter;
import tsandmeier.ba.normalizer.interpreter.WeightInterpreter;

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



	WeightInterpreter sw;
	AgeInterpreter sa;

	Set<EntityType> weightSet = new HashSet<>(Collections.singletonList(EntityType.get("Weight")));
	Set<EntityType> ageSet = new HashSet<>(Collections.singletonList(EntityType.get("Age")));
	Set<EntityType> set = new HashSet<>(Arrays.asList(EntityType.get("Weight"), EntityType.get("Age")));
	/**
	 * The reversed dictionary for fast look up.
	 */
	final private Map<String, Set<EntityType>> reverseDictionary = new HashMap<>();


	final private Map<String, WeightInterpreter > weightCache = new HashMap<	>();
	final private Map<String, AgeInterpreter> ageCache = new HashMap<	>();

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

		if((sw = weightCache.get(text))==null)
		{
			weightCache.put(text, sw =new WeightInterpreter(text));
		}

		if(sw.isInterpretable()){
			return weightSet;
		}
//
//
//		if((sa = ageCache.get(text))==null)
//		{
//			ageCache.put(text, sa =new AgeInterpreter(text));
//		}
//
//		if(sa.isInterpretable()){
//			return ageSet;
//		}




//		SemanticAge sa = new SemanticAge.Builder().interprete(text).build();
//
//		if(sa.exists() && sa.unit.equals("week")){
//			return ageSet;
//		}

//		System.out.println(sw.asFormattedString());
//
//		System.out.println(sw.exists());

		return reverseDictionary.getOrDefault(text, Collections.emptySet());
	}
}
