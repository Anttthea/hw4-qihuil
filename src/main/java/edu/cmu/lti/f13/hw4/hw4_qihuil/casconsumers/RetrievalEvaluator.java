package edu.cmu.lti.f13.hw4.hw4_qihuil.casconsumers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f13.hw4.hw4_qihuil.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_qihuil.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_qihuil.utils.Utils;

public class RetrievalEvaluator extends CasConsumer_ImplBase {

	/** query id number **/
	public ArrayList<Integer> qIdList;

	/** query and text relevant values **/
	public ArrayList<Integer> relList;

	/** query and document tokens frequency **/
	public ArrayList<Map<String, Integer>> freqList;

	/** rank list by cosine similarity **/
	public ArrayList<Integer> ranking;

	public void initialize() throws ResourceInitializationException {

		qIdList = new ArrayList<Integer>();

		relList = new ArrayList<Integer>();

		freqList = new ArrayList<Map<String, Integer>>();

		ranking = new ArrayList<Integer>();

	}

	/**
	 * TODO :: 1. construct the global word dictionary 2. keep the word
	 * frequency for each sentence
	 */
	@Override
	public void processCas(CAS aCas) throws ResourceProcessException {

		JCas jcas;
		try {
			jcas = aCas.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}

		FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();

		if (it.hasNext()) {
			Document doc = (Document) it.next();

			// Make sure that your previous annotators have populated this in
			// CAS
			FSList fsTokenList = doc.getTokenList();
			ArrayList<Token> tokenList = Utils.fromFSListToCollection(
					fsTokenList, Token.class);
			Map<String, Integer> ma = new HashMap<String, Integer>();
			Iterator<Token> iter = tokenList.iterator();
			while (iter.hasNext()) {
				Token tok = iter.next();
				ma.put(tok.getText().toLowerCase(), tok.getFrequency());
			}
			qIdList.add(doc.getQueryID());
			relList.add(doc.getRelevanceValue());
			freqList.add(ma);
			// Do something useful here

		}
	}

	/**
	 * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2.
	 * Compute the MRR metric
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {

		super.collectionProcessComplete(arg0);

		// TODO :: compute the cosine similarity measure
		// arraylist cosSim stores cosine similarity for each document,if is
		// query equals 99
		ArrayList<Double> cosSim = new ArrayList<Double>();
		Iterator<Integer> it = qIdList.iterator();
		int count = 0;
		// Number of queries
		while (it.hasNext()) {
			// assume all valid qIds are greater than 0
			int qId = it.next();
			if (relList.get(count) != 99) {
				System.out.println("Document order error!");
				break;
			}
			cosSim.add(99.0);
			int query = count;
			int qId2 = 0;
			if (it.hasNext())
				qId2 = qIdList.get(++count);
			while (qId == qId2) {
				qId2 = it.next();
				double cs = computeCosineSimilarity(freqList.get(query),
						freqList.get(count));
				cosSim.add(cs);
				if (it.hasNext())
					qId2 = qIdList.get(++count);
				else
					break;
			}
		}

		// TODO :: compute the rank of retrieved sentences
		Iterator<Integer> it1 = qIdList.iterator();
		int count1 = 0;
		while (it1.hasNext()) {
			// assume all valid qIds are greater than 0
			int qId = it1.next();
			if (relList.get(count1) != 99) {
				System.out.println("Document order error!");
				break;
			}
			int query = count1;
			int qId2 = 0;
			if (it1.hasNext())
				qId2 = qIdList.get(++count1);
			int trueDoc = query;
			int rank = 1;
			int sent = 0;
			while (qId == qId2) {
				qId2 = it1.next();
				sent++;
				if (relList.get(count1) == 1) {
					trueDoc = count1;
					for (int i = query + 1; i < count1; i++) {
						if (cosSim.get(i) > cosSim.get(trueDoc))
							rank++;
					}
					break;
				}
				if (it1.hasNext())
					qId2 = qIdList.get(++count1);
				else
					break;
			}
			if (qId != qId2) {
				System.out.println("No true document exists!");
				break;
			} else {
				qId2 = 0;
				if (it1.hasNext())
					qId2 = qIdList.get(++count1);
				while (qId == qId2) {
					qId2 = it1.next();
					sent++;
					if (cosSim.get(count1) > cosSim.get(trueDoc))
						rank++;
					if (it1.hasNext())
						qId2 = qIdList.get(++count1);
					else
						break;
				}
			}
			// output result
			System.out.print("Score: " + cosSim.get(trueDoc));
			System.out.print(" Rank: " + rank);
			System.out.print(" rel: " + relList.get(trueDoc));
			System.out.print(" qid: " + qId);
			System.out.print(" sent: " + sent + "\n");
			ranking.add(rank);
		}

		// TODO :: compute the metric:: mean reciprocal rank
		double metric_mrr = compute_mrr();
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
	}

	/**
	 * 
	 * @return cosine_similarity
	 */
	private double computeCosineSimilarity(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		double cosine_similarity = 0.0;

		// TODO :: compute cosine similarity between two sentences
		double docMul = 0;
		double qAbsol = 0;
		double dAbsol = 0;
		for (Map.Entry<String, Integer> entry : queryVector.entrySet()) {
			String key = entry.getKey();
			if (docVector.containsKey(key)) {
				docMul = docMul + docVector.get(key) * entry.getValue();
			}
			double incre = entry.getValue();
			qAbsol = qAbsol + incre * incre;
		}
		for (Map.Entry<String, Integer> entry1 : docVector.entrySet()) {
			double incre1 = entry1.getValue();
			dAbsol = dAbsol + incre1 * incre1;
		}
		cosine_similarity = docMul
				/ (Math.pow(qAbsol, 0.5) * Math.pow(dAbsol, 0.5));
		return cosine_similarity;
	}

	/**
	 * 
	 * @return mrr
	 */
	private double compute_mrr() {
		double metric_mrr = 0.0;

		// TODO :: compute Mean Reciprocal Rank (MRR) of the text collection
		int qsize = ranking.size();
		Iterator<Integer> itr = ranking.iterator();
		double temp = 0.0;
		while (itr.hasNext()) {
			int rank = itr.next();
			temp = temp + 1 / rank;
		}
		metric_mrr = temp / qsize;
		return metric_mrr;
	}

}
