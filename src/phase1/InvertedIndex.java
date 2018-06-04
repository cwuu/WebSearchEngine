/* --
COMP4321 Lab1 Exercise
Student Name: xjian
Student ID:
Section:
Email: xjian@ust.hk
 */
package phase1;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import java.util.Vector;
import java.io.IOException;
import java.util.*;

public class InvertedIndex
{
	private RecordManager recman;
	private HTree hashtable;

	InvertedIndex(String recordmanager, String objectname) throws IOException
	{
		recman = RecordManagerFactory.createRecordManager(recordmanager);
		long recid = recman.getNamedObject(objectname);

		if (recid != 0)
			hashtable = HTree.load(recman, recid);
		else
		{
			hashtable = HTree.createInstance(recman);
			recman.setNamedObject( objectname, hashtable.getRecid() );
		}
	}

	InvertedIndex(RecordManager recordmanager, String objectname) throws IOException
	{
		this.recman = recordmanager;
		long recid = recman.getNamedObject(objectname);

		if(recid != 0)
			hashtable = HTree.load(recman, recid);
		else
		{
			hashtable = HTree.createInstance(recman);
			recman.setNamedObject( objectname, hashtable.getRecid() );
		}
	}

	public void finalize() throws IOException
	{
		recman.commit();
		recman.close();                
	} 

	public boolean addEntry(int wordID, int pageID, int freq, int pos) throws IOException
	{
		// ADD YOUR CODES HERE
		boolean word_exist = false;
		Vector<Posting> content = (Vector<Posting>)hashtable.get(wordID);
		if (content == null) {
			content = new Vector<Posting>();
			Posting newPosting = new Posting(pageID, freq);
			newPosting.wordPosition.add(new Integer(pos));
			content.add(newPosting);
			hashtable.put(wordID, content);
			word_exist = false;
		} else {
			word_exist = true;
			boolean exist = false;
			for(int i = 0; i < content.size(); i++){
				if(pageID == content.elementAt(i).pageID){
					exist = true;
					Posting newPosting = new Posting(pageID, freq);
					newPosting.wordPosition = content.elementAt(i).wordPosition;
					if(!newPosting.wordPosition.contains(new Integer(pos)))
						newPosting.wordPosition.add(new Integer(pos));
					content.set(i, newPosting);
					break;	//update the frequency of the word in the page
				}
			}
			if(exist == false){
				Posting newPosting = new Posting(pageID, freq);	//add new posting to the vector
				newPosting.wordPosition.add(new Integer(pos));
				content.add(newPosting);
			}
			hashtable.put(wordID, content);
		}
		return word_exist;
	}    

	public void delEntry(int wordID) throws IOException
	{   
		// Delete the word and its list from the hashtable
		// ADD YOUR CODES HERE
		hashtable.remove(wordID);
	} 

	public int getFrequency(int wordID, int pageID) throws IOException {
		Vector<Posting> content = (Vector<Posting>)hashtable.get(wordID);
		if(content == null) {
			//System.out.println("no frequency");
			return 0;	//0 represents that the posting does not exist
		}
		for(int i=0;i<content.size();i++) {
			if(content.elementAt(i).getPageID() == pageID) {
				//System.out.println("get frequency");
				return content.elementAt(i).getFrequency();
				
			}
		}
		
		//System.out.println("word exist, but no page");
		return 0;
	}
	

    public Vector<Posting> get(int wordID) throws IOException

    {
    	Vector<Posting> content = (Vector<Posting>)hashtable.get(wordID);
        return content;
    }
	

    public Vector<Integer> getPoslist(int wordID,int pageID) throws IOException
    {
    	Vector<Posting> content = (Vector<Posting>)hashtable.get(wordID);
    	if(content==null)
    		return null;
    	//Posting tmp = content.get(pageID);
    	//return tmp.getPosition();
    	for(int i=0;i<content.size();i++) {
    		if(pageID==content.elementAt(i).getPageID())
    			return content.elementAt(i).getPosition();
    	}
    	return null;
    }
    
    public int getDocumentFrequency(int wordID) throws IOException

    {
        Vector<Posting> content = (Vector<Posting>)hashtable.get(wordID);
        if (content == null)
            return -1;
        return content.size();
    }


    public boolean containsWordPos(int pageID, int wordID, int wordPos) throws IOException
    {
    	Vector<Posting> content = (Vector<Posting>)hashtable.get(wordID);

        if (content == null || (content.size()<= pageID))
            return false;

        Posting posting = content.get(pageID);
        return posting.containsWordPos(wordPos);

    }
	/* public void printAll() throws IOException
    {
        // Print all the data in the hashtable
        // ADD YOUR CODES HERE
        FastIterator iter = hashtable.keys();
        String key;
        while( (key=(String)iter.next()) != null ) {
            System.out.println(key + " = " + hashtable.get(key));
        }
    }    

    public static void main(String[] args)
    {
        try
        {
            InvertedIndex index = new InvertedIndex("lab1","ht1");

            index.addEntry("cat", 2, 6);
            index.addEntry("dog", 1, 33);
            System.out.println("First print");
            index.printAll();

            index.addEntry("cat", 8, 3);
            index.addEntry("dog", 6, 73);
            index.addEntry("dog", 8, 83);
            index.addEntry("dog", 10, 5);
            index.addEntry("cat", 11, 106);
            System.out.println("Second print");
            index.printAll();

            index.delEntry("dog");
            System.out.println("Third print");
            index.printAll();
            index.finalize();
        }
        catch(IOException ex)
        {
            System.err.println(ex.toString());
        }

    }*/
} //no use right now
