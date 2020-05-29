import com.hp.hpl.jena.ontology.ConversionException;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import edu.isi.karma.rep.alignment.Label;

Class AutoGenerateLiteral{

	private Resource r;
	
	private OntResource ontResource;

	private OntProperty ontProperty;

	private String rdfsType;

	public AutoGenerateLiteral(){
		
		this.r = null;
		
		this.ontResource = null;

		this.rdfsType = null;
	}
	
	public AutoGenerateLiteral( Resource r ){
		
		this.r = r;
		
		this.rdfsType = null;

		setOntResource(this.r);

	}
	
	public AutoGenerateLiteral( Resource r , String rdfsType){
		
		this.r = r;
		
		this.ontResource = null;

		this.rdfsType = rdfsType;

		setOntResource(this.r);

	}
	
	public void setResource( Resource r ){
	
		this.r = r;
	
	}
	
	public void setRdfsType(String rdfsType){
	
		this.rdfsType = rdfsType;
	

	}
	
	public Resource getResource(){
	
		return this.r;
	
	}
	
	public String getRdfsType(){
	
		return this.rdfsType
	
	}

	public void setOntResource( Resource r){

		try{
			
			this.ontResource = (OntResource)r;
		
		}catch(Exception e){

		}

	}

	public OntResource getOntResource(){

		return this.ontResource;
	
	}

	public void setOntProperty(OntResource ontR){

		if(getOntResource() != null){

			this.ontProperty = ontR.asProperty();
		
		}else{

			this.ontProperty = null;
		}
		

	}

	public OntProperty getOntProperty(){

		return this.ontProperty;
	
	}

	public static String getLiteralType(){

		OntResource ontR = getOntResource();
		
		Boolean checkProperty = ontR.isDatatypeProperty();
		
		String toAddType = "";

		if(checkProperty){

			setOntProperty(ontR);
			
			OntProperty ontP = getOntProperty();

			if(ontP != null){
			
				toAddType = String.valueOf(ontP.getRange());

				String[] findXMLSchema = toAddType.split("#");
        		
        		for(int index = ans.length-1 ; index >= 0 ; index++){
        			
        			String checkXML = ans[index];
        			
        			if(test.contains("XMLSchema")){
        			
        				toAddType = ans[index + 1];

        				break;
        			
        			}
        		
        		}
        		
			
				int length = toAddType.length();

				if( ( !toAddType.equals("") ) && length > 0){
			
					setRdfsType("xsd:");
			
					rdfsType = getRdfsType();
			
					rdfsType = rdfsType + toAddType

				}else{
			
					rdfsType = "";
			
					logger.error("Empty rdfs type");
			
				}

			}else{
			
				rdfsType = "";
			
				logger.error("Error in getting ontology Property");
			
			}

		}else{
			
			rdfsType = "";
			
			logger.error("Not a Data type Property");
		
		}
	
	}

}