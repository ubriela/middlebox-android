package edu.usc.middlebox.activities;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import android.view.Gravity;
import java.util.ArrayList;
import android.widget.LinearLayout;


import edu.usc.middlebox.R;
import edu.usc.middlebox.utils.CommonUtils;
import edu.usc.middlebox.utils.CustomHttpClient;
import edu.usc.middlebox.utils.MyJsonResponse;


/**
 * Created by ubriela on 12/7/2014.
 */

public class FragmentAll extends Fragment {

    public FragmentAll() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_all, container,
                false);

        TableLayout tl = (TableLayout) view.findViewById(R.id.all_results_tlayout);

        TableLayout tlStat = (TableLayout) view.findViewById(R.id.all_stats_tlayout);

        JSONObject jObj = MainActivity.summary_all_jObj;

        if (jObj != null) {
            try {
                JSONObject allData = jObj.getJSONObject("msg");
                JSONArray results = allData.getJSONArray("data");
                TableRow tr = null;
                String exp_type = "";
                int k = 0;
                for(int i = 0; i < results.length(); i++){
                    JSONObject jsonObject = results.getJSONObject(i);
                    // Create a TableRow and give it an ID
                    int j = k % 5;
                    if (j == 0) {
                        tr = new TableRow(getActivity());
                        tr.setLayoutParams(new LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.WRAP_CONTENT));
                        tr.setGravity(Gravity.RIGHT);
                    }

                    switch (j) {
                        case 0:
                            TextView cProvider = new TextView(getActivity());
                            cProvider.setLayoutParams(new TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT));
                            cProvider.setId(i + 200);
                            cProvider.setTextColor(Color.BLACK);
                            cProvider.setText(jsonObject.getString("cellular"));
                            tr.addView(cProvider);

                        default:
//                            TextView res1 = new TextView(getActivity());
//                            res1.setLayoutParams(new TableRow.LayoutParams(
//                                    TableRow.LayoutParams.MATCH_PARENT,
//                                    TableRow.LayoutParams.WRAP_CONTENT));
//                            res1.setId(i + 200);
//                            res1.setTextColor(Color.BLACK);
//                            res1.setText(jsonObject.getString("result") + "/" + jsonObject.getString("count"));
//                            tr.addView(res1);
                            ImageView imageView = null;
                            if (exp_type.equals(jsonObject.getString("exp_type"))) {
                                continue;
                            }

                            exp_type = jsonObject.getString("exp_type");

                            if (jsonObject.getString("result").endsWith("1")) {
                                imageView = new ImageView(getActivity());
                                imageView.setImageResource(R.drawable.check_ic);
                            } else {
                                imageView = new ImageView(getActivity());
                                imageView.setImageResource(R.drawable.uncheck_ic);
                            }
                            tr.addView(imageView);
                            break;
                    }

                    if (j == 4) {

                        // Add the TableRow to the TableLayout
                        tl.addView(tr, new TableLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.WRAP_CONTENT));
                    }
                    k++;
                }


                /**
                 * Statistics
                 */
                JSONArray stats = allData.getJSONArray("stat");
                for(int i = 0; i < stats.length(); i++) {
                    JSONObject jsonObject = stats.getJSONObject(i);

                    TableRow trStat = new TableRow(getActivity());
                    trStat.setLayoutParams(new LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT));
                    trStat.setGravity(Gravity.RIGHT);

//                    TextView simOperator = new TextView(getActivity());
//                    simOperator.setLayoutParams(new TableRow.LayoutParams(
//                            TableRow.LayoutParams.MATCH_PARENT,
//                            TableRow.LayoutParams.WRAP_CONTENT));
//                    simOperator.setId(i + 400);
//                    simOperator.setTextColor(Color.BLACK);
//                    simOperator.setText(jsonObject.getString("simOperator"));
//                    trStat.addView(simOperator);

                    TextView cellular = new TextView(getActivity());
                    cellular.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));
                    cellular.setId(i + 400);
                    cellular.setTextColor(Color.BLACK);
                    cellular.setText(jsonObject.getString("cellular"));
                    trStat.addView(cellular);

                    TextView simCountryISO = new TextView(getActivity());
                    simCountryISO.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));
                    simCountryISO.setId(i + 400);
                    simCountryISO.setTextColor(Color.BLACK);
                    simCountryISO.setText(jsonObject.getString("simCountryISO"));
                    trStat.addView(simCountryISO);

                    TextView networkTypeCount = new TextView(getActivity());
                    networkTypeCount.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));
                    networkTypeCount.setId(i + 400);
                    networkTypeCount.setTextColor(Color.BLACK);
                    networkTypeCount.setText(jsonObject.getString("networkTypeCount"));
                    trStat.addView(networkTypeCount);

                    TextView deviceCountDistinct = new TextView(getActivity());
                    deviceCountDistinct.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));
                    deviceCountDistinct.setId(i + 400);
                    deviceCountDistinct.setTextColor(Color.BLACK);
                    deviceCountDistinct.setText(jsonObject.getString("deviceCountDistinct"));
                    trStat.addView(deviceCountDistinct);

                    TextView deviceCount = new TextView(getActivity());
                    deviceCount.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));
                    deviceCount.setId(i + 400);
                    deviceCount.setTextColor(Color.BLACK);
                    deviceCount.setText(jsonObject.getString("exp"));
                    trStat.addView(deviceCount);

                    // Add the TableRow to the TableLayout
                    tlStat.addView(trStat, new TableLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT));
                }
            } catch (Exception e) {

            }
        }
        return view;
    }


}

