package android.nmr.keeptrack;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/*
    TrackerActivity: This controller holds most of the logic for the program.
    I've divided the code into different sections, as there's a fair bit here.
 */


public class TrackerActivity extends AppCompatActivity {

    protected CountDownTimer mTimer;
    protected Button mStart;
    protected Button mStop;
    protected TextView mTimerText;

    protected Button m1Button;
    protected Button m5Button;
    protected Button m10Button;
    protected Button m30Button;
    protected Button mClearButton;
    protected Button mClearRecordsButton;

    private long mInitialTimeLength;
    private long mTimeLeft;
    private boolean mTimerRunning;
    private static final String TAG = "TrackerActivity";

    private ArrayList<String> mTimerList = new ArrayList<>(); //Holds the data for the RecyclerView


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);
        Log.d(TAG, "onCreate: started");

        mInitialTimeLength = 0;

        mTimerText = findViewById(R.id.timerText);
        //mTextOutput = (TextView) findViewById(R.id.outPutView);
        //mTimer = new Timer(10, mTimerText);

        m1Button = findViewById(R.id.add1Button);
        m5Button = findViewById(R.id.add5Button);
        m10Button = findViewById(R.id.add10Button);
        m30Button = findViewById(R.id.add30Button);
        mClearButton = findViewById(R.id.clearButton);
        mClearRecordsButton = findViewById(R.id.clearRecordsButton);

        mStart = findViewById(R.id.startButton);
        mStart.setText(R.string.start);

        mStop = findViewById(R.id.stopButton);
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


        //Initialising Buttons
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

        mClearRecordsButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               launchDialog();
           }
       });


        //Initialising RecyclerView & Countdown Functionality
        initRecyclerView();
        updateCountText();
    }

    //MAIN TIMER METHODS

    //BEGINS TIMER
    private void startTimer() {
        if(mInitialTimeLength <= 0) { //Timer won't run if no time set.
            Toast.makeText(this, "No Time Set!", Toast.LENGTH_SHORT).show();
        } else {
            mTimer = new CountDownTimer(mInitialTimeLength, 1000) {
                @Override
                public void onTick(long timeUntilFinished) {
                    mTimeLeft = timeUntilFinished;
                    updateCountText();
                }

                @Override
                public void onFinish() { //Ends Timer Set Start Invisible. Reset Visible. WHAT DOES THIS MEAN?
                    System.out.println("FINISHED: " + mInitialTimeLength );
                    timerDingDing();
                    mTimerRunning = false;
                    mStart.setEnabled(false);
                    mStop.setVisibility(View.VISIBLE);
                    //RECORD TIMER
                    Pair<String, String> newRecord = new Pair<>
                            (dateRetriever(), millisToString(mInitialTimeLength)); //Saves Data+Inital Set Time as a key-value pair.
                    saveTimerRecord(newRecord.first, newRecord.second);
                    loadTimerRecord();
                }
            };

            mTimer.start(); //Timer starts
            mTimerRunning = true;
            swapStartText();
            mStop.setVisibility(View.INVISIBLE);
            disableButtons(true);//disables buttons
            loadTimerRecord();
        }
    }

    //PAUSES TIMER
    private void pauseTimer() {
        mTimer.cancel();
        mTimerRunning = false;
        swapStartText();
        mStop.setVisibility(View.VISIBLE);
    }

    //RESETS TIMER
    private void resetTimer() {
        mTimeLeft = mInitialTimeLength;
        updateCountText();
        mStop.setVisibility(View.INVISIBLE);
        mStart.setEnabled(true);
        swapStartText();
        mStart.setVisibility(View.VISIBLE);
        disableButtons(false);
    }

    //Handles Ticking of Timer (for OnTick method)
    private void updateCountText() {
        if(mTimerRunning) {
            mTimerText.setText(millisToString(mInitialTimeLength));//(Long.toString(mInitialTimeLength));
        } else {
            mTimerText.setText(millisToString(mTimeLeft));
        }
        loadTimerRecord();
    }

    //BUTTON METHODS
    private void swapStartText() { //Switches START button text from pause to start.
        if(mStart.getText() == getResources().getString(R.string.pause)) {
            mStart.setText(getResources().getString(R.string.start));
        } else {
            mStart.setText(getResources().getString(R.string.pause));
        }
    }


    // SAVING/LOADING RECORDS

    //Saves record to SharedPreferences, Recyclerview below.
    public void saveTimerRecord(String dateString, String timeString) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("TIMER SET ON:" + dateString, " FOR: " + timeString );
        Log.d(TAG, "saveTimerRecord: " + timeString + " ON: "  + dateString);
        editor.apply();
    }

    //LOADS & UPDATES RECORDS
    public void loadTimerRecord() {
        mTimerList.clear();
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        Map<String, ?> allEntries = sharedPref.getAll();
        int i = 0;
        for(Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d(TAG, "mapValues" + entry.getKey() + ": " + entry.getValue().toString());
            mTimerList.add(entry.getKey() + ": " + entry.getValue().toString());
            Log.d(TAG, "mTIMERELAPSEDSAV" + mTimerList.get(i));
            i++;
        }
        initRecyclerView();

    }

    //CLEARS RECORDS & SAVED SHAREDPREFS
    private void clearSharedPrefs() {
        SharedPreferences settings = this.getPreferences(Context.MODE_PRIVATE);
        settings.edit().clear().apply();
        Log.d(TAG, "clearSharedPrefs: mTimerList Left:" + mTimerList.size() );
        loadTimerRecord();
    }


    //UI METHODS

    private void launchDialog(){ //Launched toast dialog, confirming if user wishes to clear sharedprefs/records.
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selection) {
                switch (selection) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Records Cleared
                        clearSharedPrefs();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //Nothing happens
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure? All Records will be Cleared").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
    }

    //RECYCLER METHOD: starts recyclerview
    private void initRecyclerView() {
        Log.d(TAG, "initTimerNames: init recyc view");
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mTimerList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    //Initiates Sound of Alarm
    private void timerDingDing() { //Plays sound as soon as alarm completes.
        MediaPlayer mp = MediaPlayer.create(this, R.raw.alarm);
        mp.start();
    }

    public void addTime(long minutesAdded) {
        mInitialTimeLength += (minutesAdded * 60000); //formats to milliseconds
        updateCountText();
        Log.d(TAG, "addTime: " + mInitialTimeLength);
    }

    private void disableButtons(boolean choice) {
        if(choice) {
            m1Button.setVisibility(View.INVISIBLE);
            m5Button.setVisibility(View.INVISIBLE);
            m10Button.setVisibility(View.INVISIBLE);
            m30Button.setVisibility(View.INVISIBLE);
            mClearButton.setVisibility(View.INVISIBLE);
        } else{
            m1Button.setVisibility(View.VISIBLE);
            m5Button.setVisibility(View.VISIBLE);
            m10Button.setVisibility(View.VISIBLE);
            m30Button.setVisibility(View.VISIBLE);
            mClearButton.setVisibility(View.VISIBLE);
        }
    }

    //FORMATTER METHODS

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
        String current = DateFormat.getDateTimeInstance().format(new Date());
        Log.d(TAG, "dateRetriever: Current Time:" + current); // + df.format(current));
        return current;
    }
}