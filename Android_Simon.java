package com.example.miroslav.finalproject;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class Android_Simon extends ActionBarActivity {

    public SensorManager mSensorManager;
    public SensorEventListener sel_gyroscope;
    public float xGyro;
    public dogGame1 dog;
    public TableLayout tLay;
    public LinearLayout lLay;
    public RelativeLayout Rellayout;
    Button btnGreen, btnYellow, btnRed, btnBlue;
    final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android__simon);
        btnBlue = (Button) findViewById(R.id.buttonBlue);
        btnGreen = (Button) findViewById(R.id.buttonGreen);
        btnRed = (Button) findViewById(R.id.buttonRed);
        btnYellow = (Button) findViewById(R.id.buttonYellow);
        //Rellayout = (RelativeLayout) findViewById(R.id.RelLay) ;
        //tLay= (TableLayout) findViewById(R.id.tLayout);
        lLay= (LinearLayout) findViewById(R.id.lLayout);
        dog = new dogGame1(200, 800, 0);
        final ImageView doge = new ImageView(this);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        PackageManager pm = getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY)) {
            Toast.makeText(this, "no Gyroscope Sensor available", Toast.LENGTH_SHORT).show();
        } else {
            dog.draw(lLay, doge);

            sel_gyroscope = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    xGyro = event.values[1];
                    doge.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    dog.update(xGyro, doge);

                                    collisionButton(dog,btnBlue);
                                    collisionButton(dog,btnYellow);
                                    collisionButton(dog,btnGreen);
                                    collisionButton(dog,btnRed);

                                    Log.d("Sol\n","Blue " + collisionButton(dog,btnBlue) );
                                    Log.d("Sol\n","Red " + collisionButton(dog,btnRed) );
                                    Log.d("Sol\n","green " + collisionButton(dog,btnGreen) );
                                    Log.d("Sol\n","yellow " + collisionButton(dog,btnYellow) );


                                }
                            });
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }
            };
        }
        mSensorManager.registerListener(sel_gyroscope, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), 9999);
    }


    public boolean collisionButton(dogGame1 doge, Button other) {;
        return ((Math.abs(doge.xPos - other.getX()) < (int) ((other.getWidth()/ 2))) &&
                (Math.abs(doge.yPos - other.getY()) < (int) ((other.getHeight()/ 2))));
    }


    @Override
    protected void onStart() {
        super.onStart();
        //ScoreBarFragment observes Score changes in BoardFragment
        ScoreBarFragment scoreBarFragment = (ScoreBarFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_score_bar);
        BoardFragment boardFragment = (BoardFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_board);
        boardFragment.getScoreObservable().addObserver(scoreBarFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    public static class SettingsActivity extends PreferenceActivity {
        final String TAG = SettingsActivity.class.getSimpleName();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            } else {
                showPreferencesFragmentStyle(savedInstanceState);
            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        private void showPreferencesFragmentStyle(Bundle savedInstanceState) {
            if (savedInstanceState == null) {
                FragmentTransaction transaction = getFragmentManager()
                        .beginTransaction();
                android.app.Fragment fragment = new MyPreferencesFragment();
                transaction.replace(android.R.id.content, fragment);
                transaction.commit();
            }
        }

        @SuppressWarnings("deprecation")


        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public static class MyPreferencesFragment extends PreferenceFragment {
            final String TAG = MyPreferencesFragment.class.getSimpleName();

            @Override
            public void onAttach(Activity activity) {
                super.onAttach(activity);
                Log.d(TAG, "Attached to activity: " + activity.getClass().getSimpleName());
            }


        }


    }

    public static class BoardFragment extends Fragment implements  Simon.BoardHost {
        /* This BoardFragment hosts everything to do with managing the board: the game logic class Simon,
        listening to and pressing the buttons via their .setPressed() methods.
         */
        final int PLAY_DURATION_MS = 600; // How long the computer presses the buttons during playback
        final int PAUSE_DURATION_MS = 200; // How long the computer pauses between playback button presses
        private final Handler handlerPlay = new Handler();
        private String TAG = BoardFragment.class.getSimpleName();
        private List<View> simonButtons;
        private Simon simon;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            // Inflate the layout for this fragment
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean largeBoard = prefs.getBoolean(getString(R.string.key_large_board), false);
            return largeBoard ? inflater.inflate(R.layout.fragment_board_2x2, container, false) : inflater.inflate(R.layout.fragment_board_2x2, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (!initialize(getView())) {
                Log.e(TAG, "Simon is not available");
                Toast.makeText(getActivity(), getActivity().getString(R.string.initialization_error), Toast.LENGTH_SHORT).show();
            }
        }


        public void onCollission(View view){
            byte indexButton = (byte) simonButtons.indexOf(view);
            try {
                simon.Collission(indexButton);
            } catch (Exception e) {
                Log.e(TAG, "Simon unavailable", e);
            }

        }

        //"Business" logic
        void begin() {
            /* Assumes a game starting with 1 random note
            Prior to begin() method, if the user started a new game while the melody was playing,
            the melody from the old game would continue playing while the first note from the new melody
            would start playing. begin() cancels any such pending activity.
             */
            handlerPlay.removeCallbacksAndMessages(null);
            try {
                simon.begin();
            } catch (Exception e) {
                Log.e(TAG, "Simon unavailable", e);
            }
        }

        public void gameMessage(byte MESSAGE_CODE) {
          //  String[] boardHostMessages = getActivity().getResources().getStringArray(R.array.board_host_messages);
           // Toast.makeText(getActivity(), boardHostMessages[MESSAGE_CODE], Toast.LENGTH_SHORT).show();
        }

        public void gameOver() {
            /* Received signal that game is over, cancel all callbacks on this handler */
            handlerPlay.removeCallbacksAndMessages(null);
        }

        Simon.ScoreObservable getScoreObservable() {
            /* Any observers interested in the score will need to observe this observable */
            return simon.getScoreObservable();
        }

        public boolean initialize(View rootView) {
            /* BoardFragment must have initialized its root view already; could call in onCreateView AFTER
            inflating the view or in, for example, on ActivityCreated(). Passing the rootView parameter
            guarantees that the View hierarchy is available; the caller can getView() to get the rootView.
            If the parent Activity has a configuration change (e.g. orientation), we need to get all the new Views.
            We only initialize Simon if it is null because "the new" Simon stores button indexes not actual views
            and does not need to be refreshed when the Activity destroys and recreates.
            Return false if initialization fails, true otherwise
             */

            try {
                /* If we are running initialize() due to a configuration change, we need to reuse our existing
                * Simon instance which has the state of the current melody but we need to refresh our Views
                * as the previous Views were destroyed.
                * All non-Layout child Views of the supplied rootView are assumed to be melody playing buttons */
                simonButtons = getNonLayoutChildrenBFS(rootView);
                Log.d(TAG, "Simon initialized with device button count: " + simonButtons.size());
            } catch (Exception e) {
                Log.e(TAG, "Unable to create array of buttons", e);
                return false;
            }
            for (View simonButton : simonButtons) {
               // simonButton.setOnClickListener(this);


            }


            try {
                if (simon == null) this.simon = new Simon((byte) simonButtons.size(), this);
            } catch (Exception e) {
                Log.e(TAG, "Unable to instantiate Simon, e");
                return false;
            }
            return true;
        }


        public boolean play(List<Byte> playList) {
            /* In: List of byte objects (notes) to play
            Returns false if the melody won't play, true otherwise */
            int delayMultiplier = 1;
            if (playList == null) return false;
            for (final Byte buttonIndex : playList) {
                handlerPlay.postDelayed(new Runnable() {
                    public void run() {
                        playOne(buttonIndex);
                    }
                }, delayMultiplier * (PLAY_DURATION_MS + PAUSE_DURATION_MS));
                delayMultiplier++; //Need this multiplier so that played back notes are consecutive in time
            }
            return true;
        }

        public void playOne(byte buttonIndex) {

            /* convert the buttonIndex supplied by Simon to the actual View corresponding to that index.
            * Press the button (passive XML properties will make the button appear to "light up") */
            final View VW = simonButtons.get(buttonIndex);
            VW.setPressed(true); //if doesn't redraw on API10, add BTN.invalidate()!
            VW.postDelayed(new Runnable() {
                public void run() {
                    VW.setPressed(false);
                }
            }, PLAY_DURATION_MS);
        }

        private List<View> getNonLayoutChildrenBFS(View v) {
            /* Iterative method to get all View children using Breadth First Search (BFS)
            http://stackoverflow.com/questions/18668897/android-get-all-children-elements-of-a-viewgroup
            I modified it to return only the "non Layout" (ViewGroup) children in the list */
            List<View> visited = new ArrayList<View>();
            List<View> unvisited = new ArrayList<View>();
            unvisited.add(v);

            while (!unvisited.isEmpty()) {
                View child = unvisited.remove(0);
                if (!(child instanceof ViewGroup)) {
                    visited.add(child); //Original version always added the child to the visited list
                    continue;
                }
                ViewGroup group = (ViewGroup) child;
                final int childCount = group.getChildCount();
                for (int i = 0; i < childCount; i++) unvisited.add(group.getChildAt(i));
            }
            return visited;
        }
    }

    public static class ScoreBarFragment extends Fragment implements Observer {
        /* Takes care of updating the score and round when called through its ScoreBarUpdate interface.
        * In the event of a configuration change (orientation, for example)
        * this fragment maintains no member variables and does not maintain state if the host activity is destroyed.
        * The score and round persistence is external in TextViews and both need android:freezesText = "true" to
        * survive configuration changes. */
        private String TAG = ScoreBarFragment.class.getSimpleName();

        //Class: v4.app.Fragment
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_score_bar, container, false);
        }

        //Interface: observer
        public void update(Observable observable, Object o) {
            if (observable instanceof Simon.ScoreObservable) {
                byte scoreRequest = ((Simon.ScoreObservable) observable).getRequest();
                switch (scoreRequest) {
                    case Simon.ScoreObservable.RESET_SCORE:
                        resetScore();
                        break;
                    case Simon.ScoreObservable.INCREMENT_ROUND:
                        incrementRound();
                        break;
                    case Simon.ScoreObservable.INCREMENT_SCORE:
                        incrementScore();
                        break;
                    default:
                        //do nothing
                }
                Log.d(TAG, "ScoreBarFragment: received ScoreUpdate");
            }
            Log.d(TAG, "ScoreBarFragment: received unknown observable");
        }

        //"Business" logic
        private void incrementRound() {
            try {
                TextView tvwRound = (TextView) getView().findViewById(R.id.tvwRound);
                int round = Integer.parseInt(tvwRound.getText().toString());
                round++;
                tvwRound.setText(Integer.toString(round));
            } catch (Exception e) {
                Log.w(TAG, "Unable to set round", e);
            }
        }

        private void incrementScore() {
            try {
                TextView tvwScore = (TextView) getView().findViewById(R.id.tvwScore);
                if (tvwScore == null) Log.w(TAG, "tvwScore is null");
                int score = Integer.parseInt(tvwScore.getText().toString());
                score++;
                tvwScore.setText(Integer.toString(score));
            } catch (Exception e) {
                Log.w(TAG, "Unable to set score", e);
            }
        }

        private void resetScore() {
            try {
                TextView tvwScore = (TextView) getView().findViewById(R.id.tvwScore);
                tvwScore.setText(Integer.toString(0));
                TextView tvwRound = (TextView) getView().findViewById(R.id.tvwRound);
                tvwRound.setText(Integer.toString(0));
            } catch (Exception e) {
                Log.w(TAG, "resetScore(): Unable to reset score or round", e);
            }
        }
    }
}
