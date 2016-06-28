package io.github.mikesaelim.nomenclature;

import com.google.common.annotations.VisibleForTesting;
import lombok.Data;
import org.eclipse.egit.github.core.IResourceProvider;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PagedRequest;
import org.eclipse.egit.github.core.service.GitHubService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.List;

/**
 * Executes search queries against the GitHub API.
 *
 * This is necessary because the search capabilities of {@link RepositoryService} are restricted by the legacy search
 * API.  Feelsbadman
 *
 * TODO refactor so that we don't use this silly Eclipse library at all
 */
class SearchService extends GitHubService {

    public SearchService() {
        super();
    }

    public SearchService(GitHubClient client) {
        super(client);
    }

    /**
     * Retrieve info on the Java repositories with a minimum number of stars.
     *
     * @param minStars minimum number of stars; null is equivalent to 0
     * @return List of results
     * @throws IOException if there is a problem connecting, or if the rate limit has been exceeded
     */
    public List<RepositoryResult> searchJavaRepositoriesByStars(Integer minStars) throws IOException {
        PagedRequest<RepositoryResult> request = createSearchRequest(minStars);
        return getAll(request);
    }

    @VisibleForTesting PagedRequest<RepositoryResult> createSearchRequest(Integer minStars) {
        StringBuilder uriPath = new StringBuilder("/search/repositories?q=language:java");
        if (minStars != null) {
            uriPath.append("+stars:%3E").append(minStars);
        }
        uriPath.append("&sort=stars");

        PagedRequest<RepositoryResult> request = createPagedRequest();
        request.setUri(uriPath);
        request.setType(RepositoryContainer.class);

        return request;
    }

    private static class RepositoryContainer implements
            IResourceProvider<RepositoryResult> {

        private List<RepositoryResult> items;

        /**
         * @see org.eclipse.egit.github.core.IResourceProvider#getResources()
         */
        public List<RepositoryResult> getResources() {
            return items;
        }
    }

    @Data
    public static class RepositoryResult {
        private String fullName;
        private String defaultBranch;
    }

}
