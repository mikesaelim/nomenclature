package io.github.mikesaelim.nomenclature;

import com.google.common.collect.Lists;
import com.google.common.collect.TreeMultimap;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.service.DataService;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

public class Application {

    public static void main(String[] args) {
        DataService dataService = new DataService();

        Tree tree;
        try {
            tree = dataService.getTree(RepositoryId.create("mikesaelim", "arXivOAIHarvester"), "master", true);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }

        TreeMultimap<String, String> javaClassNamesByRoot = extractClassNamesAndCollateByRoot(tree);

        for (String root : javaClassNamesByRoot.keySet()) {
            Set<String> classNames = javaClassNamesByRoot.get(root);
            System.out.println(root + " (" + classNames.size() + "):");
            for (String className : classNames) {
                System.out.println("    " + className);
            }
        }
    }

    /**
     * TODO this method's name is intentionally annoying so that I will refactor this into smaller methods later
     * @param tree
     * @return
     */
    private static TreeMultimap<String, String> extractClassNamesAndCollateByRoot(Tree tree) {
        Set<String> classNames = tree.getTree().stream()
                .filter(t -> TreeEntry.TYPE_BLOB.equals(t.getType()))
                .filter(t -> t.getPath().endsWith(".java"))
                .map(t -> substringAfterLast(t.getPath(), "/"))
                .map(s -> removeEnd(s, ".java"))
                .sorted()
                .collect(toSet());

        TreeMultimap<String, String> classNamesByRoot = TreeMultimap.create();

        classNames.stream()
                .filter(StringUtils::isNotBlank)
                .filter(StringUtils::isAlphanumeric)
                .map(Application::tokenize)
                .filter(name -> name.numTokens() >= 2)
                .filter(name -> !name.getRoot().equals("Test"))
                .forEach(name -> classNamesByRoot.put(name.getRoot(), name.getClassName()));

        return classNamesByRoot;
    }

    private static TokenizedClassName tokenize(String className) {
        return new TokenizedClassName(className,
                Lists.newArrayList(StringUtils.splitByCharacterTypeCamelCase(className)));
    }

    @Value
    private static class TokenizedClassName {
        String className;
        List<String> tokens;

        public int numTokens() {
            return tokens.size();
        }

        public String getRoot() {
            return tokens.get(numTokens() - 1);
        }
    }

}
