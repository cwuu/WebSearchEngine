package phase1;

import java.util.Date;
import java.io.Serializable;

public class Page implements Serializable
{
	String title;
	String url;
	Date lastModifiedDate;
	int size;
	
	public Page(String title, String url, Date lastModifiedDate, int size)
	{
		this.title = title;
		this.url = url;
		this.lastModifiedDate = lastModifiedDate;
		this.size = size;
	}

	public Date getModifiedDate(){
		return lastModifiedDate;
	}

	public void setModifiedDate(Date date){
		this.lastModifiedDate = date;
	}
	
	public String getTitle(){
		return title;
	}

	public String getURL(){
		return url;
	}

	public int getPageSize(){
		return size;
	}
	
	public void setTitle(String _title) {
		title = _title;
	}
}
/*Page class only contain simple imformation of a page,
 * to print out <word, freq> and child link, retrieve from inverted files
  */
//
