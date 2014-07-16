package com.clover.spika.enterprise.chat.api;

import com.clover.spika.enterprise.chat.models.Result;

public interface ApiCallback<T> {
    /**
     * Triggered AFTER API call has finished. Provides a parameter which
     * gives more context on the result of the API call.
     * @param result carries result provided by the API call.
     */
    void onApiResponse(Result<T> result);
}
