package jump.exp.search.lucene.searcher.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties()
public class Configuration {

	@Value("${search.index}")
	private String indexPath;

	@Value("${search.field}")
	private String field;

	@Value("${search.summary.phrase.lenght}")
	private int phraseLenght;

	@Value("${search.summary.phrase.number}")
	private int phraseNum;

	@Value("${crawl.folder}")
	private String crawlFolder;

	@Value("${crawl.userAgent}")
	private String crawlUserAgent;

	@Value("${crawl.site}")
	private String crawlSite;

	@Value("${crawl.filter}")
	private String crawlFilter;
	
	@Value("${crawl.thread}")
	private int crawlThread;
	
	@Value("${craw.index}")
	private String crawlIndexPath;

	public String getField() {
		return field;
	}

	public void setField(String filed) {
		this.field = filed;
	}

	public String getIndexPath() {
		return indexPath;
	}

	public void setIndexPath(String indexPath) {
		this.indexPath = indexPath;
	}

	public int getPhraseLenght() {
		return phraseLenght;
	}

	public void setPhraseLenght(int phraseLenght) {
		this.phraseLenght = phraseLenght;
	}

	public int getPhraseNum() {
		return phraseNum;
	}

	public void setPhraseNum(int phraseNum) {
		this.phraseNum = phraseNum;
	}

	public String getCrawlFolder() {
		return crawlFolder;
	}

	public void setCrawlFolder(String crawlFolder) {
		this.crawlFolder = crawlFolder;
	}

	public String getCrawlUserAgent() {
		return crawlUserAgent;
	}

	public void setCrawlUserAgent(String crawlUserAgent) {
		this.crawlUserAgent = crawlUserAgent;
	}

	public String getCrawlSite() {
		return crawlSite;
	}

	public void setCrawlSite(String crawlSite) {
		this.crawlSite = crawlSite;
	}

	public String getCrawlFilter() {
		return crawlFilter;
	}

	public void setCrawlFilter(String crawlFilter) {
		this.crawlFilter = crawlFilter;
	}

	public int getCrawlThread() {
		return crawlThread;
	}

	public void setCrawlThread(int crawlThread) {
		this.crawlThread = crawlThread;
	}

	public String getCrawlIndexPath() {
		return crawlIndexPath;
	}

	public void setCrawlIndexPath(String crawlIndexPath) {
		this.crawlIndexPath = crawlIndexPath;
	}

}
