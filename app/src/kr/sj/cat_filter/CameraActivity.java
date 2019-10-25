package kr.sj.cat_filter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, Runnable {

    private static final String TAG = "OCVSampleFaceDetect";
    private CameraBridgeViewBase cameraBridgeViewBase;
    public final static int GALLERY_IMAGE_LOADED = 1001;

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");
                cameraBridgeViewBase.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    private volatile boolean running = false;
    private volatile int qtdFaces;
    private volatile Mat matTmpProcessingFace;

    private CascadeClassifier cascadeClassifier;
    private File mCascadeFile;
    private TextView infoFaces;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        infoFaces = findViewById(R.id.tv);
        cameraBridgeViewBase = findViewById(R.id.main_surface);
        loadHaarCascadeFile();
        checkPermissions();

        ImageView galBtn = findViewById(R.id.iv_gallery);
        galBtn.setOnClickListener(this.GalleryBtnClick);

        ArrayList<FilterData> filterList = new ArrayList<FilterData>();
        filterList.add(new FilterData(0,"필터1"));
        filterList.add(new FilterData(1,"필터2"));
        filterList.add(new FilterData(2,"필터3"));
        filterList.add(new FilterData(3,"필터4"));
        filterList.add(new FilterData(4,"필터5"));
        filterList.add(new FilterData(5,"필터6"));
        filterList.add(new FilterData(6,"필터7"));
        filterList.add(new FilterData(7,"필터8"));
        filterList.add(new FilterData(8,"필터9"));

        RecyclerView rv_filterList =  findViewById(R.id.rv_filterList);
        rv_filterList.setLayoutManager(new LinearLayoutManager(this, LinearLayout.HORIZONTAL, false));
        //    rv_filterList.setHasFixedSize(true)
        rv_filterList.setAdapter(new FilterAdapter(filterList));

        ImageView filterBtn = findViewById(R.id.iv_filter);
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout ll_filter = findViewById(R.id.ll_filter);
                if(ll_filter.getVisibility() == View.INVISIBLE)
                    ll_filter.setVisibility(View.VISIBLE);
                else
                    ll_filter.setVisibility(View.INVISIBLE);
            }
        });

    }

    private View.OnClickListener GalleryBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_IMAGE_LOADED);
        }
    };

    private void checkPermissions() {
        if (isPermissionGranted()) {
            loadCameraBridge();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    private boolean isPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkPermissions();
    }

    private void loadCameraBridge() {
        cameraBridgeViewBase.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
    }

    private void loadHaarCascadeFile() {
        try {
            File cascadeDir = getDir("haarcascade_frontalface_alt", Context.MODE_PRIVATE);
            mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");

            if (!mCascadeFile.exists()) {
                FileOutputStream os = new FileOutputStream(mCascadeFile);
                InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                is.close();
                os.close();
            }
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to load Haar Cascade file");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        disableCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isPermissionGranted()) return;
        resumeOCV();
    }

    private void resumeOCV() {
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        }
        cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        cascadeClassifier.load(mCascadeFile.getAbsolutePath());
        startFaceDetect();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (matTmpProcessingFace == null) {
            matTmpProcessingFace = inputFrame.gray();
        }
        return inputFrame.rgba();
    }


    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }


    public void startFaceDetect() {
        if (running) return;
        new Thread(this).start();
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                if (matTmpProcessingFace != null) {
                    MatOfRect matOfRect = new MatOfRect();
                    cascadeClassifier.detectMultiScale(matTmpProcessingFace, matOfRect);
                    int newQtdFaces = matOfRect.toList().size();
                    if (qtdFaces != newQtdFaces) {
                        qtdFaces = newQtdFaces;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                infoFaces.setText(String.format(getString(R.string.faces_detects), qtdFaces));
                            }
                        });
                    }
                    Thread.sleep(500);//if you want an interval
                    matTmpProcessingFace = null;
                }
                Thread.sleep(50);
            } catch (Throwable t) {
                try {
                    Thread.sleep(10_000);
                } catch (Throwable tt) {
                }
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        disableCamera();
    }

    private void disableCamera() {
        running = false;
        if (cameraBridgeViewBase != null)
            cameraBridgeViewBase.disableView();
    }

}

