package org.meridor.perspective.shell.common.validator;

import org.meridor.perspective.beans.MetadataKey;
import org.meridor.perspective.beans.Network;
import org.meridor.perspective.framework.EntityGenerator;
import org.meridor.perspective.shell.common.repository.*;
import org.meridor.perspective.shell.common.request.*;
import org.meridor.perspective.shell.common.result.*;
import org.meridor.perspective.sql.*;
import org.springframework.stereotype.Repository;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.enumerateValues;

@Repository
public class TestRepository implements ProjectsRepository, ImagesRepository, InstancesRepository, SettingsRepository, FiltersAware, SettingsAware, QueryRepository {

    public static final String ONE = "one";
    public static final String TWO = "two";
    
    @Override
    public List<FindImagesResult> findImages(FindImagesRequest findImagesRequest) {
        FindImagesResult findImagesResult = new FindImagesResult(
                EntityGenerator.getImage().getId(),
                EntityGenerator.getImage().getRealId(),
                EntityGenerator.getImage().getName(),
                EntityGenerator.getImage().getCloudType().value(),
                EntityGenerator.getImage().getState().value(),
                EntityGenerator.getImage().getTimestamp().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
        );
        findImagesResult.getProjectIds().add(EntityGenerator.getProject().getId());
        findImagesResult.getProjectNames().add(EntityGenerator.getProject().getName());
        return Collections.singletonList(findImagesResult);
    }

    @Override
    public Set<String> addImages(AddImagesRequest addImagesRequest) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> deleteImages(Collection<String> imageIds) {
        return Collections.emptySet();
    }

    @Override
    public List<FindInstancesResult> findInstances(FindInstancesRequest findInstancesRequest) {
        return Collections.singletonList(new FindInstancesResult(
                EntityGenerator.getInstance().getId(),
                EntityGenerator.getInstance().getRealId(),
                EntityGenerator.getInstance().getName(),
                EntityGenerator.getProject().getId(),
                EntityGenerator.getProject().getName(),
                EntityGenerator.getInstance().getCloudId(),
                EntityGenerator.getInstance().getCloudType().value(),
                EntityGenerator.getImage().getName(),
                EntityGenerator.getFlavor().getName(),
                enumerateValues(EntityGenerator.getInstance().getAddresses()),
                EntityGenerator.getInstance().getState().value(),
                EntityGenerator.getInstance().getTimestamp().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
        ));
    }

    @Override
    public Map<String, Map<String, String>> getInstancesMetadata(FindInstancesRequest findInstancesRequest) {
        return Collections.singletonMap(EntityGenerator.getInstance().getName(), Collections.singletonMap(MetadataKey.CONSOLE_URL.value(), "http://localhost/"));
    }

    @Override
    public Set<String> addInstances(AddInstancesRequest addInstancesRequest) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> deleteInstances(Collection<String> instanceIds) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> rebootInstances(Collection<String> instanceIds) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> hardRebootInstances(Collection<String> instanceIds) {
        return Collections.emptySet();
    }

    @Override
    public List<FindProjectsResult> findProjects(FindProjectsRequest findProjectsRequest) {
        return Collections.singletonList(new FindProjectsResult(
                EntityGenerator.getProject().getId(),
                EntityGenerator.getProject().getName(),
                EntityGenerator.getProject().getCloudId(),
                EntityGenerator.getProject().getCloudType().value()
        ));
    }

    @Override
    public List<FindFlavorsResult> findFlavors(FindFlavorsRequest findFlavorsRequest) {
        return Collections.singletonList(new FindFlavorsResult(
                EntityGenerator.getFlavor().getId(),
                EntityGenerator.getFlavor().getName(),
                EntityGenerator.getProject().getName(),
                String.valueOf(EntityGenerator.getFlavor().getVcpus()),
                String.valueOf(EntityGenerator.getFlavor().getRam()),
                String.valueOf(EntityGenerator.getFlavor().getRootDisk()),
                String.valueOf(EntityGenerator.getFlavor().getEphemeralDisk())
        ));
    }

    @Override
    public List<FindNetworksResult> findNetworks(FindNetworksRequest findNetworksRequest) {
        Network network = EntityGenerator.getNetwork();
        List<String> subnets = EntityGenerator.getNetwork().getSubnets().stream()
                .map(
                        s -> String.format(
                                "%s/%s",
                                s.getCidr().getAddress(),
                                String.valueOf(s.getCidr().getPrefixSize())
                        )
                )
                .collect(Collectors.toList());

        FindNetworksResult findNetworksResult = new FindNetworksResult(
                network.getId(),
                network.getName(),
                EntityGenerator.getProject().getId(),
                EntityGenerator.getNetwork().getState(),
                EntityGenerator.getNetwork().isIsShared()
        );
        findNetworksResult.getSubnets().addAll(subnets);
        return Collections.singletonList(findNetworksResult);
    }

    @Override
    public List<FindKeypairsResult> findKeypairs(FindKeypairsRequest findKeypairsRequest) {
        return Collections.singletonList(new FindKeypairsResult(
                EntityGenerator.getKeypair().getName(),
                EntityGenerator.getKeypair().getFingerprint(),
                EntityGenerator.getProject().getName()
        ));
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
        return getValue(cls);
    }

    private <T> T getValue(Class<T> cls) {
        if (Integer.class.isAssignableFrom(cls) || Long.class.isAssignableFrom(cls)) {
            return cls.cast(200);
        } else if (String.class.isAssignableFrom(cls)) {
            return cls.cast(ONE);
        } else if (Boolean.class.isAssignableFrom(cls)) {
            return cls.cast(true);
        }
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
        return getValue(cls);
    }

    @Override
    public Map<String, String> getFilters(boolean all) {
        return getSettingsMap();
    }

    @Override
    public QueryResult query(Query query) {
        return new QueryResult(){
            {
                setCount(1);
                setStatus(QueryStatus.SUCCESS);
                Data data = new Data(){
                    {
                        setColumnNames(Collections.singletonList("test"));
                        Row row = new Row(){
                            {
                                getValues().add("test-data");
                            }
                        };
                        getRows().add(row);
                    }
                };
                setData(data);
            }
        };
    }
}
