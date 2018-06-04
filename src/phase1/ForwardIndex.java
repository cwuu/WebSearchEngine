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

public class ForwardIndex
{
	private RecordManager recman;
	private HTree hashtable;
	//private HTree hashtable_mtf;
	/*ForwardIndex(String recordmanager, String objectname) throws IOException
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
    }*/

	ForwardIndex(RecordManager recordmanager, String objectname) throws IOException
	{
		this.recman = recordmanager;
		long recid = recman.getNamedObject(objectname);
		//long recid_mtf = recman.getNamedObject(objectname + "_mtf");   

		if(recid != 0)
		{
			hashtable = HTree.load(recman, recid);
			//hashtable_mtf = HTree.load(recman, recid_mtf);
		}
		else
		{
			hashtable = HTree.createInstance(recman);
			recman.setNamedObject( objectname, hashtable.getRecid() );
            //hashtable_mtf = HTree.createInstance(recman);
            //recman.setNamedObject( objectname + "_mtf", hashtable_mtf.getRecid() );
		}
	}

	public void finalize() throws IOException
	{
		recman.commit();
		recman.close();                
	} 

	public Vector<Integer> getWords(int pageID) throws IOException
	{
		Vector<Integer> content = (Vector<Integer>)hashtable.get(pageID);
		if(content == null)
			return null;
		else
			return content;
	}

	public Vector<Integer> getLinks(int pageID)throws IOException
	{
		Vector<Integer> content = (Vector<Integer>)hashtable.get(pageID);
		if(content == null)
			return null;
		else
			return content;
	}

	public boolean addEntry(int pageID, int wordID) throws IOException
	{
		boolean exist = false;
		Vector<Integer> content = (Vector<Integer>)hashtable.get(pageID);
		if(content == null){
			content = new Vector<Integer>();
			Integer newID = new Integer(wordID);
			content.add(newID);
			hashtable.put(pageID, content);
		} 
		else{		//check if the word is already in the index
			exist = false;
			for(int i = 0; i < content.size(); i++){
				if(wordID == content.elementAt(i).intValue()){
					exist = true;
					break;
				}
			}
			if(exist == false){
				Integer newID = new Integer(wordID);
				content.add(newID);
				hashtable.put(pageID, content);
			}
		}
		return exist;



		// ADD YOUR CODES HERE
		/*Vector<Posting> content = (Vecotr<Posting>)hashtable.get(wordID);
        if (content == null) {
            content = new Vector<Posting>();
	    Posting newPosting = Posting(pageID, freq);
	    content.add(newPosting);
            hashtable.put(wordID, content);
        } else {
            boolean exist = false;
	    for(int i = 0; i < content.size(); i++){
		if(pageID == content[i].pageID){
		    exist = true;
		    content[i] = new Posting(pageID, freq);
		    break;	//update the frequency of the word in the page
		}
	    }
	    if(exist == false){
	        Posting newPosting = Posting(pageID, freq);	//add new posting to the vector
		content.add(newPosting);
	    }
        }*/
	}    

	public void delEntry(int pageID) throws IOException
	{   
		// Delete the word and its list from the hashtable
		// ADD YOUR CODES HERE
		Vector<Integer> content = (Vector<Integer>)hashtable.get(pageID);
		if(content != null)
			hashtable.remove(pageID);
	} 
/*
    public void calculateMaxTermFrequency(int pageID) throws IOException
    {
        Vector<Integer> content = (Vector<Integer>)hashtable.get(pageID);

        if (content == null)
        {
            System.out.println("ERROR: calculateMaxTermFrequency");
            return;
        }

        Vector<Integer> list = (Vector<Integer>) hashtable.get(pageID);
        Set<Integer> unique = new HashSet<Integer>(list);   // elminate duplicate terms
        int maxtf = 0;
        for(int wordID : unique) 
        {
            int tf = Collections.frequency(list, wordID);
            if (tf > maxtf)
                maxtf = tf;
        }
        System.out.println("MAX Term Frequency = " + maxtf);
        hashtable_mtf.put(pageID, maxtf);  // put the maxtf
    }
    
    public int getMaxTermFrequency(int pageID) throws IOException

    {
        if(hashtable_mtf.get(pageID) == null)
            return 0;
        return (Integer) hashtable_mtf.get(pageID);

    }
    
    public int getPageSize(int pageID) throws IOException

    {
        Vector<Integer> list = (Vector<Integer>) hashtable.get(pageID);
        if(list == null)
        	 return 0;
        return list.size();
    }

	 public void printAll() throws IOException
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
//
