/* --
COMP4321 Lab2 Exercise
Student Name:
Student ID:
Section:
Email:
 */
package phase1;
import java.util.Vector;
import org.htmlparser.beans.StringBean;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import java.io.IOException;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

import java.util.StringTokenizer;
import org.htmlparser.beans.LinkBean;
import java.net.URL;
import phase1.Indexer;
import java.util.Queue;
import java.util.LinkedList;
import java.sql.Date;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

class Crawler
{
	private static String url;
	public static RecordManager recman;
	public static Indexer indexer;

	Crawler(String _url)
	{
		url = _url;
		try{
		recman = RecordManagerFactory.createRecordManager("fetch");
		indexer = new Indexer(recman);
		}
		catch(Exception e){}
	}
	Crawler(){
		url = null;
		try{
			recman = RecordManagerFactory.createRecordManager("fetch");
			indexer = new Indexer(recman);
			}
			catch(Exception e){}
	}
	public void setURL(String _url) {
		url = _url;
	}
	public Vector<String> extractWords() throws ParserException

	{
		// extract words in url and return them
		// use StringTokenizer to tokenize the result from StringBean
		// ADD YOUR CODES HERE
		Vector<String> result = new Vector<String>();
		StringBean bean = new StringBean();
		bean.setURL(url);
		bean.setLinks(false);
		String contents = bean.getStrings();
		StringTokenizer st = new StringTokenizer(contents);
		while (st.hasMoreTokens()) {
			result.add(st.nextToken());
		}
		return result;
	}
	public static Vector<String> extractTitle(String _url) throws ParserException
	{
		Vector<String> result = new Vector<String>();
		Parser parser = new Parser(_url);
		parser.setEncoding("UTF-8");
		NodeList list = new NodeList();
		NodeFilter filter = new TagNameFilter("title");
		for(NodeIterator e = parser.elements();e.hasMoreNodes();) {
			e.nextNode().collectInto(list,filter);
		}
		for(int i = 0; i < list.size(); i++) {
			Node e = list.elementAt(i);
			if(e instanceof TitleTag) {
				String str = ((TitleTag)e).getTitle();
				StringTokenizer st = new StringTokenizer(str);
				while (st.hasMoreTokens()) {
					String a = st.nextToken();
					if(a.matches("^[A-Za-z0-9]+")&&a!=null)
						result.add(a);
				}
			}
		}
		return result;
	}

	public Vector<String> extractBody() throws ParserException
	{
		Vector<String> result = new Vector<String>();
		Parser parser = new Parser(url);
		parser.setEncoding("UTF-8");
		NodeList list = new NodeList();
		NodeFilter filter = new TagNameFilter("body");
		for(NodeIterator e = parser.elements();e.hasMoreNodes();) {
			e.nextNode().collectInto(list,filter);
		}
		for(int i = 0; i < list.size(); i++) {
			Node e = list.elementAt(i);
			if(e instanceof BodyTag) {
				String str = ((BodyTag)e).getBody();
				StringTokenizer st = new StringTokenizer(str);
				while (st.hasMoreTokens()) {
					String a = st.nextToken();
					if(a.matches("^[A-Za-z0-9]+")&&a!=null)
						result.add(a);
				}
			}
		}
		return result;
	}
	public Vector<String> extractLinks() throws ParserException

