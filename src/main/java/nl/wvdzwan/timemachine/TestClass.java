package nl.wvdzwan.timemachine;

import java.io.IOException;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import nl.wvdzwan.timemachine.callgraph.ArtifactRecord;
import nl.wvdzwan.timemachine.callgraph.ClassToArtifactResolver;
import nl.wvdzwan.timemachine.callgraph.FolderLayout.MavenFolderLayout;
import nl.wvdzwan.timemachine.callgraph.WalaAnalysis;

public class TestClass {

    public static void main(String[] args) throws IOException, ClassHierarchyException {

        WalaAnalysis analysis = new WalaAnalysis(
                "/home/wouter/Tagspaces/Thesis/dated-maven/maven-timemachine/target/local-repo/org/xwiki/commons/xwiki-commons-diff-api/5.2.1/xwiki-commons-diff-api-5.2.1.jar",
                "/home/wouter/Tagspaces/Thesis/dated-maven/maven-timemachine/target/local-repo/org/xwiki/commons/xwiki-commons-component-api/5.2.1/xwiki-commons-component-api-5.2.1.jar:/home/wouter/Tagspaces/Thesis/dated-maven/maven-timemachine/target/local-repo/org/xwiki/commons/xwiki-commons-stability/5.2.1/xwiki-commons-stability-5.2.1.jar:/home/wouter/Tagspaces/Thesis/dated-maven/maven-timemachine/target/local-repo/org/xwiki/commons/xwiki-commons-text/5.2.1/xwiki-commons-text-5.2.1.jar:/home/wouter/Tagspaces/Thesis/dated-maven/maven-timemachine/target/local-repo/org/slf4j/slf4j-api/1.7.5/slf4j-api-1.7.5.jar:/home/wouter/Tagspaces/Thesis/dated-maven/maven-timemachine/target/local-repo/javax/inject/javax.inject/1/javax.inject-1.jar:/home/wouter/Tagspaces/Thesis/dated-maven/maven-timemachine/target/local-repo/org/xwiki/commons/xwiki-commons-logging-api/5.2.1/xwiki-commons-logging-api-5.2.1.jar:/home/wouter/Tagspaces/Thesis/dated-maven/maven-timemachine/target/local-repo/org/xwiki/commons/xwiki-commons-observation-api/5.2.1/xwiki-commons-observation-api-5.2.1.jar:/home/wouter/Tagspaces/Thesis/dated-maven/maven-timemachine/target/local-repo/org/apache/commons/commons-lang3/3.1/commons-lang3-3.1.jar:/home/wouter/Tagspaces/Thesis/dated-maven/maven-timemachine/target/local-repo/commons-io/commons-io/2.4/commons-io-2.4.jar:/home/wouter/Tagspaces/Thesis/dated-maven/maven-timemachine/target/local-repo/com/googlecode/java-diff-utils/diffutils/1.3.0/diffutils-1.3.0.jar",
                "Java60RegressionExclusions.txt");

        CallGraph cg = analysis.run();

        ClassToArtifactResolver resolver = new ClassToArtifactResolver(analysis.getExtendedCha(), new MavenFolderLayout("/home/wouter/Tagspaces/Thesis/dated-maven/maven-timemachine/target/local-repo/"));



        IClass klass = cg.getNode(5).getMethod().getDeclaringClass();

        ArtifactRecord record = resolver.artifactRecordFromClass(klass);

        System.out.println(record.getIdentifier());

    }
}
