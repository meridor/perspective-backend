package org.meridor.perspective.shell.common.repository.impl;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;

public class MockCall<T> implements Call<T> {

    private final Response<T> response;

    private boolean canceled;

    public MockCall(Response<T> response) {
        this.response = response;
    }

    @Override
    public Response<T> execute() throws IOException {
        return response;
    }

    @Override
    public void enqueue(Callback<T> callback) {
        callback.onResponse(this, response);
    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public Call<T> clone() {
        return new MockCall<>(response);
    }

    @Override
    public Request request() {
        throw new UnsupportedOperationException();
    }
}
