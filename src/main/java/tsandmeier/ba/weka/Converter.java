package tsandmeier.ba.weka;

import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.ShuffleCorpusDistributor;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.init.specifications.SystemScope;
import tsandmeier.ba.specs.NERLASpecsGroupName;
import tsandmeier.ba.templates.AMFLTemplate;
import tsandmeier.ba.weka.DataPointCollector;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Converter {

    private DataPointCollector trainingDataCollector = new DataPointCollector();


    private static final String CLASSIFICATION_LABEL_YES = "Y";

    private static final String CLASSIFICATION_LABEL_NO = "N";

    private static final String CLASSIFICATION_LABEL_UNKOWN = "?";


    private final Attribute classAttribute = new Attribute("classLabel",
            Arrays.asList(CLASSIFICATION_LABEL_NO, CLASSIFICATION_LABEL_YES, CLASSIFICATION_LABEL_UNKOWN));



    public static void main(String[] args) throws IOException {
        new Converter();
    }

    public Converter() throws IOException {
        String instanceDirectory = "src/main/resources/examples/nerla/group_name/corpus/instances/";

        InstanceProvider.maxNumberOfAnnotations = 10000;

        SystemScope.Builder.getScopeHandler()
                .addScopeSpecification(NERLASpecsGroupName.csvSpecsReader)
                .build();

        InstanceProvider instanceProvider = new InstanceProvider(new File(instanceDirectory));

        collectInstances(instanceProvider.getInstances(), trainingDataCollector, true);

        Instances wekaTRAINInstance = convertToWekaInstances("TEST", trainingDataCollector.getDataPoints());

        saveArff(new File("arff/groupNameClustering_train.arff"), wekaTRAINInstance);
    }

    private void collectInstances(List<Instance> instances, DataPointCollector collector, boolean training) {

//        Map<Boolean, Set<GroupNamePair>> dataSet = GroupNameDataSetHelper.getGroupNameClusterDataSet(instances);

        for (DataPointCollector.DataPoint fdp : convertToDataPoints(instances.get(0).getGoldAnnotations().getAnnotations(), training)) {
            collector.addFeatureDataPoint(fdp);
        }
//        for (DataPointCollector.DataPoint fdp : convertToDataPoints(dataSet.get(true), training)) {
//            collector.addFeatureDataPoint(fdp);
//        }

    }

    private List<DataPointCollector.DataPoint> convertToDataPoints(Collection<GroupNamePair> pairs, boolean training) {

        List<DataPointCollector.DataPoint> dataPoints = new ArrayList<>();

        for (GroupNamePair groupNamePair : pairs) {
            Map<String, Object> parameter = new HashMap<>();

            parameter.put("docID", groupNamePair.groupName1.asInstanceOfDocumentLinkedAnnotation().document.documentID);

            DataPointCollector.DataPoint fdp = new DataPointCollector.DataPoint(parameter, trainingDataCollector, getFeatures(groupNamePair),
                    groupNamePair.sameCluster ? 1D : 0D, training);
            fdp.parameter.put("groupNamePair", groupNamePair);
            dataPoints.add(fdp);
        }
        return dataPoints;

    }

    private List<DataPointCollector.DataPoint> convertToDataPoints(List<DocumentLinkedAnnotation> annotations, boolean training) {

        List<DataPointCollector.DataPoint> dataPoints = new ArrayList<>();

        for (DocumentLinkedAnnotation annotation : annotations) {
            Map<String, Object> parameter = new HashMap<>();

            parameter.put("docID", annotation.document.documentID);

            DataPointCollector.DataPoint fdp = new DataPointCollector.DataPoint(parameter, trainingDataCollector, getFeatures(annotation),
                    2.0, training);
            fdp.parameter.put("Annotation", annotation);
            dataPoints.add(fdp);
        }
        return dataPoints;

    }

    private Map<String, Double> getFeatures(GroupNamePair groupNamePair) {

        Map<String, Double> features = new HashMap<>();

        FeaturesFactory.set(features, groupNamePair);
        FeaturesFactory.levenshtein();
        FeaturesFactory.overlap();
        FeaturesFactory.charBasedNGrams();

        return features;
    }


    private Map<String, Double> getFeatures(DocumentLinkedAnnotation annotation) {

        Map<String, Double> features = new HashMap<>();

        features.put("VERSUCH", 5.0);

        return features;
    }

    private Instances convertToWekaInstances(final String dataSetName, final List<DataPointCollector.DataPoint> dataPoints) {

        Attribute[] attributes = new Attribute[trainingDataCollector.sparseIndexMapping.size()];

        for (Map.Entry<String, Integer> attribute : trainingDataCollector.sparseIndexMapping.entrySet()) {
            attributes[attribute.getValue()] = new Attribute(attribute.getKey());
        }

        ArrayList<Attribute> attributeList = new ArrayList<>();
        attributeList.addAll(Arrays.asList(attributes));
        Instances instances = new Instances(dataSetName, attributeList, dataPoints.size());

        attributeList.add(classAttribute);

        instances.setClassIndex(attributeList.size() - 1);

        for (DataPointCollector.DataPoint fdp : dataPoints) {
            double[] attValues = new double[attributeList.size()];

            for (Map.Entry<Integer, Double> d : fdp.features.entrySet()) {
                attValues[d.getKey()] = d.getValue();
            }
            attValues[attributeList.size() - 1] = fdp.score;
            instances.add(new SparseInstance(1, attValues));
        }

        return instances;
    }

    private void saveArff(final File arffOutputFile, Instances dataSet) throws IOException {
        ArffSaver saver = new ArffSaver();
        saver.setInstances(dataSet);
        saver.setFile(arffOutputFile);
        saver.writeBatch();
    }

}
