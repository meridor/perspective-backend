package org.meridor.perspective.shell.validator;

import org.meridor.perspective.beans.*;
import org.meridor.perspective.framework.EntityGenerator;
import org.meridor.perspective.shell.query.*;
import org.meridor.perspective.shell.repository.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TestRepository implements ProjectsRepository, ImagesRepository, InstancesRepository, SettingsRepository, FiltersAware, SettingsAware {
    
    public static final String TEST = "test";
    public static final String ONE = "one";
    public static final String TWO = "two";
    
    @Override
    public List<Image> showImages(ShowImagesQuery showImagesQuery) {
        return Collections.singletonList(EntityGenerator.getImage());
    }

    @Override
    public Set<String> addImages(AddImagesQuery addImagesQuery) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> deleteImages(DeleteImagesQuery deleteImagesQuery) {
        deleteImagesQuery.getPayload();
        return Collections.emptySet();
    }

    @Override
    public List<Instance> showInstances(ShowInstancesQuery showInstancesQuery) {
        return Collections.singletonList(EntityGenerator.getInstance());
    }

    @Override
    public Set<String> addInstances(AddInstancesQuery addInstancesQuery) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> deleteInstances(ModifyInstancesQuery modifyInstancesQuery) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> rebootInstances(ModifyInstancesQuery modifyInstancesQuery) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> hardRebootInstances(ModifyInstancesQuery modifyInstancesQuery) {
        return Collections.emptySet();
    }

    @Override
    public List<Project> showProjects(ShowProjectsQuery query) {
        return Collections.singletonList(EntityGenerator.getProject());
    }

    @Override
    public Map<Project, List<Flavor>> showFlavors(ShowFlavorsQuery showFlavorsQuery) {
        return Collections.singletonMap(EntityGenerator.getProject(), Collections.singletonList(EntityGenerator.getFlavor()));
    }

    @Override
    public Map<Project, List<Network>> showNetworks(ShowNetworksQuery showNetworksQuery) {
        return Collections.singletonMap(EntityGenerator.getProject(), Collections.singletonList(EntityGenerator.getNetwork()));
    }

    @Override
    public Set<String> set(String data) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> unset(String data) {
        return Collections.emptySet();
    }

    @Override
    public Map<String, String> showSettings(boolean all) {
        return getSettingsMap();
    }

    @Override
    public Map<String, String> showFilters(boolean all) {
        return getSettingsMap();
    }

    @Override
    public boolean hasSetting(Setting setting) {
        return true;
    }

    @Override
    public void setSetting(Setting setting, Set<String> value) {
        //Do nothing
    }

    @Override
    public void unsetSetting(Setting setting) {
        //Do nothing
    }

    @Override
    public Set<String> getSetting(Setting setting) {
        return new HashSet<>(Arrays.asList(ONE, TWO));
    }

    @Override
    public <T> T getSettingAs(Setting setting, Class<T> cls) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getSettings(boolean all) {
        return getSettingsMap();
    }

    private static Map<String, String> getSettingsMap() {
        return new HashMap<String, String>(){
            {
                put(ONE, ONE);
                put(TWO, TWO);
            }
        };
    }

    @Override
    public boolean hasFilter(Field field) {
        return true;
    }

    @Override
    public void setFilter(Field field, Set<String> value) {
        //Do nothing
    }

    @Override
    public void unsetFilter(Field field) {
        //Do nothing
    }

    @Override
    public Set<String> getFilter(Field field) {
        return new HashSet<>(Arrays.asList(ONE, TWO));
    }

    @Override
    public <T> T getFilterAs(Field field, Class<T> cls) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getFilters(boolean all) {
        return getSettingsMap();
    }
}
