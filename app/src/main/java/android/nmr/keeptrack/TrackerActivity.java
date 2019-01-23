package android.nmr.keeptrack;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;


public class TrackerActivity extends AppCompatActivity {

    protected CountDownTimer mTimer;
    protected Button mStart;
    protected Button mStop;
    protected TextView mTimerText;
    protected TextView mTextOutput;

    protected Button m1Button;
    protected Button m5Button;
    protected Button m10Button;
    protected Button m30Button;
    protected Button mClearButton;

    protected Date mDateStarted;

    private long mInitialTimeLength;
    private long mTimeLeft;
    private boolean mTimerRunning;
    private static final String TAG = "TrackerActivity";

    private ArrayList<String> mTimerNames = new ArrayList<>();
    private ArrayList<String> mTimerElapsed = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);
        Log.d(TAG, "onCreate: started");

        mInitialTimeLength = 0;

        mTimerText = (TextView) findViewById(R.id.timerText);
        //mTextOutput = (TextView) findViewById(R.id.outPutView);
        //mTimer = new Timer(10, mTimerText);

        m1Button = (Button)findViewById(R.id.add1Button);
        m5Button = (Button)findViewById(R.id.add5Button);
        m10Button = (Button)findViewById(R.id.add10Button);
        m30Button = (Button)findViewById(R.id.add30Button);
        mClearButton = (Button)findViewById(R.id.clearButton);

        mStart = (Button) findViewById(R.id.startButton);
        mStart.setText(R.string.start);

        mStop = (Button)findViewById(R.id.stopButton);
        mStop.setVisibility(View.INVISIBLE); //Reset Button Invisible on start

        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(mTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
            }
        });

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTimer();
            }
        });

        m1Button.setOnClickListener(new View.OnClickListener() { //Adds 1 minute to timer.
            @Override
            public void onClick(View view) {
                addTime(1);
            }
        });

        m5Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTime(5);
            }
        });

        m10Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTime(10);
            }
        });

        m30Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTime(30);
            }
        });

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTimer();
            }
        });

        // Clears Timer
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInitialTimeLength = 0;
                updateCountText();
            }
        });


        initTimerData();
        updateCountText();
    }

    //BEGINS TIMER
    private void startTimer() {
        if(mInitialTimeLength <= 0) { //Timer won't run if no time set.
            Toast.makeText(this, "No Time Set!", Toast.LENGTH_SHORT).show();
        } else {
            mTimer = new CountDownTimer(mInitialTimeLength, 1000) {
                @Override
                public void onTick(long timeUntilFinished) {
                    System.out.println("TICKED!" + timeUntilFinished + ": TIMESET: " + mInitialTimeLength);
                    mTimeLeft = timeUntilFinished;
                    updateCountText();
                }

                @Override
                public void onFinish() { //Ends Timer Set Start Invisible. Reset Visible. WHAT DOES THIS MEAN?
                    System.out.println("FINISHED: " + mInitialTimeLength );
                    mTimerRunning = false;
                    mStart.setEnabled(false);
                    mStop.setVisibility(View.VISIBLE);
                    //RECORD TIMER
                    Pair<String, String> newRecord = new Pair<>
                            (dateRetriever(), millisToString(mInitialTimeLength)); //Saves Data+Inital Set Time as a key-value pair.
                    //Inject fucking date.
//                    mTimerNames.add(newRecord.first);
//                    mTimerElapsed.add(newRecord.second);
//                    initRecyclerView();
                    saveTimerRecord(newRecord.first, newRecord.second);
                    loadTimerRecord();
                }
                //recordsDate
            };

            mTimer.start(); //Timer starts
            mTimerRunning = true;
            swapStartText();
            mStop.setVisibility(View.INVISIBLE);
        }
    }

    //PAUSES TIMER
    private void pauseTimer() {
        mTimer.cancel();
        mTimerRunning = false;
        swapStartText();
        mStop.setVisibility(View.VISIBLE);
    }


    private void resetTimer() {
        mTimeLeft = mInitialTimeLength;
        updateCountText();
        mStop.setVisibility(View.INVISIBLE);
        mStart.setEnabled(true);
        swapStartText();
        mStart.setVisibility(View.VISIBLE);
    }

    //Handles Ticking of Timer (for OnTick method)
    private void updateCountText() {
        if(mTimerRunning == false) {
            mTimerText.setText(millisToString(mInitialTimeLength));//(Long.toString(mInitialTimeLength));
            System.out.println("mINTIIAL UPDATE TRIGGER");
        } else {
            mTimerText.setText(millisToString(mTimeLeft));
            System.out.println("mLEFT UPDATE TRIGGER");
        }
        System.out.println("mLeft" + mTimeLeft + "mInitial" + mInitialTimeLength);
    }

    //BUTTON METHODS
    public void addTime(long minutesAdded) {
        mInitialTimeLength += (minutesAdded * 60000); //formats to milliseconds
        updateCountText();
        Log.d(TAG, "addTime: " + mInitialTimeLength);
    }

    private void swapStartText() { //Switches START button text from pause to start.
        if(mStart.getText() == getResources().getString(R.string.pause)) {
            mStart.setText(getResources().getString(R.string.start));
        } else {
            mStart.setText(getResources().getString(R.string.pause));
        }
    }


    //Converts Milliseconds to minutes/seconds
    private String millisToString(long time) {
        int minutes = (int) (time / 1000) / 60; //millsecs to secs to minutes
        int seconds = (int) (time / 1000) % 60; //all that are left is seconds.

        String clockFace = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        System.out.println(clockFace);
        return clockFace;
    }

    //Gets current date to record when timer started.
    public String dateRetriever(){
        Date current = Calendar.getInstance().getTime();
        Log.d(TAG, "dateRetriever: Current Time:" + current);

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy mm:ss");
        return df.format(current);
    }

    //Save Single Record (on timer end)
