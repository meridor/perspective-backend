package org.meridor.perspective.shell.repository;

import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.beans.Network;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.shell.query.ShowFlavorsQuery;
import org.meridor.perspective.shell.query.ShowNetworksQuery;
import org.meridor.perspective.shell.query.ShowProjectsQuery;

import java.util.List;

public interface ProjectsRepository {
    List<Project> showProjects(ShowProjectsQuery query);

    List<Flavor> showFlavors(String projectNames, String clouds, ShowFlavorsQuery showFlavorsQuery);

    List<Network> showNetworks(String projectNames, String clouds, ShowNetworksQuery showNetworksQuery);
}
