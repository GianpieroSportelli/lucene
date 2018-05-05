package jump.exp.search.lucene.searcher.controller;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jump.exp.search.lucene.searcher.configuration.Configuration;
import jump.exp.search.lucene.searcher.lucene.Searcher;


@RestController
@RequestMapping("/")
public class Controller {
	
	static final Logger log=LoggerFactory.getLogger(Controller.class);
	
	@SuppressWarnings("unused")
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
	
	@RequestMapping(path="/search" , method = RequestMethod.GET, params = { "q"})
	public String search(@RequestParam(value = "q") String q) throws IOException, InvalidTokenOffsetsException, ParseException {
		return searcher.doPagingSearch(10, 1, q);
	}
	
	
}
