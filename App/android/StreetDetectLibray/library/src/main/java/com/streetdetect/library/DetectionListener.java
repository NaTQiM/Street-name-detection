package com.streetdetect.library;

public interface DetectionListener {
    void onSuccess(String response);
    void onFailure(String error);
}