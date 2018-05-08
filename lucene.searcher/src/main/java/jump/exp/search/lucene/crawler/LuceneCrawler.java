package jump.exp.search.lucene.crawler;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

@Component
public class LuceneCrawler extends WebCrawler {

	private static IndexWriter writer;
	public static String indexPath = "./index";
	public static Pattern FILTERS = Pattern.compile(".*");
	private static int indexed = 0;
	private static int skipped = 0;

	Logger log = LoggerFactory.getLogger(getClass());
	
	public LuceneCrawler() throws IOException {
		super();
		if (writer == null) {
			log.info("Constructor");
			Directory dir = FSDirectory.open(Paths.get(indexPath));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			writer = new IndexWriter(dir, iwc);
		}
	}

	/**
	 * This method receives two parameters. The first parameter is the page in which
	 * we have discovered this new url and the second parameter is the new url. You
	 * should implement this function to specify whether the given url should be
	 * crawled or not (based on your crawling logic). In this example, we are
	 * instructing the crawler to ignore urls that have css, js, git, ... extensions
	 * and to only accept urls that start with "http://www.ics.uci.edu/". In this
	 * case, we didn't need the referringPage parameter to make the decision.
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		return FILTERS.matcher(href).matches();
	}

	/**
	 * This function is called when a page is fetched and ready to be processed by
	 * your program.
	 */
	@Override
	public void visit(Page page) {
		String threadName = this.getThread().getName();
		String url = page.getWebURL().getURL();
		log.info(threadName + " say FRONTIER LENGHT: " + myController.getFrontier().getQueueLength() + " Indexed: "
				+ indexed + " Skipped: " + skipped);
		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String html = htmlParseData.getHtml();
			Set<WebURL> links = htmlParseData.getOutgoingUrls();
			try {
				index(writer, html, url);
			} catch (IOException e) {
				log.error(e.getMessage());
			}
			log.info("Number of outgoing links: " + links.size());
		} else {
			log.info("<--SKIP " + url);
			incrementSkippedDoc();
		}
	}

	public static void finalized() throws IOException {
		if (writer != null)
			writer.close();
	}

	private void index(IndexWriter writer, String html, String url) throws IOException {
		org.jsoup.nodes.Document Jdoc = Jsoup.parse(html);
		String title = Jdoc.title();
		String text = Jdoc.text();
		log.debug("-->INDEX URL: " + url + " TITLE: " + title + " CONTENTS: " + text);
		Document doc = new Document();
		doc.add(new StringField("url", url, Field.Store.YES));
		doc.add(new TextField("contents", text, Field.Store.YES));
		doc.add(new TextField("title", title, Field.Store.YES));
		log.debug("adding " + url);
		writer.addDocument(doc);
		incrementIndexDoc();
	}

	private synchronized void incrementIndexDoc() {
		indexed++;
	}

	private synchronized void incrementSkippedDoc() {
		skipped++;
	}
}