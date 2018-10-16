package nl.wvdzwan.timemachine;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.*;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;


import java.io.File;

public class ModelFactory {


    public Model getModel(String groupId, String artifactId, String version) throws UnresolvableModelException {
        ArtifactRepositoryLayout layout = new DefaultRepositoryLayout();
        CustomRemoteRepository remoteRepository = new CustomRemoteRepository("http://repo1.maven.org/maven2/", layout);

        ModelResolver resolver = new CustomModelResolver(remoteRepository);

        File pom = remoteRepository.getPom( new DefaultArtifact(groupId, artifactId, version, "", "", "", new DefaultArtifactHandler()));

        DefaultModelBuilderFactory factory = new DefaultModelBuilderFactory();
        DefaultModelBuilder builder = factory.newInstance();

        ModelBuildingRequest buildingRequest = new DefaultModelBuildingRequest();
        buildingRequest.setPomFile(pom);
        buildingRequest.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        buildingRequest.setModelResolver(resolver);
        buildingRequest.setSystemProperties(System.getProperties());

        ModelBuildingResult result = null;
        try {
            result = builder.build(buildingRequest);
        } catch (ModelBuildingException e) {
            e.printStackTrace();
            for (ModelProblem problem : e.getProblems()) {
                System.err.println(problem.getMessage());
            }
            return null;
        }

        return result.getEffectiveModel();
    }

}
