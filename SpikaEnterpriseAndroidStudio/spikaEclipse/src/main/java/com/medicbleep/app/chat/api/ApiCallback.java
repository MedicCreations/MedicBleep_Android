package com.medicbleep.app.chat.api;

import com.medicbleep.app.chat.models.Result;

public interface ApiCallback<T> {
    /**
     * Triggered AFTER API call has finished. Provides a parameter which
     * gives more context on the result of the API call.
     * @param result carries result provided by the API call.
     */
    void onApiResponse(Result<T> result);
}
