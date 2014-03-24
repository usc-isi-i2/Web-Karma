package edu.isi.karma.kr2rml;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.transformation.tokenizer.*;

import java.util.List;

public class PythonTransformationToTemplateTermSetBuilder extends PythonTransformationTokenVistor{

	private HNode referenceHNode;
	private RepFactory factory;
	private KR2RMLMappingColumnNameHNodeTranslator translator;
	private TemplateTermSet tts;
	public PythonTransformationToTemplateTermSetBuilder(KR2RMLMappingColumnNameHNodeTranslator translator, RepFactory factory)
	{
		this.translator = translator;
		this.factory = factory;
	}
	@Override
	protected
	void visit(PythonTransformationToken pythonTransformationToken) {
		
	}

	protected
	void visit(PythonTransformationColumnToken pythonTransformationToken) {
		
		HNode columnHNode = referenceHNode.getNeighborByColumnName(pythonTransformationToken.toString(), factory);
		String columnName = translator.getColumnNameForHNodeId(columnHNode.getId());
		tts.addTemplateTermToSet(new ColumnTemplateTerm(columnName));
		
	}
	
	protected void visit(PythonTransformationStringToken pythonTransformationToken) {
		tts.addTemplateTermToSet(new StringTemplateTerm(pythonTransformationToken.toString()));

	}
	
	protected void visit(PythonTransformationInvalidToken pythonTransformationToken) {
	
	}
	
	public TemplateTermSet translate(String transformationCode, String referenceHNodeId)
	{
		List<PythonTransformationToken> tokens = PythonTransformationAsURITokenizer.tokenize(transformationCode);
		return translate(tokens, referenceHNodeId);
		
	}
	public TemplateTermSet translate(List<PythonTransformationToken> tokens, String referenceHNodeId) {
		tts = new TemplateTermSet();
		referenceHNode = factory.getHNode(referenceHNodeId);
		this.visit(tokens);
		return tts;
	}

}
