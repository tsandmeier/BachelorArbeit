package tsandmeier.ba.templates;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;
import org.xml.sax.SAXException;
import tsandmeier.ba.mesh.MeshConcept;
import tsandmeier.ba.mesh.MeshDescriptor;
import tsandmeier.ba.mesh.MeshTerm;
import tsandmeier.ba.mesh.XmlReader;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * first word of a mention
 */
public class MedicalHeadingTemplate extends AbstractFeatureTemplate<MedicalHeadingTemplate.MedicalHeadingScope> {

	List<MeshDescriptor> descriptorList;

	public MedicalHeadingTemplate(boolean cache) throws IOException, SAXException, ParserConfigurationException {
		super(cache);
		this.descriptorList = new XmlReader("mesh/mesh.xml").getDescriptorList();
	}

	public MedicalHeadingTemplate() throws IOException, SAXException, ParserConfigurationException, UnirestException {
		super();
		System.out.println("ADD MEDICAL HEADING...");

//		URLConnection connection = new URL("https://id.nlm.nih.gov/mesh/lookup/descriptor?label=Wistar%20Rats&match=exact&limit=10").openConnection();
////		connection.setRequestProperty("header1", header1);
////		connection.setRequestProperty("header2", header2);
////Get Response
//		InputStream is = connection.getInputStream();
//		System.out.println(connection.getContentType());
//		System.out.println(connection.getContent());
//		System.out.println(connection.getHeaderField("http://id.nlm.nih.gov/mesh/vocab#preferredConcept"));

		HttpResponse<JsonNode> response = Unirest.get("https://id.nlm.nih.gov/mesh/lookup/descriptor?label=Wistar%20Rats&match=exact&limit=10")
				.asJson();
//		System.out.println(response.getStatus());
//		System.out.println(response.getHeaders().get("Content-Type"));
//		System.out.println(response.getBody().getObject().get("id"));

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(response.getBody().toString());
		String prettyJsonString = gson.toJson(je);
		System.out.println(prettyJsonString);



//		this.descriptorList = new XmlReader("mesh/mesh.xml").getDescriptorList();
		System.out.println("MEDICAL HEADING ADDED");
	}

	static class MedicalHeadingScope
			extends AbstractFactorScope {

		EntityType type;
		String medicalHeading;


		public MedicalHeadingScope(
				AbstractFeatureTemplate<MedicalHeadingScope> template, EntityType type, String medicalHeading) {
			super(template);
			this.type = type;
			this.medicalHeading = medicalHeading;
		}

		@Override
		public int implementHashCode() {
			return 0;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;
			MedicalHeadingScope that = (MedicalHeadingScope) o;
			return Objects.equals(type, that.type) &&
					Objects.equals(medicalHeading, that.medicalHeading);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), type, medicalHeading);
		}

		@Override
		public boolean implementEquals(Object obj) {
			return false;
		}
	}

	@Override
	public List<MedicalHeadingScope> generateFactorScopes(State state) {
		List<MedicalHeadingScope> factors = new ArrayList<>();

		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
			for(MeshDescriptor descriptor : descriptorList){
				for(MeshConcept concept : descriptor.getConcepts()){
					for(MeshTerm term : concept.getTerms()){
						if(term.getName().equals(annotation.getSurfaceForm())){
							factors.add(new MedicalHeadingScope(this, annotation.entityType, descriptor.getName()));
						}
					}
				}
			}
		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<MedicalHeadingScope> factor) {
		factor.getFeatureVector().set("Descriptor of <"+factor.getFactorScope().type.name + ">: " + factor.getFactorScope().medicalHeading, true);
	}

}
