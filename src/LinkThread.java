import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.omg.CORBA.portable.InputStream;
import org.xml.sax.SAXException;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
import de.l3s.boilerpipe.sax.HTMLFetcher;

class LinkThread implements Runnable{
	Thread childthread;
	String childthreadLink; 
	String filename; 
	String publishedDate;
	String title;
	static int count=0;

	LinkThread( FeedMessage message){	
		childthreadLink = message.getLink();
		publishedDate = message.getpubDate();
		title = message.getTitle();
		filename = title;
		childthread = new Thread (this,childthreadLink);
		childthread.start();
	}
	public synchronized void run() {
		//String title = null;
		try {
			String domain = " ";
			String line = "", all = "";
			URL url = new URL(childthreadLink);
			System.out.println("Link :" + url);
			String htmlcontent = ArticleExtractor.INSTANCE.getText(url);
			final de.l3s.boilerpipe.sax.HTMLDocument htmlDoc = HTMLFetcher.fetch(url);
			final TextDocument doc = new BoilerpipeSAXInput(htmlDoc.toInputSource()).getTextDocument();
			//title= doc.getTitle();
			domain = url.getHost();
			//filename = filename.replaceAll("[:<>;\\|]/","");
			filename = filename.replace("<","");
			filename = filename.replace(">","");
			filename = filename.replace("*","");
			filename = filename.replace(";","");
			filename = filename.replace(":","");
			filename = filename.replace("\\","");
			filename = filename.replace("]","");
			filename = filename.replace("|","");
			filename = filename.replace('?','\0');
			filename = filename.replace("'","");
			filename = filename.replace("`","");
			filename = filename.trim();
			//filename = filename.replace('.','\0');
			//System.out.println(filename);
			String textfilename =filename +  ".txt";
			String htmlfilename = filename + ".html";
			FileWriter saveFile = new FileWriter("Text Content//"+textfilename);
			saveFile.write(htmlcontent);
			saveFile.close();
			java.io.InputStream inStr =  url.openConnection().getInputStream();
			BufferedInputStream bins = new BufferedInputStream(inStr);
			//System.out.println("C:/Users/nkedia1/workspace/RSSFeed/HTML Content/"+htmlfilename);
			FileOutputStream fostreame = new FileOutputStream("C:/Users/nkedia1/workspace/RSSFeed/HTML Content/"+htmlfilename);
			int c;
			while((c= bins.read())!=-1)
			{
				fostreame.write(c);
			}
			fostreame.close();
			bins.close();
			inStr.close();



			Connection connection = null;
			PreparedStatement pst = null;
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/postgres","postgres","kediya");
			if (connection != null) {
				ResultSet rs; 
				count++;
				String stm = "INSERT INTO rssfeeds(title, url, pubdate, content, domain) VALUES(?, ?, ?, ?, ?)";
				pst = connection.prepareStatement(stm);
				pst.setString(1, title);
				pst.setString(2, childthreadLink); 
				pst.setString(3, publishedDate);
				pst.setString(4, htmlcontent);
				pst.setString(5, domain);
				pst.executeUpdate();
				connection.close();




			} else {
				System.out.println("Failed to make connection!");
			}
		} catch (ClassNotFoundException | SQLException| BoilerpipeProcessingException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Failed! Check output console");
			e.printStackTrace();
		}

	}



}