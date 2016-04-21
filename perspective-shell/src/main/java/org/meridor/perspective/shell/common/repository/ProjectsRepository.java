package org.meridor.perspective.shell.common.repository;

import org.meridor.perspective.shell.common.request.FindFlavorsRequest;
import org.meridor.perspective.shell.common.request.FindKeypairsRequest;
import org.meridor.perspective.shell.common.request.FindNetworksRequest;
import org.meridor.perspective.shell.common.request.FindProjectsRequest;
import org.meridor.perspective.shell.common.result.FindFlavorsResult;
import org.meridor.perspective.shell.common.result.FindKeypairsResult;
import org.meridor.perspective.shell.common.result.FindNetworksResult;
import org.meridor.perspective.shell.common.result.FindProjectsResult;

import java.util.List;

public interface ProjectsRepository {
    
    List<FindProjectsResult> findProjects(FindProjectsRequest findProjectsRequest);

    List<FindFlavorsResult> findFlavors(FindFlavorsRequest findFlavorsRequest);

    List<FindNetworksResult> findNetworks(FindNetworksRequest findNetworksRequest);
    
    List<FindKeypairsResult> findKeypairs(FindKeypairsRequest findKeypairsRequest);
    
}