	{
		// extract links in url and return them
		// ADD YOUR CODES HERE
		Vector<String> result = new Vector<String>();
		LinkBean bean = new LinkBean();
		bean.setURL(url);
		URL[] urls = bean.getLinks();
		for (URL s : urls) {
			result.add(s.toString());
		}
		return result;
	}
	public static Date getLastModifiedDate(String _url) throws IOException {
		URL u = new URL(_url);
		URLConnection connection = u.openConnection();
		Date date = new Date(connection.getLastModified());

		//if(date.toString().equals("1970-01-01"))
		//date.setTime(connection.getDate());

		return date;
	}
	public static int getPageSize(String _url) throws IOException {
		URL u = new URL(_url);
		URLConnection connection = u.openConnection();
		BufferedReader b = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		String input = "";
		String temp = "";
		while((input = b.readLine())!=null) 
			temp += input;


		b.close();
		return temp.length();
	}
	public static void fetch()
	{
		try
		{
			Crawler crawler = new Crawler();
			
			int PageMax = 200;
			int wordID = indexer.getWordIDSize();
			int pageID = indexer.getPageIDSize();
			Queue<String> queue = new LinkedList<String>();
			String initial_url = "http://www.cse.ust.hk/";
			queue.offer(initial_url);
			if(!indexer.insertURLID(initial_url, pageID)){
				//title
				String initial_title = null;
				Parser initial_parser = new Parser(initial_url);
				NodeList initial_list = new NodeList();
				NodeFilter initial_filter = new TagNameFilter("title");
				for(NodeIterator e = initial_parser.elements();e.hasMoreNodes();) {
					e.nextNode().collectInto(initial_list,initial_filter);
				}
				for(int m = 0; m < initial_list.size(); m++) {
					Node e = initial_list.elementAt(m);
					if(e instanceof TitleTag) {
						initial_title = ((TitleTag)e).getTitle();								
					}
				}

				Page initial_p = new Page (initial_title, initial_url, getLastModifiedDate(initial_url), getPageSize(initial_url));
				indexer.insertPage(pageID, initial_p);
				pageID++;
			}



			for(int n = 0; n < PageMax; n++) {
				if(queue.isEmpty())
				{
					break;
				}
				String url = queue.poll();
				crawler.setURL(url);
				int current_pageID = indexer.getURLID(url);
				System.out.println("current PageID" + current_pageID);


				//extract text from title tag
				Vector<String> words_title = crawler.extractTitle(url);
				//remove stop word and do word stem
				Indexer.removeStopWord(words_title);
				Indexer.wordStem(words_title);

				for(int i = 0; i < words_title.size(); i++) {
					boolean word_exist = indexer.insertWordID(words_title.get(i), wordID);
					//					if(words_title.get(i)==null) {
					//						continue;
					//					}
					//					else {
					if(word_exist) {
						int current_wordID = indexer.getWordID(words_title.get(i));
						indexer.insertTitleWord(current_wordID, current_pageID, (indexer.getFrequency(current_wordID, current_pageID)+1),i);
						indexer.insertForward(current_pageID, current_wordID, indexer.getFrequency(current_wordID, current_pageID));
						wordID--;
					}
					else {
						indexer.insertTitleWord(wordID, current_pageID, (indexer.getFrequency(wordID, current_pageID)+1),i);
						indexer.insertForward(current_pageID, wordID, indexer.getFrequency(wordID, current_pageID));
						
					}
					wordID++;
					//					}
				}





				//extract text from body tag
				Vector<String> words = crawler.extractBody();
				Indexer.removeStopWord(words);
				Indexer.wordStem(words);

				for(int i = 0; i < words.size(); i++) {
					if(words.get(i).length() >= 30)
						continue;
					boolean word_exist = indexer.insertWordID(words.get(i), wordID);
					//					if(words.get(i)==null) {
					//						continue;
					//					}
					//					else {
					if(word_exist) {
						int current_wordID = indexer.getWordID(words.get(i));
						indexer.insertBodyWord(current_wordID, current_pageID, (indexer.getFrequency(current_wordID, current_pageID)+1),i);
						indexer.insertForward(current_pageID, current_wordID, indexer.getFrequency(current_wordID, current_pageID));
						
					}
					else {
						
						indexer.insertBodyWord(wordID, current_pageID, (indexer.getFrequency(wordID, current_pageID)+1),i);
						indexer.insertForward(current_pageID, wordID, indexer.getFrequency(wordID, current_pageID));
						wordID++;
					}
					//					}
				}




				Vector<String> links = crawler.extractLinks();
				for(int i = 0; i < links.size(); i++) {
					try{
						URL url_fetch = new URL(links.get(i));
						HttpURLConnection connection = (HttpURLConnection) url_fetch
								.openConnection();
						connection.setRequestMethod("GET");
						connection.setConnectTimeout(2000);
						connection.setReadTimeout(2000);

						if (connection.getResponseCode() == 200){

							boolean exist = indexer.insertURLID(links.get(i), pageID);
							if(links.get(i)==null) {
								continue;
							}
							else {
								if(exist) {
									if(indexer.getURLID(links.get(i))<PageMax)
										if(indexer.getPageContent(indexer.getURLID(links.get(i))).getModifiedDate().before(getLastModifiedDate(links.get(i))))
										{
											queue.offer(links.get(i));	
										}						
								}
								else {
									if(pageID < PageMax) {
										//title
										String title ="";
										Vector<String> v = Crawler.extractTitle(links.get(i));
										for(int x = 0; x < v.size(); x++) {
											title = title + v.get(x) + " ";
										}
										System.out.print(title);
										System.out.print(pageID);
										System.out.println("");


										Page p = new Page (title, links.get(i),getLastModifiedDate(links.get(i)), getPageSize(links.get(i)));
										indexer.insertPage(pageID, p);
									}
									indexer.insertChildLink(current_pageID, pageID);
									indexer.insertParentLink(pageID, current_pageID);
									queue.offer(links.get(i));
									pageID++;
								}
							}
						}
					}
					catch(Exception e){}

				}
				recman.commit();

			}

			recman.close();

		}

		catch (Exception e)
		{
			e.printStackTrace ();
		}



	}

	public static void main(String[] args){
		Crawler.fetch();
		System.out.println("fetch finished");
	}


}

