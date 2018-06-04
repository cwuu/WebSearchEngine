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
import java.io.Serializable;

public class MappingTable
{
	private RecordManager recman;
	private HTree table;
	private HTree inverse_table;

	/*InvertedIndex(String recordmanager, String objectName) throws IOException
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

	MappingTable(RecordManager recordmanager, String objectname, String inverse_objectname) throws IOException
	{
		this.recman = recordmanager;
		long recid = recman.getNamedObject(objectname);
		long inverse_recid = recman.getNamedObject(inverse_objectname);

		if(recid != 0)
			table = HTree.load(recman, recid);
		else
		{
			table = HTree.createInstance(recman);
			recman.setNamedObject( objectname, table.getRecid() );
		}

		if(inverse_recid != 0)
			inverse_table = HTree.load(recman, inverse_recid);
		else
		{
			inverse_table = HTree.createInstance(recman);
			recman.setNamedObject( inverse_objectname, inverse_table.getRecid() );
		}
	}

	public void finalize() throws IOException
	{
		recman.commit();
		recman.close();                
	} 

	public boolean addEntry(String value, int id) throws IOException	//check modified date before call this function when using pageid
	{
		Integer tmpID = (Integer)table.get(value);
		boolean exist = true;

		if(tmpID == null)
		{
			exist = false;
			tmpID = new Integer(id);
			table.put(value, tmpID);
			inverse_table.put(tmpID, value);	
		}
		return exist;
	}    

	public void delEntry(String value) throws IOException
	{   
		Integer tmpID = (Integer)table.get(value);
		if(tmpID == null)
			return;
		table.remove(value);
		inverse_table.remove(tmpID.intValue());
		// Delete the word and its list from the hashtable
		// ADD YOUR CODES HERE
		//hashtable.remove(wordID);
	} 

	public int getID(String value) throws IOException
	{
		Integer tmpID = (Integer)table.get(value);
		if(tmpID == null)
			return -1;	//-1 represent nothing
		else{
			int id = tmpID.intValue();
			return id;
		}
	}


	public String getWord(int id) throws IOException
	{
		Integer tmpID = new Integer(id);
		String tmpValue = (String)inverse_table.get(tmpID);
		if(tmpValue == null)
			return null;
		else{
			return tmpValue;
		}
	}

	public String getURL(int id) throws IOException
	{
		Integer tmpID = new Integer(id);
		String tmpURL = (String)inverse_table.get(tmpID);
		if(tmpURL == null)
			return null;
		else
			return tmpURL;
	}

	public int getSize() throws IOException{

		int num=0;
		while(inverse_table.get(num)!=null) {
			num++;
		}
		return num;
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
}
//
