package edu.cmu.lti.f13.hw4.hw4_qihuil.annotators;


import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;










import edu.cmu.lti.f13.hw4.hw4_qihuil.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_qihuil.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_qihuil.utils.Utils;
public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		FSIterator iter = jcas.getAnnotationIndex(Document.type).iterator();
		while (iter.hasNext()) {
			Document doc = (Document) iter.next();
			createTermFreqVector(jcas, doc);
		}

	}
	/**
	 * 
	 * @param jcas
	 * @param doc
	 */

	private void createTermFreqVector(JCas jcas, Document doc) {

		String docText = doc.getText();
		
		//TO DO: construct a vector of tokens and update the tokenList in CAS
		StringTokenizer st = new StringTokenizer(docText);
		FSList l = new FSList(jcas);
		Vector<Token> vec = new Vector<Token>(st.countTokens());
	     while (st.hasMoreTokens()) {
	         Token tok = new Token(jcas);
	         String token = st.nextToken();
	         tok.setText(token);
	         int occur = StringUtils.countMatches(docText, token);
	         tok.setFrequency(occur);
	         tok.addToIndexes();
	         vec.add(tok);
	     }
	     l = Utils.fromCollectionToFSList(jcas, vec);
	     doc.setTokenList(l);
	   //Adding populated FeatureStructure to CAS
		//	jcas.addFsToIndexes(doc);
	}

}
