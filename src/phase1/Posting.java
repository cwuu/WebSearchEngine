package phase1;


import java.io.Serializable;
import java.util.Vector;

public class Posting implements Serializable
{
    public int  pageID;
    public int freq;
    public Vector<Integer> wordPosition;
    
    Posting(int pageID, int freq)
    {
        this.pageID = pageID;
        this. freq = freq;
        wordPosition = new Vector<Integer>();
    }
    
    Vector<Integer> getPosition(){return wordPosition;}
    int getPageID() {return pageID;}
    int getFrequency() {return freq;}
    

   boolean containsWordPos(int wordPos) 
   {
        return wordPosition.contains(wordPos);
   }
}
//
