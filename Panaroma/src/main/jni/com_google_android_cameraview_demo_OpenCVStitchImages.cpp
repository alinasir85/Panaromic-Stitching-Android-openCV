#include<com_google_android_cameraview_demo_OpenCVStitchImages.h>
#include "opencv2/stitching.hpp"
#include <stdio.h>
#include <iostream>
#include <vector>
using namespace std;
using namespace cv;
JNIEXPORT jint JNICALL Java_com_google_android_cameraview_demo_OpenCVStitchImages_stichImages
  (JNIEnv * env , jclass obj, jlongArray imgsAdrs, jlong resAdrs)
  {
        jint ret=1;
        jsize len = env->GetArrayLength(imgsAdrs);
        jlong *imgsAdrsEle = env->GetLongArrayElements(imgsAdrs,0);
        vector< Mat > images;
        for( int i = 0 ; i<len ; i++)
        {
             Mat & orgPic = *(Mat*)imgsAdrsEle[i];
             Mat temp;
             cvtColor(orgPic, temp, CV_BGRA2RGB);
            // float scale = 1000.0f / orgPic.rows;
            // resize(temp, temp, Size(scale * orgPic.rows, scale * orgPic.cols));
             resize(temp, temp, Size(800,600));

             images.push_back(temp);
         }

         Mat & res  = *(Mat*)resAdrs;
         Stitcher stitcher = Stitcher::createDefault(true);
         Stitcher::Status status =stitcher.stitch(images, res);
         if (status != Stitcher::OK)
           {

                ret=0;
           }
           else
           {
                cv::cvtColor(res, res, CV_BGR2RGBA, 4);
            }
         return ret;
  }
