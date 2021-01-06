package com.jike.cameraproplus.neuralengine;

import android.content.res.AssetManager;
import android.os.Trace;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class DenoiseEngine {
    private static final String MODEL_FILE = "file:///android_asset/denoise.pb"; //模型存放路径

    //模型中输出变量的名称
    private static final String inputName = "Placeholder:0";

    //模型中输出变量的名称
    private static final String outputName = "g_conv10/BiasAdd:0";

    TensorFlowInferenceInterface inferenceInterface;

    static {
        System.loadLibrary("tensorflow_demo");
    }

    public DenoiseEngine(AssetManager assetManager) {
        //接口定义
        inferenceInterface = new TensorFlowInferenceInterface(assetManager,MODEL_FILE);
    }

    public float[] process(float[] inputs, int w, int h) {
        float[] outputs = new float[w*h];
        // 1、多行、一行、rgb
        /*float[][][][] input = new float[1][w][h][3];

        int colIndex = 0;
        int rowIndex = 0;
        for(int i = 0; i < inputs.length; i+=3){
            float r = inputs[i];
            float g = inputs[i+1];
            float b = inputs[i+2];
            input[0][rowIndex][colIndex][0] = r;
            input[0][rowIndex][colIndex][1] = g;
            input[0][rowIndex][colIndex][2] = b;

            colIndex++;
            if(colIndex > w){
                colIndex = 0;
                rowIndex ++;
            }
        }*/

        //将数据feed给tensorflow
        Trace.beginSection("feed");
        inferenceInterface.feed(inputName, inputs, w, h);
        Trace.endSection();

        //运行降噪的操作
        Trace.beginSection("run");
        String[] outputNames = new String[] {outputName};
        inferenceInterface.run(outputNames);
        Trace.endSection();

        //将输出存放到outputs中
        Trace.beginSection("fetch");
        inferenceInterface.fetch(outputName, outputs);
        Trace.endSection();

        return outputs;
    }


}
