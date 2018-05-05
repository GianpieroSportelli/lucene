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
		
	
}
