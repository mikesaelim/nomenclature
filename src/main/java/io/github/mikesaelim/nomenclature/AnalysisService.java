package io.github.mikesaelim.nomenclature;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.service.DataService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Nomenclature analysis of GitHub Java repositories.
 */
class AnalysisService {

    private DataService dataService;
    private SearchService searchService;

    public AnalysisService(DataService dataService, SearchService searchService) {
        this.dataService = dataService;
        this.searchService = searchService;
    }

    public Map<String, Integer> parseRepositories(Integer minStars) throws IOException {
        List<Pair<RepositoryId, String>> searchResults = searchService.searchJavaRepositoriesByStars(minStars).stream()
                .map(rr -> Pair.of(
                        RepositoryId.create(substringBefore(rr.getFullName(), "/"), substringAfter(rr.getFullName(), "/")),
                        defaultIfBlank(rr.getDefaultBranch(), "master")
                        ))
                .collect(toList());

        System.out.println(searchResults.toString());

        Map<String, Integer> multiplicities = Maps.newHashMap();
        for (Pair<RepositoryId, String> searchResult : searchResults) {
            System.out.println("Parsing " + searchResult.getLeft().toString() + " : " + searchResult.getRight() + " ...");

            TreeMultimap<String, String> classNames = parseRepository(searchResult.getLeft(), searchResult.getRight());

            for (String className : classNames.keySet()) {
                multiplicities.merge(className, classNames.get(className).size(), Integer::sum);
            }
        }

        return multiplicities;
    }

    public TreeMultimap<String, String> parseRepository(RepositoryId repositoryId, String branchName) throws IOException {
        Tree tree = dataService.getTree(repositoryId, branchName, true);

        Set<String> classNames = extractClassNames(tree);

        return collateByRoot(classNames);
    }

    /**
     * Extract Java class names from a GitHub Tree of files.
     */
    @VisibleForTesting static Set<String> extractClassNames(Tree tree) {
        return tree.getTree().stream()
                .filter(isFile).filter(isJava)
                .map(toFileName).map(toClassName)
                .collect(toSet());
    }

    /**
     * For classes with names of the form (prefix)(root), collate the class names by their root.  This also filters out
     * some special cases, like test classes or implementation classes (so we don't double-count).
     *
     * @return TreeMultiMap keyed by root
     */
    @VisibleForTesting static TreeMultimap<String, String> collateByRoot(Set<String> classNames) {
        TreeMultimap<String, String> classNamesByRoot = TreeMultimap.create();

        classNames.stream()
                .filter(StringUtils::isNotBlank)
                .filter(StringUtils::isAlphanumeric)
                .map(TokenizedClassName::fromClassName)
                .filter(hasAtLeastTwoTokens)
                .filter(isTestClassName.negate())
                .filter(isImplClassName.negate())
                .forEach(name -> classNamesByRoot.put(name.getRoot(), name.getClassName()));

        return classNamesByRoot;
    }

    private static final Predicate<TreeEntry> isFile = t -> TreeEntry.TYPE_BLOB.equals(t.getType());
    private static final Predicate<TreeEntry> isJava = t -> t.getPath().endsWith(".java");
    private static final Function<TreeEntry, String> toFileName = t ->
            t.getPath().contains("/") ? substringAfterLast(t.getPath(), "/") : t.getPath();
    private static final Function<String, String> toClassName = s -> removeEnd(s, ".java");

    private static final Predicate<TokenizedClassName> hasAtLeastTwoTokens = tcn -> tcn.numTokens() >= 2;
    private static final Predicate<TokenizedClassName> isTestClassName =
            tcn -> tcn.getRoot().equals("Test") || tcn.getRoot().equals("Tests");
    private static final Predicate<TokenizedClassName> isImplClassName = tcn -> tcn.getRoot().equals("Impl");

}
