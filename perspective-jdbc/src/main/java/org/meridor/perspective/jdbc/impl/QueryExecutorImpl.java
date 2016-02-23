package org.meridor.perspective.jdbc.impl;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.meridor.perspective.client.Perspective;
import org.meridor.perspective.jdbc.QueryExecutor;
import org.meridor.perspective.jdbc.UrlInfo;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryResult;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.List;

public class QueryExecutorImpl implements QueryExecutor {
    
    private final Client client;

    public QueryExecutorImpl(UrlInfo urlInfo) {
        this.client = createClient(urlInfo);
    }

    @Override
    public List<QueryResult> execute(List<Query> queries) {
        GenericType<ArrayList<QueryResult>> queryResultListType = new GenericType<ArrayList<QueryResult>>() {};
        return Perspective.query(client).postXmlAs(queries, queryResultListType);
    }
    
    private Client createClient(UrlInfo urlInfo) {
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder()
                .nonPreemptive()
                .credentials(urlInfo.getUserName(), urlInfo.getPassword())
                .build();

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(feature);

        return ClientBuilder.newClient(clientConfig);
    }

}
