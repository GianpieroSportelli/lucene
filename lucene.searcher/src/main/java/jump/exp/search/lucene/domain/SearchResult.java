package jump.exp.search.lucene.domain;

import java.util.List;

public class SearchResult{
	
	int page;
	int totalResult;
	List<Document> results;
	
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getTotalResult() {
		return totalResult;
	}
	public void setTotalResult(int totalResult) {
		this.totalResult = totalResult;
	}
	public List<Document> getResults() {
		return results;
	}
	public void setResults(List<Document> results) {
		this.results = results;
	}

}
