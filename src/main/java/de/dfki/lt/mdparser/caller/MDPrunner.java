package de.dfki.lt.mdparser.caller;


import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import de.bwaldvogel.liblinear.*;
import de.dfki.lt.mdparser.archive.Archivator;
import de.dfki.lt.mdparser.data.Data;
import de.dfki.lt.mdparser.eval.Eval;
import de.dfki.lt.mdparser.features.Alphabet;
import de.dfki.lt.mdparser.parser.Parser;

public class MDPrunner {
	private String[] dirs = {"split","splitA","splitF","splitO","splitC","splitModels","temp"};
	private String algorithm = "covington";
	private String resultFile = "temp/1.conll";
	private Parser parser = new Parser();
	private Data data = null;
	private Eval evaluator = null;

	// Getters and setters
	
	public Eval getEvaluator() {
		return evaluator;
	}
	public void setEvaluator(Eval evaluator) {
		this.evaluator = evaluator;
	}
	public String getResultFile() {
		return resultFile;
	}
	public void setResultFile(String resultFile) {
		this.resultFile = resultFile;
	}
	public Data getData() {
		return data;
	}
	public void setData(Data data) {
		this.data = data;
	}
	
	public String getAlgorithm() {
		return this.algorithm;
	}
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	// Class instantiation
	public MDPrunner(){
	}

	public MDPrunner(String resultFile){	
		this.resultFile = resultFile;
	}

	// Methods

	public void conllFileParsingAndEval(String conllFile, String resultFile, String modelFile) 
			throws IOException{
		this.parser = new Parser();
		this.data = new Data(conllFile, false);
		this.resultFile = resultFile;
		System.out.println("No. of sentences: "+ data.getSentences().length);

		Archivator arch = new Archivator(modelFile,dirs);
		arch.extract();
		Alphabet alphabetParser = new Alphabet(arch.getParserAlphabetInputStream());
		parser.setNumberOfClassesParser(alphabetParser.getMaxLabelIndex()-1);


		parser.parseCombined(algorithm, data, arch, alphabetParser, false);

		this.getData().printToFile(resultFile);
		this.evalParser(conllFile, resultFile);

	}
	
	public void evalParser(String conllFile, String resultFile) throws IOException{
		evaluator = new Eval(conllFile, resultFile, 6, 6, 7, 7);
		System.out.println("Parent accuracy: " + evaluator.getParentsAccuracy());
		System.out.println("Label accuracy:  " + evaluator.getLabelsAccuracy());
	}


	public static void main(String[] args) throws IOException, InvalidInputDataException, NoSuchAlgorithmException {
		MDPrunner mdpRunner = new MDPrunner();
		String conllFile = "/Users/gune00/data/UniversalDependencies/conll/German/de-ud-test.conll";
		String resultFile = "/Users/gune00/data/UniversalDependencies/conll/German/de-ud-test-result.conll";
		String modelFile = "/Users/gune00/data/UniversalDependencies/conll/German/de-MDPmodel.zip";

		mdpRunner.conllFileParsingAndEval(conllFile, resultFile, modelFile);

	}
}
