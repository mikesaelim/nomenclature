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

    public static void main(String[] args) {
        AnalysisService analysisService = new AnalysisService(new DataService());

        RepositoryId repositoryId = RepositoryId.create("elastic", "elasticsearch");

        try {
            TreeMultimap<String, String> javaClassNamesByRoot = analysisService.parseRepository(repositoryId);
            printResults(javaClassNamesByRoot);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
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

        System.out.println();
        System.out.println();
        System.out.println("Mult.  Classname");
        System.out.println("----------------------------------");
        multiplicities.keySet().stream()
                .sorted((s1, s2) -> Integer.compare(multiplicities.get(s2), multiplicities.get(s1)))
                .forEachOrdered(s -> {
                    System.out.println(StringUtils.leftPad(multiplicities.get(s).toString(), 5) + "  " + s);
                });
    }

}
