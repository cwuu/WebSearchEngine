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

public class Indexer
{

	private MappingTable pageID;
	private MappingTable wordID;
	private InvertedIndex titleWordIndex;
	private InvertedIndex bodyWordIndex;
	private Forward forwardIndex;
	private RecordManager recman;
	private PageContent pageContent;
	private ForwardIndex childIndex;
	private ForwardIndex parentIndex;

	public Indexer(RecordManager recordmanager) throws IOException
	{
		recman = recordmanager;
		pageID = new MappingTable(recman, "page_id", "page_id_inverse");
		wordID = new MappingTable(recman, "word_id", "word_id_inverse");
		titleWordIndex = new InvertedIndex(recman, "title_inverted_index");
		bodyWordIndex = new InvertedIndex(recman, "body_inverted_index");
		forwardIndex = new Forward(recman, "forward_index");
		pageContent = new PageContent(recman, "page_content");
		childIndex = new ForwardIndex(recman, "child_link");
		parentIndex = new ForwardIndex(recman, "parent_link");
	}

	public void finalize() throws IOException
	{
		recman.commit();
		recman.close();
	}

	public static void removeStopWord(Vector<String> content) throws IOException
	{
		StopStem st = new StopStem("stopwords.txt");
		for(int i = 0; i < content.size(); i++){
			if(st.isStopWord(content.elementAt(i))){
				content.remove(i);
				i--;
			}
		}
	}

 
	public int getWordIDSize() throws IOException
	{
		return wordID.getSize();
	}     

	public int getPageIDSize() throws IOException
	{
		return pageID.getSize();
	}


	public static void wordStem(Vector<String> content) throws IOException
	{
		StopStem st = new StopStem("stopwords.txt");
		for(int i = 0; i < content.size(); i++)
			content.setElementAt(st.stem(content.elementAt(i)), i);
	}//use after removing stop words

	public boolean insertURLID(String url, int id) throws IOException
	{
		if(url == null)
			return true;
		return pageID.addEntry(url, id);
	}

	public int getURLID(String url) throws IOException
	{
		return pageID.getID(url);
	}	

	public String getURL(int id) throws IOException
	{
		return pageID.getURL(id);
	}

	public boolean insertWordID(String word, int id) throws IOException
	{
		if(word == null)
			return true;
		return wordID.addEntry(word, id);
	}

	public int getWordID(String word) throws IOException
	{
		return wordID.getID(word);
	}

	public String getWord(int id) throws IOException
	{
		return wordID.getWord(id);
	}

	public boolean insertTitleWord(int wordID, int pageID, int freq, int pos) throws IOException
	{
		return titleWordIndex.addEntry(wordID, pageID, freq, pos);
	}

	public boolean insertBodyWord(int wordID, int pageID,int freq, int pos) throws IOException
	{
		return bodyWordIndex.addEntry(wordID, pageID, freq, pos);
	}

	public void insertForward(int pageID, int wordID, int freq) throws IOException
	{
		forwardIndex.addEntry(pageID, wordID, freq);
		//forwardIndex.calculateMaxTermFrequency(pageID);
	}
	
	public int getMaxTermFrequency(int pageID) throws IOException
	{
		return forwardIndex.getMaxTermFrequency(pageID);
	}

	public int getPageSize(int pageID) throws IOException
	{
		return forwardIndex.getPageSize(pageID);
	}

	public void insertPage(int pageID, Page page) throws IOException
	{
		pageContent.addEntry(pageID, page);
	}

	public Page getPageContent(int pageID) throws IOException
	{
		return pageContent.getPageContent(pageID);
	}

	public int getFrequency(int wordID, int pageID) throws IOException
	{
		return titleWordIndex.getFrequency(wordID, pageID)+bodyWordIndex.getFrequency( wordID, pageID);
	}

	public int getDocumentFrequency(int wordID) throws IOException
	{
		return bodyWordIndex.getDocumentFrequency(wordID);
	}
	
	
	public void insertChildLink(int parentID, int childID) throws IOException
	{
		childIndex.addEntry(parentID, childID);
	}



	public void insertParentLink(int childID, int parentID) throws IOException
	{
		parentIndex.addEntry(childID, parentID);
	}

	public Vector<Integer> getWordList(int pageID) throws IOException
	{
		return forwardIndex.getWords(pageID);
	}

	public Vector<Integer> getChildLink(int pageID) throws IOException
	{
		return childIndex.getLinks(pageID);
	}

	public Vector<Integer> getParentLink(int pageID) throws IOException
	{
		return parentIndex.getLinks(pageID);
	}
	
	public Vector<Posting> getTitleInveredIndexPosting(int wordID) throws IOException
	{
		return titleWordIndex.get(wordID);
	}

	public Vector<Posting> getBodyInveredIndexPosting(int wordID) throws IOException
	{
		return bodyWordIndex.get(wordID);
	}
	
	public boolean containsWordPos(int pageID,int wordID,int pos) throws IOException
	{
		return bodyWordIndex.containsWordPos(pageID, wordID, pos);
	}
	
    public Vector<Integer> getPoslist(int wordID,int pageID) throws IOException
    {
    	return bodyWordIndex.getPoslist(wordID,pageID);

    }
    
    public Vector<fPair> getTopFiveWord(int pageID) throws IOException
    {
    	return forwardIndex.getTopFiveID(pageID);
    }
    
    public Vector<fPair> getTopTenWord(int pageID) throws IOException
    {
    	return forwardIndex.getTopTenID(pageID);
    }
}
/*this class is used for create and control database
 *  provide	        1.method to remove stop words
 * 			2.method to transform words to stems
 * 			3.method to insert words and url into database
 * Notice: crawler should make sure all the information are inserted, also a 
 * recordmanager should be passed to crawler to construct indexer
 */
