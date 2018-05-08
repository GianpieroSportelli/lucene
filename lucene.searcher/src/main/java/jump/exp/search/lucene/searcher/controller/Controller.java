package jump.exp.search.lucene.searcher.controller;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import jump.exp.search.lucene.crawler.LuceneCrawler;
import jump.exp.search.lucene.domain.SearchResult;
import jump.exp.search.lucene.searcher.configuration.Configuration;
import jump.exp.search.lucene.searcher.lucene.Searcher;


@RestController
@RequestMapping("/")
public class Controller {
	
	static final Logger log=LoggerFactory.getLogger(Controller.class);
	
	private Configuration conf;
	private Searcher searcher;
	
	
	@Autowired
	public Controller(Configuration conf,Searcher searcher) {
		this.conf=conf;
		this.searcher=searcher;
		log.debug("Searcher is null? "+(searcher==null));
	}
	
	@RequestMapping("/test")
	public void test(){
		log.debug("DEBUG");
		log.info("INFO");
		log.warn("WARN");
		log.error("ERROR");
	}
	
	@RequestMapping(path="/search" , method = RequestMethod.GET, params = { "q","page","row"}, produces = "application/json")
	public String search(@RequestParam(value = "q") String q,@RequestParam(value = "page") int page,@RequestParam(value = "row") int row) throws IOException, InvalidTokenOffsetsException, ParseException {
		SearchResult result= searcher.doPagingSearch(row, page, q);
		return new ObjectMapper().writeValueAsString(result);
	}
	
	@RequestMapping(path="/crawling")
	public void crawl() throws Exception{
		
		CrawlConfig config = new CrawlConfig();
		config.setMaxDepthOfCrawling(10);
		config.setCrawlStorageFolder(conf.getCrawlFolder());
		
		PageFetcher pageFetcher = new PageFetcher(config);
		
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		
		robotstxtConfig.setUserAgentName(conf.getCrawlUserAgent());
		
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
		
		controller.addSeed(conf.getCrawlSite());
		LuceneCrawler.FILTERS=Pattern.compile(conf.getCrawlFilter());
		LuceneCrawler.indexPath=conf.getCrawlIndexPath();
		
		controller.start(LuceneCrawler.class, conf.getCrawlThread());
		
		while(!controller.isFinished());
		
		LuceneCrawler.finalized();
	}
	
	
}