//    private void saveTimerRecord(Pair<String, String> recordSave) {
//        FileOutputStream fos = null;
//        try{
//            fos = openFileOutput("ATIMER" , MODE_PRIVATE);
//            //fos.write(recordSave.first + " " + recordSave.second);
//
//            Toast.makeText(this, "SAVEDTO: " + getFilesDir(), Toast.LENGTH_SHORT).show();
//        } catch(Exception e) {
//            e.printStackTrace();
//        } finally {
//            if(fos != null) {
//                try {
//                    fos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    public void saveTimerRecord(String dateString, String timeString) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(dateString, timeString);
        editor.apply();
        System.out.println("editor: " + editor); //retrieve date from savedTimer. find how to see it.
    }

    public void loadTimerRecord() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        mTimerNames.add(getResources().getString(R.string.preference_file_key));
        mTimerElapsed.add(getResources().getString(R.string.preference_file_key));
    }

//    //Retrieves full records of Timers.
//    private void loadTimerRecords() {
//        FileInputStream fis = null;
//        try {
//            fis = openFileInput("ATIMER");
//            InputStreamReader isr = new InputStreamReader(fis);
//            BufferedReader br = new BufferedReader(isr);
//            StringBuilder sb = new StringBuilder();
//            String text;
//
//            while((text = br.readLine()) != null) {
//                sb.append(text).append("\n");
//            }
//            Toast.makeText(this, "NAH", Toast.LENGTH_SHORT).show();
//            mTextOutput.setText(sb.toString()); //prints record.
//            Toast.makeText(this, "TODD HOWARD: " + getFilesDir(), Toast.LENGTH_SHORT).show();
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if(fis != null) {
//                try {
//                    fis.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    //RECYCLER METHOD: starts recyclerview
    private void initRecyclerView() {
        Log.d(TAG, "initTimerNames: init recyc view");
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mTimerElapsed, mTimerNames, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    //sets up timer data/
    private void initTimerData() {
        Log.d(TAG, "initTimerNames: preparing timer names");

        mTimerNames.add("2:00am");
        mTimerElapsed.add("0:12");

        mTimerNames.add("21:00am");
        mTimerElapsed.add("0:13");

        mTimerNames.add("23:00am");
        mTimerElapsed.add("0:14");

        initRecyclerView();
    }






    //
//    public void startPressed(View v) {
//        mTimer.getTimer().start();
//    }
//
//    public void stopPressed(View v) { //Stops ticker if time elapsed is over one.
//        //resetPressed(v);
//    }
//
//    public void pausePressed(View v) { //Pauses ticker
//        mTimer.getTimer().pause();
//    }
//
//
//    public void swapButton(View v) { //Changes the Startbutton on press
//      if(mStart.getText().equals(getResources().getString(R.string.start))) {
//          System.out.println("STARTPRESSED");
//          mStart.setText(R.string.stop);
//          System.out.println("STOPIS: " + mStart);
//          startPressed(v);
//      } else {
//          System.out.println("STOPPRESSED");
//          mStart.setText(R.string.start);
//          System.out.println("STARTIS: " + mStart);
//          pausePressed(v);
//      }
//    }
//
////    public void resetPressed(View v) {
////        mTimer.resetTime();
////        mTimerText.setText(mTimer.getTimeElapsed());
////    }

}
