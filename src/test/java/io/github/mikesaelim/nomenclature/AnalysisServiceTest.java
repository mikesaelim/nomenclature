package io.github.mikesaelim.nomenclature;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.service.DataService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class AnalysisServiceTest {

    @Mock
    private DataService dataService;
    @Mock
    private SearchService searchService;

    private AnalysisService analysisService;

    private static final RepositoryId REPO_ID_1 = new RepositoryId("mikesaelim", "repo1");
    private static final RepositoryId REPO_ID_2 = new RepositoryId("mikesaelim", "repo2");

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        analysisService = new AnalysisService(dataService, searchService);

        SearchService.RepositoryResult repo1 = new SearchService.RepositoryResult();
        repo1.setFullName("mikesaelim/repo1");
        repo1.setDefaultBranch(null);

        Tree tree1 = new Tree();
        tree1.setTree(Lists.newArrayList(
                createTreeEntry(TreeEntry.TYPE_BLOB, "CheeseRepository.java"),
                createTreeEntry(TreeEntry.TYPE_BLOB, "CheeseService.java")
        ));
        when(dataService.getTree(REPO_ID_1, "master", true)).thenReturn(tree1);

        SearchService.RepositoryResult repo2 = new SearchService.RepositoryResult();
        repo2.setFullName("mikesaelim/repo2");
        repo2.setDefaultBranch("develop");

        Tree tree2 = new Tree();
        tree2.setTree(Lists.newArrayList(
                createTreeEntry(TreeEntry.TYPE_BLOB, "BaconRepository.java")
        ));
        when(dataService.getTree(REPO_ID_2, "develop", true)).thenReturn(tree2);

        when(searchService.searchJavaRepositoriesByStars(25)).thenReturn(Lists.newArrayList(repo1, repo2));
    }

    @Test
    public void testParseRepositories() throws Exception {
        Map<String, Integer> result = analysisService.parseRepositories(25);

        assertEquals(2, result.size());
        assertEquals(2, result.get("Repository").intValue());
        assertEquals(1, result.get("Service").intValue());
    }

    @Test
    public void testParseRepository() throws Exception {
        TreeMultimap<String, String> result = analysisService.parseRepository(REPO_ID_1, "master");

        assertEquals(2, result.size());
        assertTrue(result.get("Repository").contains("CheeseRepository"));
        assertTrue(result.get("Service").contains("CheeseService"));
    }

    @Test
    public void testExtractClassNames() throws Exception {
        Tree tree = new Tree();
        tree.setTree(Lists.newArrayList(
                createTreeEntry(TreeEntry.TYPE_BLOB, "SomeClass.java"),
                createTreeEntry(TreeEntry.TYPE_BLOB, "src/main/java/com/cheese/AnotherClass.java"),
                createTreeEntry(TreeEntry.TYPE_BLOB, "OtherFile.log"),
                createTreeEntry(TreeEntry.TYPE_BLOB, "src/main/java/com/cheese/build.gradle"),
                createTreeEntry(TreeEntry.TYPE_TREE, "MysteryTree.java")
        ));

        Set<String> result = AnalysisService.extractClassNames(tree);

        assertEquals(Sets.newHashSet("SomeClass", "AnotherClass"), result);
    }

    @Test
    public void testCollateByRoot() throws Exception {
        Set<String> classNames = Sets.newHashSet("ThingManager", "ThingManagerTest", "Onlyonetoken",
                "StuffManager", "StuffManagerImpl", "BlahService", "", "   ", "Dont$ParseMe");

        TreeMultimap<String, String> collatedByRoot = AnalysisService.collateByRoot(classNames);

        assertEquals(3, collatedByRoot.size());
        assertTrue(collatedByRoot.keySet().containsAll(Sets.newHashSet("Manager", "Service")));
        assertTrue(collatedByRoot.get("Manager").containsAll(Sets.newHashSet("ThingManager", "StuffManager")));
        assertTrue(collatedByRoot.get("Service").contains("BlahService"));
    }

    private TreeEntry createTreeEntry(String type, String path) {
        TreeEntry treeEntry = new TreeEntry();
        treeEntry.setType(type);
        treeEntry.setPath(path);
        return treeEntry;
    }

}