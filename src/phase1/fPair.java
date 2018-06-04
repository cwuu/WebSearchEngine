package phase1;



import java.io.Serializable;



public class fPair implements Serializable{//this class stores wordID-freq in a page

	public int wordID;

	public int freq;

	public fPair(int wordID, int freq) {this.wordID = wordID; this.freq = freq;}

	public int getID() {return wordID;}

	public int getfreq() {return freq;}
	
	public void set(int ID, int frequency)
	{
		wordID=ID;
		freq=frequency;
	}
	
	public void add(int ID, int frequency)
	{
		wordID+=ID;
		freq+=frequency;
	}
}
//
