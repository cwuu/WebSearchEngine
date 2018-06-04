package phase1;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import java.util.Vector;
import java.io.IOException;
import java.io.Serializable;
import IRUtilities.*;
import java.io.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Test{

	public static void main(String[] args){
		try{

			RecordManager recman = RecordManagerFactory.createRecordManager("fetch");
			Indexer indexer = new Indexer(recman);

			
			
			File outputFile = new File("Spider_result.txt");
			PrintWriter writer = new PrintWriter(outputFile);

			for(int i=0;i<200;i++) {
				Page currentPage = indexer.getPageContent(i);
				System.out.println(currentPage.getTitle());
				System.out.println(currentPage.getURL());
				System.out.println(currentPage.getModifiedDate().toString());
				System.out.println(currentPage.getPageSize());
				writer.print(currentPage.getTitle());
				writer.print("\n");
				writer.print(currentPage.getURL());
				writer.print("\n");
				writer.print(currentPage.getModifiedDate().toString());
				writer.print("\n");
				writer.print(currentPage.getPageSize());
				writer.print("\n");

				Vector<Integer> wordList = indexer.getWordList(i);

				

				if(wordList != null)
					for(int j=0; j<wordList.size();j++) {
						String word = indexer.getWord(wordList.elementAt(j).intValue());
						int freq = indexer.getFrequency(wordList.elementAt(j).intValue(), i);
						System.out.print("("+word+", "+freq+") ");
						writer.print("("+word+", "+freq+") ");

						if((j+1)%10 == 0)
							System.out.println();



					}
				System.out.println();
				writer.print("\n");
				writer.print("\n");

				System.out.println();
				Vector<Integer> linkList = indexer.getChildLink(i);
				if(linkList != null)
					for(int j=0; j<linkList.size();j++) {
						System.out.println(indexer.getURL(linkList.elementAt(j).intValue()));
						writer.print(indexer.getURL(linkList.elementAt(j).intValue()));
						writer.print("\n");
					}
				System.out.println("-------------------------------------------------------------------------------------------------");
				writer.print("-------------------------------------------------------------------------------------------------");
				writer.print("\n");
			}
			indexer.finalize();
			writer.close();

			/*RecordManager recman = RecordManagerFactory.createRecordManager("test");
			Indexer indexer = new Indexer(recman);
			String url = "www.helloworld.com";
			indexer.insertURLID(url, 1);
			Page hello = new Page("hello world testing program", url, null, 200);
			indexer.insertPage(1, hello);
			Vector<String> title = new Vector<String>();
			title.add("hello");title.add("world");title.add("testing");title.add("program");
			indexer.removeStopWord(title);
			indexer.wordStem(title);
			System.out.println(indexer.getURLID(url));
			System.out.println(indexer.getURL(1));
			Vector<String> words = new Vector<String>();
			words.add("This");words.add("is");words.add("a");words.add("testing");words.add("program");//words.add("program");
			for(int i=0;i<words.size();i++){System.out.print(words.elementAt(i)+" ");}
			System.out.println();
			indexer.removeStopWord(words);
			for(int i=0;i<words.size();i++){System.out.print(words.elementAt(i)+" ");}
			System.out.println();
			indexer.wordStem(words);
			for(int i=0;i<words.size();i++){System.out.print(words.elementAt(i)+" ");}
			System.out.println();
			for(int i=0;i<words.size();i++) {
				indexer.insertWordID(words.elementAt(i), i);
			}
			for(int i=0;i<words.size();i++) {
				System.out.print(indexer.getWordID(words.elementAt(2-i))+indexer.getWord(2-i)+" ");
			}
			System.out.println();
			for(int i=0;i<words.size();i++) {
				int wordID = indexer.getWordID(words.elementAt(i));
				indexer.insertBodyWord(wordID, 1, 1);
				indexer.insertForward(1, wordID);
			}
			Page p = indexer.getPageContent(1);
			System.out.println(p.getTitle());
			System.out.println(p.getURL());
			System.out.println(p.getPageSize());
			indexer.insertBodyWord(2, 1, 2);
			for(int i=0;i<words.size();i++) {
				int wordID = indexer.getWordID(words.elementAt(i));
				System.out.print(indexer.getWord(wordID)+indexer.getFrequency(wordID, 1)+" ");
			}
			recman.commit();
			recman.close();	*/
		}
		catch(Exception e){
			System.err.println(e.toString());
		}
	}

}
