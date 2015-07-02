package com.medicbleep.app.chat.api.robospice;

import com.medicbleep.app.chat.models.GetBackroundDataResponse;
import com.medicbleep.app.chat.services.robospice.CustomSpiceRequest;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

public class BackgroundDataChatSpice {


    public static class GetMessages extends CustomSpiceRequest<GetBackroundDataResponse> {

        private String chatId;
        private String msgId;
        private int isChatActive;

        public GetMessages(String chatId, String msgId, boolean isChatActive) {
            super(GetBackroundDataResponse.class);

            this.chatId = chatId;
            this.msgId = msgId;
            this.isChatActive = isChatActive ? 1 : 0;
        }

        @Override
        public GetBackroundDataResponse loadDataFromNetwork() throws Exception {

            String url = Const.BASE_URL + Const.F_GET_PUSH_MESSAGES + "?" + Const.CHAT_ID + "=" + chatId + "&" + Const.MESSAGE_ID + "=" + msgId
                    + "&" + Const.IS_CHAT_ACTIVE + "=" + isChatActive;

            Request.Builder requestBuilder = new Request.Builder().headers(getPostHeaders()).url(url).get();

            Call connection = getOkHttpClient().newCall(requestBuilder.build());

            Logger.custom("i", "LOG", url);

            Response res = connection.execute();
            ResponseBody resBody = res.body();
            String responseBody = resBody.string();

            Logger.custom("i", "LOG", responseBody);

            ObjectMapper mapper = new ObjectMapper();

            GetBackroundDataResponse result = mapper.readValue(responseBody, GetBackroundDataResponse.class);

            return result;

        }
    }

}