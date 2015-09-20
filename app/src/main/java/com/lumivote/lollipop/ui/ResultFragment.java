package com.lumivote.lollipop.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lumivote.lollipop.R;
import com.lumivote.lollipop.TinyDB;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ResultFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    @Bind(R.id.illnessTitle)
    TextView illnessTitle;

    @Bind(R.id.illnessPhoto)
    ImageView illnessPhoto;

    @Bind(R.id.illnessDate)
    TextView illnessDate;

    public static ResultFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        ResultFragment fragment = new ResultFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);
        ButterKnife.bind(this, view);
        TinyDB tinyDB = new TinyDB(getActivity());
        ArrayList<String> photoPaths = tinyDB.getList(getActivity().getString(R.string.photoPaths));
        ArrayList<String> photoDates = tinyDB.getList(getActivity().getString(R.string.photoDates));
        ArrayList<String> photoTags = tinyDB.getList(getActivity().getString(R.string.photoTags));
        if (photoPaths.size() > 0) {
            String path = photoPaths.get(photoPaths.size() - 1);
            String date = photoDates.get(photoDates.size() - 1);
            String tag = photoTags.get(photoTags.size() - 1);

            Picasso.with(getActivity()).load("file://" + path).into(illnessPhoto);
            illnessTitle.setText("Illness detected: " + tag);
            illnessDate.setText("Time taken: " + date);
        } else {
            illnessTitle.setText("No pictures taken yet!");
        }
        return view;
    }
}
