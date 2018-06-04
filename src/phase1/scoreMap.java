
package phase1;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.io.Console;
import java.util.Scanner;
import java.util.regex.Pattern;


	public class scoreMap
	{
		scoreMap(int pageID_input, double score_input)
		{
			pageID = pageID_input;
			score = score_input;
		}
		int pageID;
		double score;
		
		public double getScore()
		{
			return score;
		}
		public int getID()
		{
			return pageID;
		}
		public void set(int ID, double sc)
		{
			pageID=ID;
			score=sc;
		}
			
	};
//
