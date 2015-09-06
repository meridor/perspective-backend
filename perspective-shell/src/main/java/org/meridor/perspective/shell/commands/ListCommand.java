package org.meridor.perspective.shell.commands;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.shell.client.ProjectsRepository;
import org.meridor.perspective.shell.misc.TableRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListCommand implements CommandMarker {
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private TableRenderer tableRenderer;
    
    @CliAvailabilityIndicator({"list projects"})
    public boolean isAvailable() {
        return true;
    }
    
    @CliCommand(value = "list projects", help = "List available projects")
    public String listProjects(
            @CliOption(key = "cloud", help = "Show projects belonging to specified cloud") String cloud
    ) {
//        CloudType cloudType = CloudType.fromValue(cloud);
        List<Project> projects = projectsRepository.listProjects(null);
        List<String[]> projectData = projects.stream().map(p -> new String[]{p.getId(), p.getName()}).collect(Collectors.toList());
        return tableRenderer.render(new String[]{"Id", "Name"}, projectData);
    }
    
}
