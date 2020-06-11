package tsandmeier.ba.mesh;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XmlReader {

    String fileName;

    public List<MeshDescriptor> getDescriptorList() {
        return descriptorList;
    }

    List<MeshDescriptor> descriptorList = new ArrayList<>();

    public static void main(String args[]) throws IOException, ParserConfigurationException, SAXException {
        new XmlReader("mesh/mesh_short.xml");
    }


    public XmlReader(String fileName) throws ParserConfigurationException,
            IOException, SAXException {

        this.fileName = fileName;

        System.out.println("build Document");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Load the input XML document, parse it and return an instance of the
        // Document class.

        System.out.println("PARSE");
        Document document = builder.parse(new File(fileName));
        System.out.println("PARSED");

        List<MeshDescriptor> descriptorList = new ArrayList<MeshDescriptor>();

        Node descriptorRecordSet = document.getDocumentElement();

        NodeList descriptorRecords = descriptorRecordSet.getChildNodes();
        for (int i = 0; i < descriptorRecords.getLength(); i++) {

            if (descriptorRecords.item(i).getNodeName().equals("DescriptorRecord")) {

                System.out.println("im DescriptorRecord");

                Node descriptorRecord = descriptorRecords.item(i);

                Element descriptorRecordElem = (Element) descriptorRecord;

                NodeList descriptorNameNode = descriptorRecordElem.getElementsByTagName("DescriptorName");

                String descriptorName = descriptorNameNode.item(0).getChildNodes().item(1).getChildNodes().item(0).getNodeValue();

                List<MeshConcept> conceptList = new ArrayList<>();
                Node conceptListNode = descriptorRecordElem.getElementsByTagName("ConceptList").item(0);
                for (int j = 0; j < conceptListNode.getChildNodes().getLength(); j++) {

                    if (conceptListNode.getChildNodes().item(j).getNodeName().equals("Concept")) {

                        System.out.println("Im Concept");

                        Element conceptNodeElem = (Element) conceptListNode.getChildNodes().item(j);
                        String conceptName = conceptNodeElem.getElementsByTagName("ConceptName").item(0).getChildNodes().item(1).getChildNodes().item(0).getNodeValue();

                        List<MeshTerm> termList = new ArrayList<>();

                        Node termListNode = conceptNodeElem.getElementsByTagName("TermList").item(0);
                        for (int k = 0; k < termListNode.getChildNodes().getLength(); k++) {
                            if (termListNode.getChildNodes().item(k).getNodeName().equals("Term")) {
                                Element termNodeElem = (Element) termListNode.getChildNodes().item(k);

                                String termName = termNodeElem.getElementsByTagName("String").item(0).getChildNodes().item(0).getNodeValue();
                                termList.add(new MeshTerm(termName.toLowerCase()));

                            }
                        }
                        conceptList.add(new MeshConcept(conceptName.toLowerCase(), termList));
                    }
                }
                descriptorList.add(new MeshDescriptor(descriptorName.toLowerCase(), conceptList));
            }
        }
    }
}
