package roopaliv.project2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 * Hello world!
 *
 */
public class App 
{

	

	public static void main(String[] args) throws IOException {
		String indexPath = "/Users/paali/Documents/First Semester/Info retrieval/project2/index";
		String inputFilePath = "/Users/paali/Documents/First Semester/Info retrieval/project2/input.txt";
		String outputFilePath = "/Users/paali/Documents/First Semester/Info retrieval/project2/output.txt";
		if (args.length > 0) {
			indexPath = args[0];
			inputFilePath = args[1];
			outputFilePath = args[2];
		}
		File indexDirectory = new File(indexPath);
		BufferedReader inputReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(inputFilePath), "UTF8"));
		String query;
		List<String> queryTerms = new ArrayList<String>();
		while ((query = inputReader.readLine()) != null) {
			queryTerms.add(query);
		}
		inputReader.close();
		Path path = indexDirectory.toPath();
		IndexReader reader = DirectoryReader.open(FSDirectory.open(path));
		int termCount = 0;
		int docCount = reader.numDocs(); // 45314
		HashMap<String, LinkedList<Integer>> termsDic = new HashMap<String, LinkedList<Integer>>();
		Fields indexedFields = MultiFields.getFields(reader);
		{
			for (String field : indexedFields) {
				if (field.equals("text_nl") || field.equals("text_fr") || field.equals("text_de")
						|| field.equals("text_ja") || field.equals("text_ru") || field.equals("text_pt")
						|| field.equals("text_es") || field.equals("text_it") || field.equals("text_da")
						|| field.equals("text_no") || field.equals("text_sv")) {
					TermsEnum termEnum = MultiFields.getTerms(reader, field).iterator();
					BytesRef term;
					while ((term = termEnum.next()) != null) {
						PostingsEnum postingsEnum = MultiFields.getTermDocsEnum(reader, field, term,
								PostingsEnum.FREQS);
						LinkedList<Integer> postingsList = new LinkedList<Integer>();
						while (postingsEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
							postingsList.add(postingsEnum.docID());
						}
						termsDic.put(term.utf8ToString(), postingsList);
						termCount++;
					}
				}

			}
		}
		reader.close();

		BufferedWriter outputWriter = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(outputFilePath), "UTF-8"));

		for (String queryLine : queryTerms) {
			LinkedList<Integer> taatAndResults = new LinkedList<Integer>();
			int taatAndDocs = 0;
			int taatAndComparisions = 0;

			LinkedList<Integer> taatOrResults = new LinkedList<Integer>();
			int taatOrDocs = 0;
			int taatOrComparisions = 0;

			LinkedList<Integer> daatAndResults = new LinkedList<Integer>();
			int daatAndDocs = 0;
			int daatAndComparisions = 0;

			LinkedList<Integer> daatOrResults = new LinkedList<Integer>();
			int daatOrDocs = 0;
			int daatOrComparisions = 0;

			LinkedList<LinkedList<Integer>> queryTermsPostings = new LinkedList<LinkedList<Integer>>();

			for (String term : queryLine.split(" ")) {
				queryTermsPostings.add(termsDic.get(term));

				outputWriter.write("GetPostings");
				outputWriter.newLine();
				outputWriter.write(term);
				outputWriter.newLine();
				outputWriter.write("Postings list:");
				int num = 0;
				while (termsDic.get(term).size() > num) {
					outputWriter.write(" " + termsDic.get(term).get(num));
					num++;
				}
				outputWriter.newLine();
			}

			// Term at a time - OR
			for (LinkedList<Integer> postings : queryTermsPostings) {
				for (int posting : postings) {
					boolean alreadyPresent = false;
					for (int id : taatOrResults) {
						taatOrComparisions++;
						if (id == posting) {
							alreadyPresent = true;
							break;
						}
					}
					if (!alreadyPresent)
						taatOrResults.add(posting);
				}
				Collections.sort(taatOrResults);
			}

			// Term at a time - AND
			LinkedList<Integer> tempTaatAnd = (LinkedList<Integer>) queryTermsPostings.get(0).clone();
			for (LinkedList<Integer> postings : queryTermsPostings) {
				if (queryTermsPostings.indexOf(postings) != 0) {
					for (int posting : postings) {
						boolean match = false;
						for (int p : tempTaatAnd) {
							taatAndComparisions++;
							if (p == posting) {
								match = true;
								break;
							}
						}
						if (match) {
							boolean alreadyPresent = false;
							for (int id : taatAndResults) {
								taatAndComparisions++;
								if (id == posting) {
									alreadyPresent = true;
									break;
								}
							}
							if (!alreadyPresent)
								taatAndResults.add(posting);
						}
						tempTaatAnd = (LinkedList<Integer>) taatAndResults.clone();
					}
				}
				Collections.sort(taatAndResults);
			}

			// Document at a time - OR
			LinkedList<Integer> tempdaatOrList = (LinkedList<Integer>) queryTermsPostings.clone();
			for (LinkedList<Integer> postings : queryTermsPostings) {
				
			}

			// Document at a time - AND
			LinkedList<Integer> tempdaatAndList = (LinkedList<Integer>) queryTermsPostings.clone();
			for (LinkedList<Integer> postings : queryTermsPostings) {
				
			}

			outputWriter.write("TaatAnd");
			outputWriter.newLine();
			outputWriter.write(queryLine);
			outputWriter.newLine();
			outputWriter.write("Results:");
			if (taatAndResults.size() == 0) {
				outputWriter.write(" empty");
			} else {
				int num = 0;
				while (taatAndResults.size() > num) {
					outputWriter.write(" " + taatAndResults.get(num));
					num++;
				}
			}
			outputWriter.newLine();
			outputWriter.write("Number of documents in results: ");
			outputWriter.write(Integer.toString(taatAndResults.size()));
			outputWriter.newLine();
			outputWriter.write("Number of comparisons: ");
			outputWriter.write(Integer.toString(taatAndComparisions));
			outputWriter.newLine();

			outputWriter.write("TaatOr");
			outputWriter.newLine();
			outputWriter.write(queryLine);
			outputWriter.newLine();
			outputWriter.write("Results:");
			if (taatOrResults.size() == 0) {
				outputWriter.write(" empty");
			} else {
				int num = 0;
				while (taatOrResults.size() > num) {
					outputWriter.write(" " + taatOrResults.get(num));
					num++;
				}
			}
			outputWriter.newLine();
			outputWriter.write("Number of documents in results: ");
			outputWriter.write(Integer.toString(taatOrResults.size()));
			outputWriter.newLine();
			outputWriter.write("Number of comparisons: ");
			outputWriter.write(Integer.toString(taatOrComparisions));
			outputWriter.newLine();

			outputWriter.write("DaatAnd");
			outputWriter.newLine();
			outputWriter.write(queryLine);
			outputWriter.newLine();
			outputWriter.write("Results:");
			if (daatAndResults.size() == 0) {
				outputWriter.write(" empty");
			} else {
				int num = 0;
				while (daatAndResults.size() > num) {
					outputWriter.write(" " + daatAndResults.get(num));
					num++;
				}
			}
			outputWriter.newLine();
			outputWriter.write("Number of documents in results: ");
			outputWriter.write(Integer.toString(daatAndResults.size()));
			outputWriter.newLine();
			outputWriter.write("Number of comparisons: ");
			outputWriter.write(Integer.toString(daatAndComparisions));
			outputWriter.newLine();

			outputWriter.write("DaatOr");
			outputWriter.newLine();
			outputWriter.write(queryLine);
			outputWriter.newLine();
			outputWriter.write("Results:");
			if (daatOrResults.size() == 0) {
				outputWriter.write(" empty");
			} else {
				int num = 0;
				while (daatOrResults.size() > num) {
					outputWriter.write(" " + daatOrResults.get(num));
					num++;
				}
			}
			outputWriter.newLine();
			outputWriter.write("Number of documents in results: ");
			outputWriter.write(Integer.toString(daatOrResults.size()));
			outputWriter.newLine();
			outputWriter.write("Number of comparisons: ");
			outputWriter.write(Integer.toString(daatOrComparisions));
			outputWriter.newLine();
		}

		outputWriter.flush();
		outputWriter.close();
		// System.out.println(docCount);
		// System.out.println(termCount);
		// System.out.println(termsDic.size());

	}
}
