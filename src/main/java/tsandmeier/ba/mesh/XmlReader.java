package tsandmeier.ba.mesh;

import org.apache.lucene.index.Term;
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

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Load the input XML document, parse it and return an instance of the
        // Document class.
        Document document = builder.parse(new File(fileName));

        List<MeshDescriptor> descriptorList = new ArrayList<MeshDescriptor>();

        Node descriptorRecordSet = document.getDocumentElement();

        NodeList descriptorRecords = descriptorRecordSet.getChildNodes();
        for (int i = 0; i < descriptorRecords.getLength(); i++) {

            if (descriptorRecords.item(i).getNodeName().equals("DescriptorRecord")) {
                Node descriptorRecord = descriptorRecords.item(i);

                Element descriptorRecordElem = (Element) descriptorRecord;

                NodeList descriptorNameNode = descriptorRecordElem.getElementsByTagName("DescriptorName");

                String descriptorName = descriptorNameNode.item(0).getChildNodes().item(1).getChildNodes().item(0).getNodeValue();

                List<MeshConcept> conceptList = new ArrayList<>();
                Node conceptListNode = descriptorRecordElem.getElementsByTagName("ConceptList").item(0);
                for (int j = 0; j < conceptListNode.getChildNodes().getLength(); j++) {

                    if (conceptListNode.getChildNodes().item(j).getNodeName().equals("Concept")) {

                        Element conceptNodeElem = (Element) conceptListNode.getChildNodes().item(j);
                        String conceptName = conceptNodeElem.getElementsByTagName("ConceptName").item(0).getChildNodes().item(1).getChildNodes().item(0).getNodeValue();

                        List<MeshTerm> termList = new ArrayList<>();

                        Node termListNode = conceptNodeElem.getElementsByTagName("TermList").item(0);
                        for (int k = 0; k < termListNode.getChildNodes().getLength(); k++) {
                            if (termListNode.getChildNodes().item(k).getNodeName().equals("Term")) {
                                Element termNodeElem = (Element) termListNode.getChildNodes().item(k);

                                String termName = termNodeElem.getElementsByTagName("String").item(0).getChildNodes().item(0).getNodeValue();
                                termList.add(new MeshTerm(termName));

                            }
                        }
                        conceptList.add(new MeshConcept(conceptName, termList));
                    }
                }
                descriptorList.add(new MeshDescriptor(descriptorName, conceptList));
            }
        }

//                // Get the value of the ID attribute.
//                String ID = node.getAttributes().getNamedItem("ID").getNodeValue();
//
//                // Get the value of all sub-elements.
//                String firstname = elem.getElementsByTagName("Firstname")
//                        .item(0).getChildNodes().item(0).getNodeValue();
//
//                String lastname = elem.getElementsByTagName("Lastname").item(0)
//                        .getChildNodes().item(0).getNodeValue();
//
//                Integer age = Integer.parseInt(elem.getElementsByTagName("Age")
//                        .item(0).getChildNodes().item(0).getNodeValue());
//
//                Double salary = Double.parseDouble(elem.getElementsByTagName("Salary")
//                        .item(0).getChildNodes().item(0).getNodeValue());
//
//                employees.add(new Employee(ID, firstname, lastname, age, salary));
//            }
//        }

        // Print all employees.
//        for (Employee empl : employees)
//            System.out.println(empl.toString());
    }
}
