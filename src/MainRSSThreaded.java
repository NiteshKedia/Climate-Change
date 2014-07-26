import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.text.html.HTMLDocument;

import org.xml.sax.SAXException;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
import de.l3s.boilerpipe.sax.HTMLFetcher;
class RSSThreaded implements Runnable{
	Thread mainthread;
	String mainthreadLink;

	RSSThreaded(String link){
		mainthreadLink = link;
		mainthread =  new Thread(this,mainthreadLink);
		mainthread.start();
		// System.out.println("Creating " +  childthreadLink );
	}
	public void run() {
		Set<LinkThread> listChildThread = new HashSet<LinkThread>();
		RSSFeedParser parser = new RSSFeedParser(mainthreadLink);
		Feed feed = parser.readFeed();
		String htmlcontent = null;
		String title = null;
		for (FeedMessage message : feed.getMessages()) {
			LinkThread T1 = new LinkThread( message);
			listChildThread.add(T1);

		}
		for (LinkThread T : listChildThread) {
			try {
				T.childthread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}  
public class MainRSSThreaded{
	public static void main(String[] args) throws MalformedURLException {
		while(true){
			try {
				Set<RSSThreaded> listMainThread = new HashSet<RSSThreaded>();
				BufferedReader br = null;
				String sCurrentLine;
				br = new BufferedReader(new FileReader("rsslinks.txt"));
				//System.out.println("Reading RSS File.....");
				while ((sCurrentLine = br.readLine()) != null) {
					System.out.println("RSS Link:" + sCurrentLine);
					RSSThreaded T = new RSSThreaded(sCurrentLine);
					listMainThread.add(T);
					//T.start();
				}
				for (RSSThreaded T : listMainThread) {
					T.mainthread.join();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		}
	}
}
