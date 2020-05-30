package com.kedar.estimationml;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;

import org.ejml.simple.SimpleMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

public class GraphActivity extends AppCompatActivity {

    CombinedChart combinedChart;
    SimpleMatrix hypothesis, X1, y1;
    ArrayList<Entry> entries, trainSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        combinedChart = findViewById(R.id.lineChart);
        hypothesis = MainActivity.hypothesis;
        X1 = MainActivity.X1;
        y1 = MainActivity.y;
        entries = new ArrayList<>();
        trainSet = new ArrayList<>();
        float startPoint = Float.parseFloat(String.valueOf(X1.elementMinAbs()-1));
        float endPoint = Float.parseFloat(String.valueOf(X1.elementMaxAbs()+1));
        float increment = (endPoint-startPoint)/500;
        float x = startPoint, y, term;
        for(int i = 0; i < 500; i++)
        {
            term = 1;
            y = 0;
            for(int j = 0; j < hypothesis.numRows(); j++)
            {
                y = y + Float.parseFloat(String.valueOf(hypothesis.get(j,0)))*term;
                term = term*x;
            }
            entries.add(new Entry(x, y));
            x = x + increment;
        }
        for(int i = 0; i < y1.numRows(); i++)
        {
            x = Float.parseFloat(String.valueOf(X1.get(i,0)));
            y = Float.parseFloat(String.valueOf(y1.get(i,0)));
            trainSet.add(new Entry(x, y));
        }

        combinedChart.setDrawOrder(new CombinedChart.DrawOrder[] {CombinedChart.DrawOrder.LINE, CombinedChart.DrawOrder.SCATTER});

        LineDataSet lineDataSet = new LineDataSet(entries,"Estimated Hypothesis : h(X)");
        lineDataSet.setDrawCircles(false);
        lineDataSet.setColor(Color.RED);

        ScatterDataSet scatterDataSet = new ScatterDataSet(trainSet, "Training Examples");
        scatterDataSet.setColor(Color.GREEN);

        CombinedData combinedData = new CombinedData();

        combinedData.setData(new LineData(lineDataSet));
        combinedData.setData(new ScatterData(scatterDataSet));

        combinedChart.setData(combinedData);
        combinedChart.setVisibleXRangeMaximum(65f);
        combinedChart.setDrawGridBackground(false);
        combinedChart.setPinchZoom(true);

        combinedChart.invalidate();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.plot_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.save:
                Bitmap bitmap = Bitmap.createBitmap(combinedChart.getWidth(), combinedChart.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                Drawable bgDrawable = combinedChart.getBackground();
                if (bgDrawable!=null) {
                    bgDrawable.draw(canvas);
                }
                else
                {
                    canvas.drawColor(Color.WHITE);
                }
                combinedChart.draw(canvas);
                if(bitmap != null) {
                    String root = Environment.getExternalStorageDirectory().toString();
                    File imgDir = new File(root + "/EstimationML");
                    imgDir.mkdir();
                    Random gen = new Random();
                    int num = 100000;
                    num = gen.nextInt(num);
                    String fname = "my_graph_" + num + ".jpg";
                    File img = new File(imgDir, fname);
                    if (img.exists())
                        img.delete();
                    try {
                        FileOutputStream out = new FileOutputStream(img);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                        Toast.makeText(GraphActivity.this, "Image saved successfully. FilePath:" + img.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        if(ContextCompat.checkSelfPermission(GraphActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                            ActivityCompat.requestPermissions(GraphActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                        Toast.makeText(this, "Error!!", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
                return true;
            case R.id.share:
                Bitmap bitmap1 = Bitmap.createBitmap(combinedChart.getWidth(), combinedChart.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas1 = new Canvas(bitmap1);
                Drawable bgDrawable1 =combinedChart.getBackground();
                if (bgDrawable1!=null) {
                    bgDrawable1.draw(canvas1);
                }
                else
                {
                    canvas1.drawColor(Color.WHITE);
                }
                combinedChart.draw(canvas1);
                if(bitmap1 != null) {
                    String root = Environment.getExternalStorageDirectory().toString();
                    File imgDir = new File(root + "/EstimationML");
                    imgDir.mkdir();
                    Random gen = new Random();
                    int num = 100000;
                    num = gen.nextInt(num);
                    String fname = "my_graph_" + num + ".jpg";
                    File img = new File(imgDir, fname);
                    if (img.exists())
                        img.delete();
                    try {
                        FileOutputStream out = new FileOutputStream(img);
                        bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                        Toast.makeText(GraphActivity.this, "Image saved successfully. FilePath:" + img.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(img.getAbsolutePath()));
                        intent.setType("image/*");
                        startActivity(Intent.createChooser(intent, "Share Image"));
                    } catch (Exception e) {
                        if(ContextCompat.checkSelfPermission(GraphActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                            ActivityCompat.requestPermissions(GraphActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                        Toast.makeText(this, "Error!!", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
