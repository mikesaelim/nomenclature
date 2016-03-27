package io.github.mikesaelim.nomenclature;

import com.google.common.collect.TreeMultimap;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.service.DataService;

import java.io.IOException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

class AnalysisService {

    private DataService dataService;

    public AnalysisService(DataService dataService) {
        this.dataService = dataService;
    }

    public TreeMultimap<String, String> parseRepository(RepositoryId repositoryId) throws IOException {
        Tree tree = dataService.getTree(repositoryId, "master", true);

        return extractClassNamesAndCollateByRoot(tree);
    }

    /**
     * TODO this method's name is intentionally annoying so that I will refactor this into smaller methods later
     * @param tree
     * @return
     */
    private static TreeMultimap<String, String> extractClassNamesAndCollateByRoot(Tree tree) {
        Set<String> classNames = tree.getTree().stream()
                .filter(isFile).filter(isJava)
                .map(toFileName).map(toClassName)
                .sorted()
                .collect(toSet());

        TreeMultimap<String, String> classNamesByRoot = TreeMultimap.create();

        classNames.stream()
                .filter(StringUtils::isNotBlank)
                .filter(StringUtils::isAlphanumeric)
                .map(TokenizedClassName::fromClassName)
                .filter(hasAtLeastTwoTokens)
                .filter(isTestClassName.negate())
                .forEach(name -> classNamesByRoot.put(name.getRoot(), name.getClassName()));

        return classNamesByRoot;
    }

    private static final Predicate<TreeEntry> isFile = t -> TreeEntry.TYPE_BLOB.equals(t.getType());
    private static final Predicate<TreeEntry> isJava = t -> t.getPath().endsWith(".java");
    private static final Function<TreeEntry, String> toFileName = t -> substringAfterLast(t.getPath(), "/");
    private static final Function<String, String> toClassName = s -> removeEnd(s, ".java");

    private static final Predicate<TokenizedClassName> hasAtLeastTwoTokens = tcn -> tcn.numTokens() >= 2;
    private static final Predicate<TokenizedClassName> isTestClassName = tcn -> tcn.getRoot().equals("Test");

}
