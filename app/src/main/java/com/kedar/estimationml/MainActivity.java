package com.kedar.estimationml;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;

import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity  {

    EditText trainingSet, testCase;
    Spinner power;
    TextView estimatedValue, hypo;
    Button estimate, plot;
    boolean flag;
    public static SimpleMatrix X1, X, Xt, XtX, estimatedVal, hypothesis, y, testC, inv_XtX, powerTestC;
    public static String hX;
    public static int row;
    public static int col;
    public static int powerRow = 0;
    int[] polyTerm;
    int[][] polyStructure;
    boolean plotPermission = false;
    public static boolean splash = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(splash) {
            Intent intent = new Intent(MainActivity.this, SplashActivity.class);
            startActivity(intent);
            finish();
        }
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

        trainingSet = findViewById(R.id.trainingSet);
        power = findViewById(R.id.power);
        testCase = findViewById(R.id.testCase);
        estimate = findViewById(R.id.estimate);
        estimatedValue = findViewById(R.id.estimatedValue);
        hypo = findViewById(R.id.hypothesis);
        plot = findViewById(R.id.plot);

        estimate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = true;
                row = getDataRow();
                if(!flag || row == 0)
                {
                    Toast.makeText(MainActivity.this, "Error!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                col = getDataCol();
                if(!flag || col == 0)
                {
                    Toast.makeText(MainActivity.this, "Error!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                X1 = new SimpleMatrix(getTrainSet(row, col));
                if(!flag)
                {
                    Toast.makeText(MainActivity.this, "Error!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                int pow = Integer.parseInt(power.getSelectedItem().toString());
                X = new SimpleMatrix(getPowerSet(X1, pow));
                if(!flag)
                {
                    Toast.makeText(MainActivity.this, "Error!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                y = new SimpleMatrix(getTrainResult(row));
                if(!flag)
                {
                    Toast.makeText(MainActivity.this, "Error!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Xt = X.transpose();
                XtX = Xt.mult(X);
                inv_XtX = XtX.pseudoInverse();
                hypothesis = inv_XtX.mult(Xt).mult(y);
                testC = new SimpleMatrix(getTestCase(col));
                if(!flag)
                {
                    Toast.makeText(MainActivity.this, "Error!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                powerTestC = new SimpleMatrix(getPowerTestSet(testC));
                estimatedVal = powerTestC.mult(hypothesis);
                getHypothesisString(hypothesis);
                if(!testCase.getText().toString().isEmpty())
                    estimatedValue.setText("Estimated Value : "+String.valueOf(estimatedVal.get(0,0)));
                plotPermission = true;
            }
        });

        plot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(col==1 && plotPermission) {
                    Intent intent = new Intent(MainActivity.this, GraphActivity.class);
                    startActivity(intent);
                }
                else
                    Toast.makeText(MainActivity.this, "This option is only available if there is only one parameter in the given training set and the hypothesis is not empty.", Toast.LENGTH_LONG).show();
            }
        });

    }

    private double[][] getPowerTestSet(SimpleMatrix testC) {
        double[][] result;
            result = new double[1][powerRow];
            result[0][0] = 1;
            for(int i = 1; i < powerRow; i++)
            {
                double r = 1;
                for(int j = 0; j < col; j++)
                {
                    r = r * Math.pow(testC.get(j), polyStructure[i][j]);
                }
                result[0][i] = r;
            }
        return result;
    }

    private void getHypothesisString(SimpleMatrix hypothesis) {
        hX = "Hypothesis : \n";
        if(abs(hypothesis.get(0)) > 0.00001)
            hX = hX +hypothesis.get(0)+"\n";
        for(int i = 1; i < hypothesis.numRows(); i++) {
            if(abs(hypothesis.get(i)) > 0.00001)
            {
                hX = hX + "+ " +hypothesis.get(i);
                for (int j = 0; j < col; j++)
                    if (polyStructure[i][j] > 0)
                    {
                        hX = hX + " * X"+j+ "^" + polyStructure[i][j];
                    }
                hX = hX + "\n";
            }
        }
        hypo.setText(hX);

    }

    double[][] getPowerSet(SimpleMatrix matrix, int pow)
    {
        double[][] powerSet;
        powerRow = 0;
        int no_of_variables = col;
        int max_power = pow;
        getPowerRow(0,max_power,0,no_of_variables);
        powerRow++;
        polyTerm = new int[no_of_variables];
        int index = no_of_variables-1;
        polyStructure = new int[powerRow][no_of_variables];
        for(int i = 1; i < powerRow; i++)
        {
            if(polyTerm[index] < max_power && degreeSum(index) < max_power)
                polyTerm[index]++;
            else
            {
                index--;
                while(degreeSum(index) >= max_power)
                    index--;
                polyTerm[index]++;
                index++;
                while(index < no_of_variables)
                {
                    polyTerm[index] = 0;
                    index++;
                }
            }
            index = no_of_variables-1;
            for(int k = 0; k < no_of_variables; k++)
                polyStructure[i][k] = polyTerm[k];
        }
        powerSet = new double[matrix.numRows()][powerRow];
        for(int i = 0; i < matrix.numRows(); i++)
        {
            for(int j = 0; j < powerRow; j++)
            {
                double result = 1;
                for(int k = 0; k < col; k++)
                {
                    result = result*Math.pow(matrix.get(i, k),polyStructure[j][k]);
                }
                powerSet[i][j] = result;
            }
        }
        return powerSet;
    }

    public int degreeSum(int index)
    {
        int result = 0;
        for(int i = 0; i <= index; i++)
            result = result + polyTerm[i];
        return result;
    }

    public void getPowerRow(int current_power,int total_power,int current_variable,int no_of_variables){
        if(current_variable < no_of_variables)
        {
            for(int pow = 0; pow+current_power <= total_power; pow++)
            {
                getPowerRow(pow+current_power, total_power, current_variable+1, no_of_variables);
                powerRow++;
            }
            powerRow--;
        }
    }

    private double[][] getTrainResult(int row) {
        double[][] result = new double[row][1];
        String data = trainingSet.getText().toString().trim();
        int r=0, c=0;
        String s="";
        try {
            for (int index = 0; index < data.length() && r < row; index++) {
                Log.d("getTrainingSet", "" + data.charAt(index) + " Row : " + r + " Col : " + c);
                if (data.charAt(index) == ',') {
                    s = "";
                } else if (data.charAt(index) == ';') {
                    result[r][c] = Double.parseDouble(s);
                    s = "";
                    r++;
                } else
                    s = s + data.charAt(index);
            }
        }
        catch (NumberFormatException e)
        {
            flag = false;
            return result;
        }
        return result;
    }

    private double[][] getTestCase(int col)
    {
        double[][] result = new double[1][col];
        String data = testCase.getText().toString().trim();
        int c=0;
        String s="";
        try {
            for (int i = 0; i < data.length(); i++) {
                if (data.charAt(i) == ',' || data.charAt(i) == ';') {
                    result[0][c] = Double.parseDouble(s);
                    c++;
                    s = "";
                } else
                    s = s + data.charAt(i);
            }
        }
        catch (NumberFormatException e)
        {
            flag = false;
            return result;
        }
        return result;
    }

    private double[][] getTrainSet(int row, int col) {
        double[][] result = new double[row][col];
        String data = trainingSet.getText().toString().trim();
        int r=0, c=0;
        String s="";
        for (int index = 0; index < data.length() && r < row; index++) {
            if (c == col && data.charAt(index) != ';')
                continue;
            else if (data.charAt(index) == ',') {
                try {
                    result[r][c] = Double.parseDouble(s);
                }
                catch (NumberFormatException e)
                {
                    flag = false;
                    return result;
                }
                s = "";
                c++;
            } else if (data.charAt(index) == ';') {
                s = "";
                r++;
                c = 0;
            } else
                s = s + data.charAt(index);
        }
        return result;
    }

    private int  getDataRow() {
        int count = 0;
        if (trainingSet.getText().toString().isEmpty()) {
            Toast.makeText(this, "Training Set is empty!!", Toast.LENGTH_SHORT).show();
            flag = false;
            return 0;
        }
        String data = trainingSet.getText().toString().trim();
        for (int i = 0; i < data.length(); i++)
            if (data.charAt(i) == ';')
                count++;

        return count;

    }

    private int getDataCol() {
        int count = 0;
        if (trainingSet.getText().toString().isEmpty()) {
            Toast.makeText(this, "Training Set is empty!!", Toast.LENGTH_SHORT).show();
            flag = false;
            return 0;
        }
        String data = trainingSet.getText().toString().trim();
        for (int i = 0; i < data.length(); i++) {
            if (data.charAt(i) == ',')
                count++;
            if (data.charAt(i) == ';')
                break;
        }
        return count;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.sample:
                trainingSet.setText(R.string.sample);
                return true;

            case R.id.manual:
                Intent intent = new Intent(MainActivity.this, ApplicationManual.class);
                startActivity(intent);
                return true;

            case R.id.developer:
                Intent intent1 = new Intent(MainActivity.this, DeveloperActivity.class);
                startActivity(intent1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
