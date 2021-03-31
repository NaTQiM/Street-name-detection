using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class MainController : MonoBehaviour
{
    // Start is called before the first frame update
    public RawImage image;

    public static MainController Instance;
    private void Awake()
    {
        Instance = this;
    }

    void Start()
    {
        Texture2D texture2D = TextureToTexture2D(image.texture);
        Debug.Log(" >>> " + texture2D.format);
        byte[] image_byte = texture2D.GetRawTextureData();
        Debug.Log(" >>> " + image_byte);

       ProcessImage(image_byte, texture2D.width, texture2D.height);
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    public void ProcessImage(byte [] image, int width, int height)
    {

#if UNITY_ANDROID && !UNITY_EDITOR
        AndroidJavaObject pluginClass = new AndroidJavaObject("com.streetdetect.testlibrary.MyStreetNameSignDetection");
        pluginClass.CallStatic("ProcessImage", image, width, height, new AndroidPluginCallback());
#endif
    }

    public static Texture2D TextureToTexture2D(Texture texture)
    {
        Texture2D texture2D = new Texture2D(texture.width, texture.height, TextureFormat.RGBA32, false);
        RenderTexture currentRT = RenderTexture.active;
        RenderTexture renderTexture = RenderTexture.GetTemporary(texture.width, texture.height, 32);
        Graphics.Blit(texture, renderTexture);

        RenderTexture.active = renderTexture;
        texture2D.ReadPixels(new Rect(0, 0, renderTexture.width, renderTexture.height), 0, 0);
        texture2D.Apply();

        RenderTexture.active = currentRT;
        RenderTexture.ReleaseTemporary(renderTexture);

        return texture2D;
    }

}
