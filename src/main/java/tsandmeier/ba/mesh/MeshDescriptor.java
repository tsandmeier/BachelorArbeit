package tsandmeier.ba.mesh;

import java.util.List;

public class MeshDescriptor {

    List<MeshConcept> concepts;
    String name;

    public MeshDescriptor(String name, List<MeshConcept> concepts){
        this.concepts = concepts;
        this.name = name;
    }

    public List<MeshConcept> getConcepts() {
        return concepts;
    }

    public String getName() {
        return name;
    }
}
