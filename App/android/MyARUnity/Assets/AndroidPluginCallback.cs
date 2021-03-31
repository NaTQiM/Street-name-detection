using System.Collections;
using System.Collections.Generic;
using UnityEngine;
class AndroidPluginCallback : AndroidJavaProxy
{
    public AndroidPluginCallback() : base("com.streetdetect.testlibrary.DetectionListener") { }

    public void onSuccess(string response)
    {
        Debug.Log("ENTER callback onSuccess: " + response);
    }
    public void onFailure(string error)
    {
        Debug.Log("ENTER callback onError: " + error);
    }
}