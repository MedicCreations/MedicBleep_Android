package com.clover.spika.enterprise.chat.models;

/**
 * Represents any kind of result from any API call
 *
 * @author Josip Marković
 */
public class Result<T> {

    private T resultData;
    private ApiResponseState state;

    public Result(ApiResponseState state) {
        this.state = state;
    }

    public Result(T resultData, ApiResponseState state) {
        this.resultData = resultData;
        this.state = state;
    }

    public T getResultData() {
        return resultData;
    }

    public ApiResponseState getState() {
        return state;
    }

    public void setResultData(T resultData) {
        this.resultData = resultData;
    }

    public void setState(ApiResponseState state) {
        this.state = state;
    }

    public boolean isSuccess() {
        return ApiResponseState.SUCCESS == getState();
    }

    public boolean isFailure() {
        return ApiResponseState.FAILURE == getState();
    }

    public enum ApiResponseState {
        SUCCESS, FAILURE
    }
}
