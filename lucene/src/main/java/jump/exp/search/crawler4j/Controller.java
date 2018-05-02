package jump.exp.search.crawler4j;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {
	public static void main(String[] args) throws Exception {

		ConsoleAppender console = new ConsoleAppender(); // create appender
		// configure the appender
		String PATTERN = "%d [%p|%c|%C{1}] %m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.FATAL);
		console.activateOptions();
		// add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(console);

		FileAppender fa = new FileAppender();
		fa.setName("FileLogger");
		fa.setFile("./log/mylog.log");
		fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
		fa.setThreshold(Level.DEBUG);
		fa.setAppend(true);
		fa.activateOptions();

		// add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(fa);
		// repeat with all other desired appenders

		String crawlStorageFolder = "./crawl/root";
		int numberOfCrawlers = 20;

		CrawlConfig config = new CrawlConfig();
		config.setMaxDepthOfCrawling(0);
		config.setCrawlStorageFolder(crawlStorageFolder);

		/*
		 * Instantiate the controller for this crawl.
		 */
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

		/*
		 * For each crawl, you need to add some seed urls. These are the first
		 * URLs that are fetched and then the crawler starts following links
		 * which are found in these pages https://www.unipolsai.it/homepage
		 */
		controller.addSeed("https://www.unipolsai.it/homepage");
		// controller.addSeed("http://www.ics.uci.edu/~lopes/");
		// controller.addSeed("http://www.ics.uci.edu/~welling/");
		//controller.addSeed("http://www.ics.uci.edu/");
		//controller.addSeed("http://www.lastampa.it");
		//controller.addSeed("https://www.weroad.it");
		/*
		 * Start the crawl. This is a blocking operation, meaning that your code
		 * will reach the line after this only when crawling is finished.
		 */
		controller.start(LuceneCrawler.class, numberOfCrawlers);
		while(!controller.isFinished()){
			
		}
		LuceneCrawler.finalized();
	}
}
