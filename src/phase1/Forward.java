package phase1;



import jdbm.RecordManager;

import jdbm.RecordManagerFactory;

import jdbm.htree.HTree;

import jdbm.helper.FastIterator;

import java.util.Vector;

import java.io.IOException;

import java.io.Serializable;





public class Forward {

	

	private RecordManager recman;

	private HTree hashtable;

	

	Forward(RecordManager recordmanager, String objectname) throws IOException

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

	

	public Vector<Integer> getWords(int pageID) throws IOException

	{

		Vector<fPair> content = (Vector<fPair>)hashtable.get(pageID);

		if(content == null)

			return null;

		else {

			Vector<Integer> r = new Vector<Integer>();//transform Vector<fPair>to Vector<Integer>.
			for(int i=0;i<content.size();i++) {
				
				int num = content.elementAt(i).getID();
				Integer in = new Integer(num);
				
				r.add(in);

			}

			return r;

		}

	}

	

	public boolean addEntry(int pageID, int wordID, int freq) throws IOException{

		boolean word_exist = false;
		Vector<fPair> content = (Vector<fPair>)hashtable.get(pageID);
		if (content == null) {	//page doesn't exist in the inverted file
			content = new Vector<fPair>();
			fPair newPair = new fPair(wordID, freq);
			content.add(newPair);
			hashtable.put(pageID, content);
			word_exist = false;
		}
		else {	//page existing in the inverted file
			word_exist = true;
			boolean exist = false;
			for(int i = 0; i < content.size(); i++){
				if(wordID == content.elementAt(i).wordID){	//word existing in table, update
					exist = true;
					fPair newPair = new fPair(wordID, freq);
					content.set(i, newPair);
					break;	//update the frequency and positions of the word in the page
				}
			}
			if(exist == false){	//word not exist in table, new one
				fPair newPair = new fPair(wordID, freq);	//add new posting to the vector
				content.add(newPair);
			}
			hashtable.put(pageID, content);
		}
		return word_exist;
	}

	

	public void delEntry(int pageID) throws IOException

	{   

		// Delete the word and its list from the hashtable

		// ADD YOUR CODES HERE

		Vector<fPair> content = (Vector<fPair>)hashtable.get(pageID);

		if(content != null)

			hashtable.remove(pageID);

	} 

	

	public int getFrequency(int pageID, int wordID) throws IOException{

		Vector<fPair> content = (Vector<fPair>)hashtable.get(pageID);

		if(content == null)

			return 0;

		for(int i=0;i<content.size();i++) {

			if(content.elementAt(i).getID() == wordID)

				return content.elementAt(i).getfreq();

		}

		return 0;

	}

	

	public int getPageSize(int pageID) throws IOException {//get the total number of words in a page

		 Vector<fPair> list = (Vector<fPair>) hashtable.get(pageID);

	        if(list == null)

	        	 return 0;

	        return list.size();

	}

	

	public int getMaxTermFrequency(int pageID) throws IOException{

		Vector<fPair> content = (Vector<fPair>) hashtable.get(pageID);

		if(content == null)

			return 0;

		int max = 0;

		for(int i=0;i<content.size();i++) {

			if(content.elementAt(i).getfreq()>max)

				max = content.elementAt(i).getfreq();

		}

		return max;

	}

	public Vector<fPair> getTopFiveID(int pageID) throws IOException
	{
		Vector<fPair> content = (Vector<fPair>) hashtable.get(pageID);
		if(content == null)
			return null;
		Vector<Integer> top = new Vector<Integer>();
		Vector<Integer> m = new Vector<Integer>();
		int curMax = 0;
		int curID = 0;
		int topNum = (content.size()<5? content.size():5);
		for(int num=0;num<topNum;num++) {
			for(int i=0;i<content.size();i++) {
				if(top.contains(new Integer(content.elementAt(i).getID())))	//skip the elements already in top
					continue;
				int curFreq = content.elementAt(i).getfreq();
				if((curFreq>curMax&&num==0)||(num>0&&curFreq>curMax&&curFreq<=m.elementAt(num-1).intValue())) {
					curMax = content.elementAt(i).getfreq();
					curID = content.elementAt(i).getID();
				}
			}
			top.add(new Integer(curID));
			m.add(new Integer(curMax));
			curMax = 0;
		}
		content = new Vector<fPair>();
		for(int i=0;i<topNum;i++) {
			content.add(new fPair(top.elementAt(i).intValue(),m.elementAt(i).intValue()));
		}
		return content;
	}
	
	public Vector<fPair> getTopTenID(int pageID) throws IOException
	{
		Vector<fPair> content = (Vector<fPair>) hashtable.get(pageID);
		if(content == null)
			return null;
		Vector<Integer> top = new Vector<Integer>();
		Vector<Integer> m = new Vector<Integer>();
		int curMax = 0;
		int curID = 0;
		int topNum = (content.size()<10? content.size():10);
		for(int num=0;num<topNum;num++) {
			for(int i=0;i<content.size();i++) {
				if(top.contains(new Integer(content.elementAt(i).getID())))	//skip the elements already in top
					continue;
				int curFreq = content.elementAt(i).getfreq();
				if((curFreq>curMax&&num==0)||(num>0&&curFreq>curMax&&curFreq<=m.elementAt(num-1).intValue())) {
					curMax = content.elementAt(i).getfreq();
					curID = content.elementAt(i).getID();
				}
			}
			top.add(new Integer(curID));
			m.add(new Integer(curMax));
			curMax = 0;
		}
		content = new Vector<fPair>();
		for(int i=0;i<topNum;i++) {
			content.add(new fPair(top.elementAt(i).intValue(),m.elementAt(i).intValue()));
		}
		return content;
	}
}