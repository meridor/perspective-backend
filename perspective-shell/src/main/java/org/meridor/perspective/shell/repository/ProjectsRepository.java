package org.meridor.perspective.shell.repository;

import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.beans.Keypair;
import org.meridor.perspective.beans.Network;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.shell.query.ShowFlavorsQuery;
import org.meridor.perspective.shell.query.ShowKeypairsQuery;
import org.meridor.perspective.shell.query.ShowNetworksQuery;
import org.meridor.perspective.shell.query.ShowProjectsQuery;

import java.util.List;
import java.util.Map;

public interface ProjectsRepository {
    
    List<Project> showProjects(ShowProjectsQuery query);

    Map<Project, List<Flavor>> showFlavors(ShowFlavorsQuery showFlavorsQuery);

    Map<Project, List<Network>> showNetworks(ShowNetworksQuery showNetworksQuery);
    
    Map<Project, List<Keypair>> showKeypairs(ShowKeypairsQuery showKeypairsQuery);
    
}
