package com.medicbleep.app.chat.models;

/**
 * Represents any kind of result from any API call
 */
public class Result<T> {

	private T resultData;
	private ApiResponseState state;

	public Result() {
	}

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

	/**
	 * @return true if result is marked as
	 *         {@link com.medicbleep.app.chat.models.Result.ApiResponseState#SUCCESS}
	 */
	public boolean isSuccess() {
		return ApiResponseState.SUCCESS == getState();
	}

	/**
	 * @return true if result is marked as
	 *         {@link com.medicbleep.app.chat.models.Result.ApiResponseState#FAILURE}
	 */
	public boolean isFailure() {
		return ApiResponseState.FAILURE == getState();
	}

	/**
	 * @return true if resultData has been set, false if it's null
	 */
	public boolean hasResultData() {
		return getResultData() != null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		@SuppressWarnings("rawtypes")
		Result result = (Result) o;

		if (resultData != null ? !resultData.equals(result.resultData) : result.resultData != null)
			return false;
		if (state != result.state)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = resultData != null ? resultData.hashCode() : 0;
		result = 31 * result + (state != null ? state.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Result{" + "resultData=" + resultData + ", state=" + state + '}';
	}

	public enum ApiResponseState {
		SUCCESS, FAILURE
	}
}
