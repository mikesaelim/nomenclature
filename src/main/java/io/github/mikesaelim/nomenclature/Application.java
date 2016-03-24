package io.github.mikesaelim.nomenclature;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.List;

public class Application {

    public static void main(String[] args) {
        RepositoryService repositoryService = new RepositoryService();

        try {
            List<Repository> repositories = repositoryService.getRepositories("mikesaelim");

            repositories.stream().forEach(repository -> System.out.println(repository.getName()));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

}
