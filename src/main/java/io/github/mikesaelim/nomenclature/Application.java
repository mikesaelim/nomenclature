package io.github.mikesaelim.nomenclature;

import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.service.DataService;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

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

        List<String> javaClassNames = tree.getTree().stream()
                .filter(t -> TreeEntry.TYPE_BLOB.equals(t.getType()))
                .filter(t -> t.getPath().endsWith(".java"))
                .map(t -> t.getPath().substring(t.getPath().lastIndexOf("/") + 1))
                .map(s -> s.substring(0, s.length() - 5))
                .filter(s -> !s.endsWith("Test"))
                .sorted()
                .collect(toList());

        // TODO filter on CamelCase
        // TODO split into tokens based on CamelCase
        // TODO filter out one-token names
        // TODO use last token as key in multimap

        javaClassNames.forEach(System.out::println);
    }

}
