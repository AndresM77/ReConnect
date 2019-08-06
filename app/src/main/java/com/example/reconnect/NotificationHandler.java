package com.example.reconnect;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

public class NotificationHandler {

    public static FirebaseFunctions mFunctions;

    public NotificationHandler(FirebaseFunctions functions) {
        mFunctions = functions;
    }

    public static Task<String> sendNotifications(String token, String text) {
        Map<String,Object> data = new HashMap<>();
        data.put("text", text);
        data.put("token", token);
        data.put("push", true);

        return mFunctions.getHttpsCallable("sendMessage")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        return (String) task.getResult().getData();
                    }
                });
    }
}
