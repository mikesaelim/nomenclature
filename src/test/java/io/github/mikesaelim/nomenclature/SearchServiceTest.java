package io.github.mikesaelim.nomenclature;

import org.eclipse.egit.github.core.client.PagedRequest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SearchServiceTest {

    private SearchService searchService;

    @Before
    public void setUp() throws Exception {
        searchService = new SearchService();
    }

    @Test
    public void testCreateSearchRequest() throws Exception {
        PagedRequest<SearchService.RepositoryResult> request = searchService.createSearchRequest(12);

        assertEquals("/search/repositories?q=language:java+stars:%3E12&sort=stars", request.getUri());
    }

    @Test
    public void testCreateSearchRequest_NullMinStars() throws Exception {
        PagedRequest<SearchService.RepositoryResult> request = searchService.createSearchRequest(null);

        assertEquals("/search/repositories?q=language:java&sort=stars", request.getUri());
    }

}