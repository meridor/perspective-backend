package org.meridor.perspective.shell.repository;

import org.meridor.perspective.shell.request.FindFlavorsRequest;
import org.meridor.perspective.shell.request.FindKeypairsRequest;
import org.meridor.perspective.shell.request.FindNetworksRequest;
import org.meridor.perspective.shell.request.FindProjectsRequest;
import org.meridor.perspective.shell.result.FindFlavorsResult;
import org.meridor.perspective.shell.result.FindKeypairsResult;
import org.meridor.perspective.shell.result.FindNetworksResult;
import org.meridor.perspective.shell.result.FindProjectsResult;

import java.util.List;

public interface ProjectsRepository {
    
    List<FindProjectsResult> findProjects(FindProjectsRequest findProjectsRequest);

    List<FindFlavorsResult> findFlavors(FindFlavorsRequest findFlavorsRequest);

    List<FindNetworksResult> findNetworks(FindNetworksRequest findNetworksRequest);
    
    List<FindKeypairsResult> findKeypairs(FindKeypairsRequest findKeypairsRequest);
    
}
