package io.github.mikesaelim.nomenclature;

import com.google.common.collect.TreeMultimap;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.DataService;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class Application {

    public static void main(String[] args) throws IOException {
        AnalysisService analysisService = new AnalysisService(new DataService(), new SearchService());

//        RepositoryId repositoryId = RepositoryId.create("elastic", "elasticsearch");
//        TreeMultimap<String, String> javaClassNamesByRoot = analysisService.parseRepository(repositoryId);
//        printResults(javaClassNamesByRoot);

        Map<String, Integer> multiplicities = analysisService.parseRepositories(5000);
        printResults(multiplicities);
    }

    private static void printResults(TreeMultimap<String, String> javaClassNamesByRoot) {
        System.out.println();
        System.out.println();
        for (String root : javaClassNamesByRoot.keySet()) {
            Set<String> classNames = javaClassNamesByRoot.get(root);
            System.out.println(root + " (" + classNames.size() + "):");
            for (String className : classNames) {
                System.out.println("    " + className);
            }
        }

        Map<String, Integer> multiplicities = javaClassNamesByRoot.keySet().stream()
                .collect(toMap(Function.<String>identity(), s -> javaClassNamesByRoot.get(s).size()));

        printResults(multiplicities);
    }

    private static void printResults(Map<String, Integer> multiplicities) {
        System.out.println();
        System.out.println();
        System.out.println("Mult.   Classname");
        System.out.println("----------------------------------");
        multiplicities.keySet().stream()
                .filter(s -> multiplicities.get(s) >= 10)
                .sorted((s1, s2) -> Integer.compare(multiplicities.get(s2), multiplicities.get(s1)))
                .forEachOrdered(s -> {
                    System.out.println(StringUtils.leftPad(multiplicities.get(s).toString(), 6) + "  " + s);
                });
    }

}
