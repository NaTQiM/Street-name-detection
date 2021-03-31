package com.streetdetect.testlibrary;

public interface DetectionListener {
    void onSuccess(String response);
    void onFailure(String error);
}