package com.example.chogba.yolo;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.SurfaceView;


public class ColorBlobDetectionActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG = "OCVSample::Activity";

    private boolean mIsColorSelected = false,objectFixed=false;
    private Mat mRgba;
    private Mat mRgbaT;
    private Mat mRgbaF;
    private Scalar mBlobColorRgba;
    private Scalar mBlobColorHsv;
    private ColorBlobDetector mDetector;
    private Mat mSpectrum;
    private Size SPECTRUM_SIZE;
    private Scalar CONTOUR_COLOR;
    private Rect grect=new Rect();
    public Robot robot;

    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ColorBlobDetectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */

    static{
        if(!OpenCVLoader.initDebug()){
            Log.i(TAG,"hehehehehe");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_detection_surface_view);
//        if(!OpenCVLoader.initDebug()){
//            Log.i(TAG,"hehehehehe");
//        }
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        robot=new Robot();
    }

    @Override
    public void onPause()
    {
        robot.noMove();
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();
        //HOGDescriptor h = new HOGDescriptor();
        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;
        Log.i(TAG, "Original image coordinates: (" + (int)event.getX() + ", " + (int)event.getY() + ")");
        Log.i(TAG, "rows and cols image coordinates: (" + rows + ", " + cols + ")");
        Log.i(TAG, "offsets image coordinates: (" + xOffset + ", " + yOffset + ")");

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        if (mIsColorSelected && objectFixed) {
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();
            Log.e(TAG, "Contours count: " + contours.size());

            for(int i=0;i<contours.size();i++) {
                Rect detectedRect = Imgproc.boundingRect(contours.get(i));
                Rect intersection = new Rect();
                double intersectionArea = 0;
                intersection.x = (detectedRect.x < grect.x) ? grect.x : detectedRect.x;
                intersection.y = (detectedRect.y < grect.y) ? grect.y : detectedRect.y;
                intersection.width = (detectedRect.x + detectedRect.width < grect.x + grect.width) ?
                        detectedRect.x + detectedRect.width : grect.x + grect.width;
                intersection.width -= intersection.x;
                intersection.height = (detectedRect.y + detectedRect.height < grect.y + grect.height) ?
                        detectedRect.y + detectedRect.height : grect.y + grect.height;
                intersection.height -= intersection.y;

                if ((intersection.width <= 0) || (intersection.height <= 0)) {
                    intersectionArea = 0;
                } else {
                    intersectionArea = intersection.area();
                }
                double unionArea = grect.area() + detectedRect.area() - intersectionArea;
                double IOU = intersectionArea / unionArea;
                if (IOU > 0.3) {
                    List<MatOfPoint> tc = new ArrayList<MatOfPoint>();
                    tc.add(contours.get(i));
                    //Imgproc.drawContours(mRgba, tc, -1, CONTOUR_COLOR);
                    Point pt1=new Point(detectedRect.x,detectedRect.y);
                    Point pt2=new Point(detectedRect.x+detectedRect.width,detectedRect.y+detectedRect.height);
                    Imgproc.rectangle(mRgba,pt1,pt2,CONTOUR_COLOR);

                    Mat colorLabel = mRgba.submat(4, 68, 4, 68);
                    colorLabel.setTo(mBlobColorRgba);

                    Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
                    mSpectrum.copyTo(spectrumLabel);
                    grect = Imgproc.boundingRect(contours.get(i));
                    Log.i(TAG,"grect is " + grect.tl().x + " " + grect.tl().y);
                    Log.i(TAG,"intersection area is " + intersectionArea);
                }
            }

        }

        if (mIsColorSelected && !objectFixed) {
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();
            Log.e(TAG, "Contours count: " + contours.size());
            //Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);

            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
            mSpectrum.copyTo(spectrumLabel);

            if(contours.size()==0){
                grect=null;
                objectFixed=false;
                robot.trackObject(null);
                Core.transpose(mRgba, mRgbaT);
                Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0,0);
                Core.flip(mRgbaF, mRgba, 1 );

                return mRgba;
            }

            grect=Imgproc.boundingRect(contours.get(0));

            Point pt1=new Point(grect.x,grect.y);
            Point pt2=new Point(grect.x+grect.width,grect.y+grect.height);
            Imgproc.rectangle(mRgba,pt1,pt2,CONTOUR_COLOR);
            if(robot.getOriginalRectangle()==null){
                robot.setOriginalRectangle(grect);
            }

            robot.trackObject(grect);

            //objectFixed=true;
        }


        Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);
        Core.flip(mRgbaF, mRgba, 1 );

        return mRgba;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

}