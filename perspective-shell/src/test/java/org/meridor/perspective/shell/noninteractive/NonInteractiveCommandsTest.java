package org.meridor.perspective.shell.noninteractive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.*;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.meridor.perspective.api.ObjectMapperFactory;
import org.meridor.perspective.shell.common.repository.ApiProvider;
import org.meridor.perspective.shell.noninteractive.NonInteractiveMain;
import org.meridor.perspective.sql.Data;
import org.meridor.perspective.sql.QueryResult;
import org.meridor.perspective.sql.QueryStatus;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@RunWith(Parameterized.class)
public class NonInteractiveCommandsTest {

    @Parameterized.Parameters(name = "call with args {0} should return 0")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {Collections.singletonList("-v"), false, null},
                {Collections.singletonList("-h"), false, null},
                {Arrays.asList("-q", "\'select * from flavors;\'"), true, new ArrayList<QueryResult>(){
                    {
                        add(new QueryResult(){
                            {
                                setCount(0);
                                setStatus(QueryStatus.SUCCESS);
                                setData(new Data(){
                                    {
                                        setColumnNames(Collections.emptyList());
                                        setRows(Collections.emptyList());
                                    }
                                });
                            }
                        });
                    }
                }},
        });
    }

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    
    private String[] args;

    private final boolean needsServer;
    
    private final MockResponse response;
    
    private MockWebServer server;
    
    public NonInteractiveCommandsTest(List<String> args, boolean needsServer, Serializable object) throws Exception {
        this.args = args.toArray(new String[args.size()]);
        this.needsServer = needsServer;
        this.response = needsServer ? responseFromObject(object) : null;
    }

    private MockResponse responseFromObject(Serializable object) throws JsonProcessingException {
        ObjectWriter ow = ObjectMapperFactory.createDefaultMapper()
                .writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(object);
        MockResponse mockResponse = new MockResponse();
        mockResponse.setBody(json);
        mockResponse.setResponseCode(200);
        mockResponse.setHeader("Content-Type", "application/json");
        mockResponse.setHeader("Content-Length", json.length());
        return mockResponse;
    }
    
    @Before
    public void before() throws IOException {
        if (needsServer) {
            server = new MockWebServer();
            server.enqueue(response);
            server.start();
            String serverUrl = server.url("/").url().toString();
            System.setProperty(ApiProvider.API_SYSTEM_PROPERTY, serverUrl);
        }
    }
    
    @Test
    public void testCommand() {
        exit.expectSystemExitWithStatus(0);
        new NonInteractiveMain(args).start();
    }
    
    @After
    public void after() throws IOException {
        if (needsServer && server != null) {
            server.shutdown();
        }
    }
}