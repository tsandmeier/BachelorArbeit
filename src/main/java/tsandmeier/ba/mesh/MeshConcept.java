package tsandmeier.ba.mesh;

import java.util.List;

public class MeshConcept {

    List<MeshTerm> terms;
    String name;

    public MeshConcept(String name, List<MeshTerm> terms){
        this.terms = terms;
    }

    public List<MeshTerm> getTerms() {
        return terms;
    }

    public String getName() {
        return name;
    }
}
