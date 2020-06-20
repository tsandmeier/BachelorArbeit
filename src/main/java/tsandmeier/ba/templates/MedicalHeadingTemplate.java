package tsandmeier.ba.templates;

import com.mashape.unirest.http.exceptions.UnirestException;
import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.State;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import tsandmeier.ba.mesh.MeshDescriptor;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * first word of a mention
 */
public class MedicalHeadingTemplate extends AbstractFeatureTemplate<MedicalHeadingTemplate.MedicalHeadingScope> {

    List<MeshDescriptor> descriptorList;

    public MedicalHeadingTemplate(boolean cache) {
        super(cache);
    }

    public MedicalHeadingTemplate() {
        super();
        System.out.println("ADD MEDICAL HEADING...");


        System.out.println("MEDICAL HEADING ADDED");
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
            InputStream is = new URL(url).openStream();
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String jsonText = readAll(rd);
                JSONObject json = new JSONObject(jsonText);
                return json;
            } finally {
                is.close();
            }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
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
        public boolean implementEquals(Object o) {
            return false;
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
    }

    @Override
    public List<MedicalHeadingScope> generateFactorScopes(State state) {
        List<MedicalHeadingScope> factors = new ArrayList<>();

        for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {

            JSONObject json = null;

            String text = annotation.getSurfaceForm().replace(" ", "%20");

            if(text.length() > 3  && !(text.charAt(0) == '\ufeff')) {

				try {
					json = readJsonFromUrl("https://id.nlm.nih.gov/mesh/sparql?query=PREFIX%20rdf%3A%20%3Chttp%3A%2F%2Fwww.w" +
							"3.org%2F1999%2F02%2F22-rdf-syntax-ns%23%3E%0D%0APREFIX%20rdfs%3A%20%3Chttp%3A%2F%2Fwww.w3.org%2F" +
							"2000%2F01%2Frdf-schema%23%3E%0D%0APREFIX%20xsd%3A%20%3Chttp%3A%2F%2Fwww.w3.org%2F2001%2FXMLSchem" +
							"a%23%3E%0D%0APREFIX%20owl%3A%20%3Chttp%3A%2F%2Fwww.w3.org%2F2002%2F07%2Fowl%23%3E%0D%0APREFIX%2" +
							"0meshv%3A%20%3Chttp%3A%2F%2Fid.nlm.nih.gov%2Fmesh%2Fvocab%23%3E%0D%0APREFIX%20mesh%3A%20%3Chttp%" +
							"3A%2F%2Fid.nlm.nih.gov%2Fmesh%2F%3E%0D%0APREFIX%20mesh2020%3A%20%3Chttp%3A%2F%2Fid.nlm.nih.gov%2" +
							"Fmesh%2F2020%2F%3E%0D%0APREFIX%20mesh2019%3A%20%3Chttp%3A%2F%2Fid.nlm.nih.gov%2Fmesh%2F2019%2F%3" +
							"E%0D%0APREFIX%20mesh2018%3A%20%3Chttp%3A%2F%2Fid.nlm.nih.gov%2Fmesh%2F2018%2F%3E%0D%0A%0D%0ASEL" +
							"ECT%20DISTINCT%20%3Fd%20%3FdName%20%0D%0AFROM%20%3Chttp%3A%2F%2Fid.nlm.nih.gov%2Fmesh%3E%0D%0AWH" +
							"ERE%20%7B%0D%0A%20%20%3Fd%20a%20meshv%3ADescriptor%20.%0D%0A%20%20%3Fd%20meshv%3Aterm%20%3Ft%20." +
							"%0D%0A%20%20%3Fd%20rdfs%3Alabel%20%3FdName%20.%0D%0A%20%20%3Ft%20rdfs%3Alabel%20%3FtName%0D%0A%20%20" +
							"FILTER(REGEX(%3FtName%2C%27" +
							annotation.getSurfaceForm().replace(" ", "%20") +
							"%27%2C%27i%27))%20%0D%0A%7D&format=JSON&limit=50&offset=0&inference=true");


					JSONArray bindings = (JSONArray) ((JSONObject) json.get("results")).get("bindings");

					for (Object object : bindings) {
						JSONObject jsonObject = (JSONObject) object;
						JSONObject dname = (JSONObject) jsonObject.get("dName");
						String name = dname.getString("value");
						factors.add(new MedicalHeadingScope(this, annotation.getEntityType(), name));
					}
				} catch (IOException e) {
//				e.printStackTrace();
//					System.out.println("No valid url for: " + annotation.getSurfaceForm());
				}

			}

        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<MedicalHeadingScope> factor) {
        factor.getFeatureVector().set("Descriptor of <" + factor.getFactorScope().type.name + ">: " + factor.getFactorScope().medicalHeading, true);
    }


}
