package phase1;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;



public class SE
{

    static final int NUM_PAGES = 200;    
    static final double TITLE_BONUS_WEIGHT = 2.0;  
    static StopStem stopStem = new StopStem("stopwords.txt");

	/*union search done by vector space*/

    public static Vector<scoreMap> unionSearch(Vector<String> inputQuery) throws IOException
    {
    	System.out.println("unionSearch function");
    	  
        RecordManager recman = RecordManagerFactory.createRecordManager("fetch");
        Indexer indexer = new Indexer(recman);
 

		//the query will be userd for searching
        Vector<String> query = new Vector<String>();
        
		// process the input Query
        for (String w : inputQuery) 
        {
            if (stopStem.isStopWord(w)) 
            {
               // System.out.println("Stop word: " + w);
                continue;
            }
            String stem = stopStem.stem(w);
            query.add(stem);
        }
        
		//output the word after stopword removal and stemming
       // System.out.println(query);

        //query.addElement("computer");
       
        //integer:pageid, double:weight
        Map<Integer, Double> sumWeightMap = new HashMap<Integer, Double>();
        //integer:pageid, double:score
        Map<Integer, Double> scoreMap = new HashMap<Integer, Double>();


        for (String q : query) 
        {
            //System.out.println("Calculating " + q);
            
			int wordID = indexer.getWordID(q);

			//title search
            // if query term is found in title inverted index, add bonus weight
            if (indexer.getTitleInveredIndexPosting(wordID) != null) 
            {
            	
            	Vector<Posting> content = indexer.getTitleInveredIndexPosting(wordID); 
                for (int i=0;i<content.size();i++) 
                {
                	int pageID = content.get(i).getPageID();    
                    Double sumWeight = sumWeightMap.get(pageID);
                    //init sumweight if it isn't existed
                    if(sumWeight == null)
                    	sumWeight = 0.0;
                    sumWeight =  sumWeight + TITLE_BONUS_WEIGHT;
                    sumWeightMap.put(pageID, sumWeight); 
                    //System.out.printf("Title bonus: %s sum_weight: %f\n", q, sumWeight);
                }
            }
            
        
            //body search
            // if query term is found in body inverted index
            if (indexer.getBodyInveredIndexPosting(wordID) != null) 
            {
            	
                Vector<Posting> content = indexer.getBodyInveredIndexPosting(wordID);

                for (int i=0;i<content.size();i++) 
                {
                	
                	int pageID = content.get(i).getPageID();
                	
                	double tf = (double) content.get(i).getFrequency();
                
                	double max_tf = (double) indexer.getMaxTermFrequency(pageID);
                	//double max_tf = 1.0;
                	
                	
                	double df = (double) indexer.getDocumentFrequency(wordID);
                	
                	double idf = Math.log(NUM_PAGES / df) / Math.log(2);
                	
                	if(max_tf == 0.0) 
                        continue;
              
                	Double sumWeight = sumWeightMap.get(pageID);
                    //init sumweight if it isn't existed
                    if(sumWeight == null)
                    	sumWeight = 0.0;
                    sumWeight = sumWeight +  (double)(tf / max_tf) * idf;
                    sumWeightMap.put(pageID, sumWeight); 
                // System.out.printf("PAGE:%s tf:%s max_tf:%s df:%s idf:%s weight:%s sumWeight:%f\n", pageID, tf, max_tf, df, idf, (double)(tf / max_tf) * idf, sumWeight);
                }
            }
        }
        
      
        
        //after getting the weight of each matched page, compute the score
        for (Map.Entry<Integer, Double> entry : sumWeightMap.entrySet()) 
        {
            int pageID = entry.getKey();
            double sumWeight = entry.getValue();
            int numWord = indexer.getPageSize(pageID);
            //sum of weight = dot product
            double score = sumWeight / (Math.sqrt(numWord)) * Math.sqrt(query.size());
//          System.out.printf("Page:%d socre:%f\n", pageID, score);
            scoreMap.put(pageID, score);    // put inside a score map
        }


        Map<Integer, Double> sortedScoreMap;
        List<Map.Entry<Integer, Double>> list = new LinkedList<Map.Entry<Integer, Double>>(scoreMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>()
        {
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) 
            {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        // sort in descending order
        Collections.reverse(list); 
        Map<Integer, Double> result = new LinkedHashMap<Integer, Double>();
        for (Map.Entry<Integer, Double> mapping:list) 
        {
        	result.put(mapping.getKey(), mapping.getValue());
        }
        sortedScoreMap = result;
   
        Vector<scoreMap> page2score = new Vector<scoreMap>();
        
        int index=0;
        for(int i=0;i<NUM_PAGES;i++)
        {
        	if(sortedScoreMap.get(i)==null)
        		continue;       		
        	scoreMap element = new scoreMap(i,sortedScoreMap.get(i));
        	page2score.add(index, element);
        	//System.out.println("pageID: "+ page2score.get(index).getID() + " Score: "+ page2score.get(index).getScore());
        	index++;
        }
       
        //System.out.println("=======================================================");
        
        //insertion sort
        scoreMap temp;
        int i,j;
      //  System.out.println(page2score.size());
        for(i=1;i<page2score.size();i++)
        {
        	temp = new scoreMap(page2score.get(i).getID(),page2score.get(i).getScore());
            for(j=i;j>0 && temp.getScore() >page2score.get(j-1).getScore();j--)
            {
            	page2score.get(j).set(page2score.get(j-1).getID(),page2score.get(j-1).getScore());
            }
            page2score.get(j).set(temp.getID(),temp.getScore());
        }
        
        
        /*for(int k=0;k<page2score.size();k++)
        {
        	System.out.println("pageID: "+ page2score.get(k).getID() + " Score: "+ page2score.get(k).getScore());
        }
        */
        
        return page2score;
    	
    }

    /*phrase search done by vector space*/

    public static Vector<scoreMap> phaseSearch(Vector<String> inputQuery) throws IOException
    {
    	System.out.println("phaseSearch function");
        RecordManager recman = RecordManagerFactory.createRecordManager("fetch");
        Indexer indexer = new Indexer(recman);
       
	
    	Vector<String> sequence = new Vector<String>();
    	Vector<scoreMap> page2score = new Vector<scoreMap>();
    	
    	 for(int i=0;i<inputQuery.size();i++)
    	 {
    		 sequence.add(i,inputQuery.get(i));
    	 }
    
    	if(sequence==null)
    		return null;
    	
    	for(int g=0;g<sequence.size()-1;g++)
    	{
   		
    		inputQuery.clear();
    		inputQuery.add(0,sequence.get(g));
    		inputQuery.add(1,sequence.get(g+1));
    		

        Vector<String> query = new Vector<String>();
        
        // process the input Query
        for (String w : inputQuery) 
        {
            if (stopStem.isStopWord(w)) 
            {
                //System.out.println("Stop word: " + w);
                continue;
            }
            String stem = stopStem.stem(w);
            query.add(stem);
        }
        //System.out.println(query);
        
        // for the first search term
        int wordID = indexer.getWordID(query.get(0));
        //no phrase found
        if (indexer.getBodyInveredIndexPosting(wordID) == null)  
        		return null; 
        
        // get intersect page maps
        Vector<Posting> firstPost = indexer.getBodyInveredIndexPosting(wordID);
        Vector<Posting> intersectPost = new Vector<Posting>();
//        System.out.println(query.get(0) + "(id:"+ wordID + "): " + firstMap);

        //find intersect page
        for (String q : query) 
        {
            if (q == query.get(0)) 
            	continue;   
            wordID = indexer.getWordID(q);
            //no phrase found
            if (indexer.getBodyInveredIndexPosting(wordID) == null)  
            		return null; 
            Vector<Posting> tmpPost = indexer.getBodyInveredIndexPosting(wordID);
//            System.out.println(q + "(id:"+ wordID + ": " + tmpMap);
/*DEBUG ONLY
            System.out.print("firstPost: ");  
            for(int i=0;i<firstPost.size();i++)
            	 System.out.print(firstPost.get(i).getPageID()+" ");  
            System.out.println(" ");  
            System.out.print("tmpPost: ");  
            for(int i=0;i<tmpPost.size();i++)
            	 System.out.print(tmpPost.get(i).getPageID()+" ");  
            System.out.println(" ");  
*/
        int index=0;
        for(int i=0;i<firstPost.size();i++)
        {
        	boolean flag = false;
        	for(int j=0;j<tmpPost.size();j++)
        	{
        		if(firstPost.get(i).getPageID()==tmpPost.get(j).getPageID())
        		{
        			flag=true;
        			continue;
        		}
        	}
        	if(flag==true)
        	{
            	Posting element = new Posting(firstPost.get(i).getPageID(),firstPost.get(i).getFrequency());
           	 	intersectPost.add(index, element);  
           	 	index++;
        	}
        		

        }
            	         
     }

      //  System.out.print("intersectPost: ");  
    //    for(int i=0;i<intersectPost.size();i++)
    //    	 System.out.print(intersectPost.get(i).getPageID()+" ");  
     //  System.out.println(" ");  


        // for each pageID, wordID = id of first query
        int term1ID = indexer.getWordID(query.get(0));
        Set<Integer> pagesContainExactPhase = new HashSet<>();
        Map<Integer, Double> tfOverMaxtf = new HashMap<>();
        
        //for each page
        for (int i=0;i<intersectPost.size();++i) 
        {
            int pageID = intersectPost.get(i).getPageID();
            double tf = 0;
      //      System.out.println(" term1  " + query.get(0) + "  wordID: "+term1ID+ "  pageID: "+pageID );
            Vector<Integer> posList = indexer.getPoslist(term1ID,pageID);
      //      System.out.println(" posList:" + posList );
            if(posList==null)
            	continue;
            for(int j=0;j<posList.size();j++)
            {
            	int pos = posList.get(j);
            	boolean found = true;   // all the term form continue sequence
            	int postIncrement=1;
            	 for (String q : query) 
                 {    
            		
                 	// for each of the query terms
                     if (q == query.get(0)) 
                     	continue;    // skip the first
                     wordID = indexer.getWordID(q);
       //              System.out.println(" term2  " + q + "  wordID: "+wordID+ "  pageID: "+pageID );
                     //debug
       //              Vector<Integer> List = indexer.getPoslist(wordID,pageID);
        //             System.out.println(" List:" + List );
                     boolean contain = indexer.containsWordPos(pageID, wordID, pos + postIncrement);
                     postIncrement++;
                     if (!contain)
                     {
                         found = false;
                         continue;
                     }
         
                 }
                 if (found) 
                 {
                     // match terms found
         //            System.out.println(query + "  Found !!!!!!!!!! PageID:" + pageID);
                     pagesContainExactPhase.add(pageID);
                     ++tf;
                 }          	
            }
            
            // calculate tf/max(tf) for each page
            if( tf > 0)
            {
                double max_tf = (double) indexer.getMaxTermFrequency(pageID);
            	//double max_tf=1.0;
                if(max_tf <= 0.0) 
                		continue;
         //       System.out.printf("page:%d tf:%f maxtf: %f\n", pageID, tf, max_tf);
                tfOverMaxtf.put(pageID, tf/max_tf); // store in the map
            }
        }
        // calculate idf
        double df = pagesContainExactPhase.size();
        double idf = Math.log(NUM_PAGES / df) / Math.log(2);

    //   System.out.printf("df:%f; idf:%f\n" , df, idf);
    //    System.out.println(pagesContainExactPhase);
     //   System.out.println(tfOverMaxtf);

        // calculating (tf/tfmax)*idf for each matched term
        Map<Integer, Double> weightMap = new HashMap<>();
        for (Map.Entry<Integer, Double> entry : tfOverMaxtf.entrySet()) 
        {
            int pageID = entry.getKey();
            double tfOverMaxrf = entry.getValue();
            double weight = tfOverMaxrf * idf;
            weightMap.put(pageID, weight);
//            System.out.printf("page:%d weight:%f\n" , pageID, weight);
        }

        // sort the weight
        Map<Integer, Double> sortedWeightMap = sortByValue(weightMap);


        int index=0;
        for(int i=0;i<NUM_PAGES;i++)
        {
        	if(sortedWeightMap.get(i)==null)
        		continue;       		
        	scoreMap element = new scoreMap(i,sortedWeightMap.get(i));
        	page2score.add(index, element);
        //	System.out.println("pageID: "+ page2score.get(index).getID() + " Score: "+ page2score.get(index).getScore());
        	index++;
        }
       
        //System.out.println("=======================================================");
       
    	}
    	  	 
        //insertion sort
        scoreMap temp;
        int i,j;

        for(i=1;i<page2score.size();i++)
        {
        	temp = new scoreMap(page2score.get(i).getID(),page2score.get(i).getScore());
            for(j=i;j>0 && temp.getScore() >page2score.get(j-1).getScore();j--)
            {
            	page2score.get(j).set(page2score.get(j-1).getID(),page2score.get(j-1).getScore());
            }
            page2score.get(j).set(temp.getID(),temp.getScore());
        }
        
        
       /* for(int k=0;k<page2score.size();k++)
        {
        	System.out.println("pageID: "+ page2score.get(k).getID() + " Score: "+ page2score.get(k).getScore());
        }
        */
     //   System.out.println("reach search function end");
        return page2score;
        
        
    }

    public static Vector<scoreMap> search(Vector<String> inputQuery) throws IOException
    {
    	System.out.println("search function");
    	Vector<scoreMap> unionSearch = new Vector<scoreMap>();
    	Vector<scoreMap> pharseSearch = new Vector<scoreMap>();
    	try{
    	unionSearch=SE.unionSearch(inputQuery);
    	pharseSearch=SE.phaseSearch(inputQuery);
    	}
    	catch(Exception e){e.printStackTrace ();}

    	if(unionSearch== null)
    		return null;
    	
    	
    	if(pharseSearch==null)
    	{
    		return unionSearch;
    	}
    	System.out.println("union size: "+ unionSearch.size());
    	System.out.println("phrase size: "+ pharseSearch.size());
    	for(int i=0;i<unionSearch.size();++i)
    	{
    		for(int j=0;j<pharseSearch.size();j++)
    		{
    			if(pharseSearch.get(j).getID() == unionSearch.get(i).getID())
    			{
    				unionSearch.get(i).set(unionSearch.get(i).getID(),10*unionSearch.get(i).getScore() + 15*pharseSearch.get(j).getScore());
    				break;
    			}
    				
    		}
    	}
    	
    	//insertion sort
        scoreMap temp;
        int i,j;
        for(i=1;i<unionSearch.size();i++)
        {
        	temp = new scoreMap(unionSearch.get(i).getID(),unionSearch.get(i).getScore());
            for(j=i;j>0 && temp.getScore() >unionSearch.get(j-1).getScore();j--)
            {
            	unionSearch.get(j).set(unionSearch.get(j-1).getID(),unionSearch.get(j-1).getScore());
            }
            unionSearch.get(j).set(temp.getID(),temp.getScore());
        }
         for(int k=0;k<unionSearch.size();k++)
        {
        	System.out.println("pageID: "+ unionSearch.get(k).getID() + " Score: "+ unionSearch.get(k).getScore());
        }
        
        return unionSearch;
    	
    }
    
    public static Vector<scoreMap> getSimilarpage(Vector<String> inputQuery) throws IOException
    {
    	System.out.println("getSimilarpage function");
        RecordManager recman = RecordManagerFactory.createRecordManager("fetch");
        Indexer indexer = new Indexer(recman);
    	Vector<scoreMap> similarPage = new Vector<scoreMap>();
    	Vector<scoreMap> searchedPage = new Vector<scoreMap>();
    	Vector<String> words = new Vector<String>();
    	Vector<fPair> Topfive = new Vector<fPair>();
    	try
    	{
    		searchedPage = SE.search(inputQuery);
    	}
    	catch(Exception e)
    	{e.printStackTrace ();}
    	
    	if(searchedPage.isEmpty())
    		return null;
    	
    	Topfive=indexer.getTopTenWord(searchedPage.get(0).getID());
        
        int count =0;
    	for(int m=0;m<5+count;m++)
    	{
    		if(indexer.getWord(Topfive.get(m).getID())==null)
    		{
    			count++;
    			continue;
    		}
    		String w = indexer.getWord(Topfive.get(m).getID());
    		System.out.println("iteration: "+ (m-count) +" term: "+ w); 
    		words.add(m-count,w);
    	}
    	try
    	{
    		similarPage = SE.search(words);
    	}
    	catch(Exception e)
    	{e.printStackTrace ();}
    	System.out.println("size"+similarPage.size()); 
    	return similarPage;
    }
    
    public static Vector<scoreMap> getTopicpage(Vector<String> inputQuery) throws IOException
    {
        RecordManager recman = RecordManagerFactory.createRecordManager("fetch");
        Indexer indexer = new Indexer(recman);
        
    	Vector<scoreMap> similarPage = new Vector<scoreMap>();
    	Vector<scoreMap> searchedPage = new Vector<scoreMap>();
    	Vector<String> words = new Vector<String>();
    	Vector<fPair> Topfive = new Vector<fPair>();
    	//from 5 top relevant searced pages
    	Vector<fPair> TopTen1 = new Vector<fPair>();
    	Vector<fPair> TopTen2 = new Vector<fPair>();
    	Vector<fPair> TopTen3 = new Vector<fPair>();
    	Vector<fPair> TopTen4 = new Vector<fPair>();
    	Vector<fPair> TopTen5 = new Vector<fPair>();
    	
    	try
    	{
    		searchedPage = SE.search(inputQuery);
    	}
    	catch(Exception e)
    	{e.printStackTrace ();}
    	
    	//the 5 most frequent words are extracted from the 5 most relevant page searched according to the input query
    	//stupid implement....
    		//iteration: 1 term: postgradu
    	//iteration: 2 term: undergradu
    	//iteration: 3 term: about
    	//iteration: 4 term: site
    	if(searchedPage.size()==0)
    	{
    		return null;
    	}
    	else if (searchedPage.size() ==1)
    	{
    		TopTen1 =  indexer.getTopTenWord(searchedPage.get(0).getID());
    	}
    	else if (searchedPage.size() ==2)
    	{
    		TopTen1 =  indexer.getTopTenWord(searchedPage.get(0).getID());
    		TopTen2 =  indexer.getTopTenWord(searchedPage.get(1).getID());
    	}
    	else if (searchedPage.size() ==3)
    	{
    		TopTen1 =  indexer.getTopTenWord(searchedPage.get(0).getID());
    		TopTen2 =  indexer.getTopTenWord(searchedPage.get(1).getID());
    		TopTen3 =  indexer.getTopTenWord(searchedPage.get(2).getID());
    	}
    	else if (searchedPage.size() ==4)
    	{
    		TopTen1 =  indexer.getTopTenWord(searchedPage.get(0).getID());
    		TopTen2 =  indexer.getTopTenWord(searchedPage.get(1).getID());
    		TopTen3 =  indexer.getTopTenWord(searchedPage.get(2).getID());
    		TopTen4 =  indexer.getTopTenWord(searchedPage.get(3).getID());
    	}
    	else if(searchedPage.size()>=5)
    	{
    		TopTen1 =  indexer.getTopTenWord(searchedPage.get(0).getID());
    		TopTen2 =  indexer.getTopTenWord(searchedPage.get(1).getID());
    		TopTen3 =  indexer.getTopTenWord(searchedPage.get(2).getID());
    		TopTen4 =  indexer.getTopTenWord(searchedPage.get(3).getID());
    		TopTen5 =  indexer.getTopTenWord(searchedPage.get(4).getID());
    	}
    	
    	for(int i=0;i<TopTen1.size();i++)
    	{
    		boolean flag = false;
    		for(int j=0;j<Topfive.size();j++)
    		{
        		if(TopTen1.get(i).getID()==Topfive.get(j).getID())
        		{
        			fPair element = new fPair(TopTen1.get(i).getID(),TopTen1.get(i).getfreq()*2+Topfive.get(j).getfreq());
        			Topfive.add(j,element);
        			flag = true;
        			break;
        		}
        			
    		}
    		if(flag == true)
    			continue;
    		else
    		{
    			fPair element = new fPair(TopTen1.get(i).getID(),TopTen1.get(i).getfreq()*2);
    			Topfive.add(i,element);

    		}
    	}
    	for(int i=0;i<TopTen2.size();i++)
    	{
    		boolean flag = false;
    		for(int j=0;j<Topfive.size();j++)
    		{
        		if(TopTen2.get(i).getID()==Topfive.get(j).getID())
        		{
        			fPair element = new fPair(TopTen2.get(i).getID(),TopTen2.get(i).getfreq()+Topfive.get(j).getfreq());
        			Topfive.add(j,element);
        			flag = true;
        			break;
        		}
        			
    		}
    		if(flag == true)
    			continue;
    		else
    		{
    			fPair element = new fPair(TopTen2.get(i).getID(),TopTen2.get(i).getfreq());
    			Topfive.add(i,element);

    		}
    	}
    	for(int i=0;i<TopTen3.size();i++)
    	{
    		boolean flag = false;
    		for(int j=0;j<Topfive.size();j++)
    		{
        		if(TopTen3.get(i).getID()==Topfive.get(j).getID())
        		{
        			fPair element = new fPair(TopTen3.get(i).getID(),TopTen3.get(i).getfreq()+Topfive.get(j).getfreq());
        			Topfive.add(j,element);
        			flag = true;
        			break;
        		}
        			
    		}
    		if(flag == true)
    			continue;
    		else
    		{
    			fPair element = new fPair(TopTen3.get(i).getID(),TopTen3.get(i).getfreq());
    			Topfive.add(i,element);

    		}
    	}
    	for(int i=0;i<TopTen4.size();i++)
    	{
    		boolean flag = false;
    		for(int j=0;j<Topfive.size();j++)
    		{
        		if(TopTen4.get(i).getID()==Topfive.get(j).getID())
        		{
        			fPair element = new fPair(TopTen4.get(i).getID(),TopTen4.get(i).getfreq()+Topfive.get(j).getfreq());
        			Topfive.add(j,element);
        			flag = true;
        			break;
        		}
        			
    		}
    		if(flag == true)
    			continue;
    		else
    		{
    			fPair element = new fPair(TopTen4.get(i).getID(),TopTen4.get(i).getfreq());
    			Topfive.add(i,element);

    		}
    	}
    	for(int i=0;i<TopTen5.size();i++)
    	{
    		boolean flag = false;
    		for(int j=0;j<Topfive.size();j++)
    		{
        		if(TopTen5.get(i).getID()==Topfive.get(j).getID())
        		{
        			fPair element = new fPair(TopTen5.get(i).getID(),TopTen5.get(i).getfreq()+Topfive.get(j).getfreq());
        			Topfive.add(j,element);
        			flag = true;
        			break;
        		}
        			
    		}
    		if(flag == true)
    			continue;
    		else
    		{
    			fPair element = new fPair(TopTen5.get(i).getID(),TopTen5.get(i).getfreq());
    			Topfive.add(i,element);

    		}
    	}
    	
    	//insertion sort
        fPair temp;
        int i,j;
        for(i=1;i<Topfive.size();i++)
        {
        	temp = new fPair(Topfive.get(i).getID(),Topfive.get(i).getfreq());
            for(j=i;j>0 && temp.getfreq() >Topfive.get(j-1).getfreq();j--)
            {
            	Topfive.get(j).set(Topfive.get(j-1).getID(),Topfive.get(j-1).getfreq());
            }
            Topfive.get(j).set(temp.getID(),temp.getfreq());
        }
        
        for(int k=0;k<Topfive.size();k++)
        {
        	System.out.println("wordID: "+ Topfive.get(k).getID() + " Score: "+ Topfive.get(k).getfreq());
        }
        Vector<fPair> compact_Topfive = new Vector<fPair>();
        for(int w=0;w<Topfive.size();w++)
        {
        	fPair element = new fPair(Topfive.get(w).getID(),Topfive.get(w).getfreq());
        	compact_Topfive.add(w,element);
        	for(int y=w;y<Topfive.size();y++)
        	{
        		if(Topfive.get(w).getID()==Topfive.get(y).getID())
        		{
        			compact_Topfive.get(w).add(Topfive.get(y).getID(), Topfive.get(y).getfreq());
        		}    		
        	}
        }
        
        System.out.println("compact_topfive: "+ compact_Topfive); 
        int count =0;
    	for(int m=0;m<5+count;m++)
    	{
    		if(indexer.getWord(compact_Topfive.get(m).getID())==null)
    		{
    			count++;
    			continue;
    		}
    		String w = indexer.getWord(compact_Topfive.get(m).getID());
    		System.out.println("iteration: "+ m +" term: "+ w); 
    		words.add(m-count,w);
    	}

    	System.out.println("words: "+ words); 
        
    	try
    	{
    		similarPage = SE.search(words);
    	}
    	catch(Exception e)
    	{e.printStackTrace ();}
    	 	
    	return similarPage;
    }

    // sort in descending order
    public static Map<Integer, Double> sortByValue(Map<Integer, Double> map) 
    {
        List<Map.Entry<Integer, Double>> list = new LinkedList<Map.Entry<Integer, Double>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>()
        {
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) 
            {
            		return o1.getValue().compareTo(o2.getValue());
            }
        });
        
        Collections.reverse(list); // sort in descending order

        Map<Integer, Double> result = new LinkedHashMap<Integer, Double>();
        
        for(Map.Entry<Integer, Double> mapping:list)
        { 
        		result.put(mapping.getKey(), mapping.getValue());
            //System.out.println(mapping.getKey()+":"+mapping.getValue()); 
        } 

        return result;
    }

    // intersect two map, if two with same key, sum their values, and sorted
    public static Map<Integer, Double> intersectSum(Map<Integer, Double> map1, Map<Integer, Double> map2) 
    {
        Set<Integer> intersectKeySet = new HashSet<Integer>(map1.keySet());
        Map<Integer, Double> resultMap = new HashMap<Integer, Double>();
        intersectKeySet.retainAll(map2.keySet());
        // loop the intersection key
        for(int key : intersectKeySet) {
            double sumScore = map1.get(key) + map2.get(key);
            resultMap.put(key, sumScore);
        }
        return sortByValue(resultMap);
    }
    
    public static void main(String[] args)
    {
    	Scanner sc = new Scanner(System.in);
    	System.out.println("enter the query:");
    	String s = sc.next();
    	String[] words = s.split(Pattern.quote(","));
    	Vector<String> input = new Vector<String>();
    	for(int i=0;i<words.length;i++)
    	{
    		input.add(words[i]);
    		System.out.println(words[i]);
    	}
    	
    	try{
    		//SE.search(input);
    		SE.getSimilarpage(input);
    	}
    	catch(Exception e){e.printStackTrace ();}
		System.out.println("query finished");
		sc.close();
    	
	}


}
