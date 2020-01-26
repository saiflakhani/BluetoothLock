package com.quicsolv.bluetoothlock.api;

public interface AsyncResponse {
    void processFinish(String output);
    void processFailed(String output);
    void tokenApiFinish(String output);
    void tokenApiFailed(String output);
}