package tsandmeier.ba.candprov;

import de.hterhors.semanticmr.candprov.nerla.INerlaCandidateProvider;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.Instance;
import tsandmeier.ba.normalizer.interpreter.AgeInterpreter;
import tsandmeier.ba.normalizer.interpreter.WeightInterpreter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The in memory dictionary based candidate provider is the simplest form of
 * providing entity-candidates given free text.
 * <p>
 * Given an input text, we do a exact string match to entries of the dictionary
 * and return all entity-types to that one or multiple matches exists.
 *
 * @author hterhors
 */
public class CreateAndGetDictionaryClass implements INerlaCandidateProvider {

    /**
     * The dictionary.
     */
    final private Map<EntityType, Set<String>> dictionary = new HashMap<>();


    WeightInterpreter sw;
    AgeInterpreter sa;


    Set<EntityType> weightSet = new HashSet<>(Collections.singletonList(EntityType.get("Weight")));
//    Set<EntityType> dosageSet = new HashSet<>(Collections.singletonList(EntityType.get("Dosage")));
    Set<EntityType> dosageSet = new HashSet<>(Arrays.asList(EntityType.get("Dosage"), EntityType.get("DosageIntracorporal"),
        EntityType.get("DosageExtracorporal")));

    private Set<EntityType> distanceSet = new HashSet<>(Collections.singletonList(EntityType.get("Distance")));


    private Set<EntityType> dosageDistanceSet = new HashSet<>(Arrays.asList(EntityType.get("Dosage"), EntityType.get("DosageIntracorporal"),
            EntityType.get("DosageExtracorporal"),EntityType.get("Distance")));


//    Set<EntityType> ageSet = new HashSet<>(Collections.singletonList(EntityType.get("Age")));
//	Set<EntityType> set = new HashSet<>(Arrays.asList(EntityType.get("Weight"), EntityType.get("Age")));
    /**
     * The reversed dictionary for fast look up.
     */
    final private Map<String, Set<EntityType>> reverseDictionary = new HashMap<>();


    final private Map<String, WeightInterpreter> weightCache = new HashMap<>();
    final private Map<String, AgeInterpreter> ageCache = new HashMap<>();

    public CreateAndGetDictionaryClass(List<Instance> instances) {

        /**
         * TODO: check file contents format.
         */

        for (Instance inst : instances) {
            for (AbstractAnnotation annotation : inst.getGoldAnnotations().getAnnotations()) {

//                String entry = String.join(annotation.asInstanceOfDocumentLinkedAnnotation().relatedTokens.stream().map(DocumentToken::getText)
//                        .collect(Collectors.joining()));


                String entry = convertToString(annotation.asInstanceOfDocumentLinkedAnnotation().relatedTokens);
                dictionary.putIfAbsent(annotation.asInstanceOfDocumentLinkedAnnotation().entityType, new HashSet<>());
                dictionary.get(annotation.asInstanceOfDocumentLinkedAnnotation().entityType).add(entry);
                reverseDictionary.putIfAbsent(entry, new HashSet<>());
                reverseDictionary.get(entry).add(annotation.asInstanceOfDocumentLinkedAnnotation().entityType);

                for (DocumentToken token : annotation.asInstanceOfDocumentLinkedAnnotation().relatedTokens) {
                    dictionary.putIfAbsent(annotation.asInstanceOfDocumentLinkedAnnotation().entityType, new HashSet<>());
                    dictionary.get(annotation.asInstanceOfDocumentLinkedAnnotation().entityType).add(token.getText());
                    reverseDictionary.putIfAbsent(token.getText(), new HashSet<>());
                    reverseDictionary.get(token.getText()).add(annotation.asInstanceOfDocumentLinkedAnnotation().entityType);
                }

                dictionary.putIfAbsent(EntityType.get("IntraperitonealLocation"), new HashSet<>());
                dictionary.get(EntityType.get("IntraperitonealLocation")).add("i.p.");
                reverseDictionary.putIfAbsent("i.p.", new HashSet<>());
                reverseDictionary.get("i.p.").add(EntityType.get("IntraperitonealLocation"));

                dictionary.putIfAbsent(EntityType.get("InjectionDelivery"), new HashSet<>());
                dictionary.get(EntityType.get("InjectionDelivery")).add("i.p.");
                reverseDictionary.putIfAbsent("i.p.", new HashSet<>());
                reverseDictionary.get("i.p.").add(EntityType.get("InjectionDelivery"));
            }
        }
    }


//
//		try {
//			for (String dictLine : Files.readAllLines(dictionaryFile.toPath())) {
//
//				final String data[] = dictLine.split("\t", 2);
//
//				for (String entry : data[1].split("\\|")) {
//
//					dictionary.putIfAbsent(EntityType.get(data[0]), new HashSet<>());
//					dictionary.get(EntityType.get(data[0])).add(entry);
//					reverseDictionary.putIfAbsent(entry, new HashSet<>());
//					reverseDictionary.get(entry).add(EntityType.get(data[0]));
//				}
//
//			}
//		} catch (IOException e) {
//			throw new RuntimeException(e.getMessage());
//		}
//	}

    @Override
    public Set<EntityType> getEntityTypeCandidates(String text) {

//		if(text.matches(".*\\d+.*")){
//			return set;
//		}
//
        if ((sw = weightCache.get(text)) == null) {
            weightCache.put(text, sw = new WeightInterpreter(text));
        }

        if (sw.isInterpretable()) {
            return weightSet;
        }

        boolean isDosage = isDosage(text);
        boolean isDistance = isDistance(text);


        if(isDosage && isDistance){
            return dosageDistanceSet;
        }

        if(isDosage)
            return dosageSet;

        if(isDistance(text)){
            return distanceSet;
        }

////
////
//            if ((sa = ageCache.get(text)) == null) {
//                ageCache.put(text, sa = new AgeInterpreter(text));
//            }
//
//            if (sa.isInterpretable()) {
//                return ageSet;
//            }


        return reverseDictionary.getOrDefault(text, Collections.emptySet());
    }

    public String convertToString(List<DocumentToken> tokens) {
        StringBuilder sb = new StringBuilder();
        for (DocumentToken token : tokens) {
            sb.append(token.getText());
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    public boolean isDosage(String text) {
        return text.matches("[0-9,.]+ ?[A-Za-z]+/[0-9]* ?[A-Za-z]+|[0-9]+ ?[a-zA-Z]+");
    }

    public boolean isDistance(String text){
        return text.matches("[0-9]+ ?(mm|cm|dm|centimeter[s]?|decimeter[s]|meter[s]?|m|km|kilometer[s]?)");
    }

}
