package com.lumivote.lollipop.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lumivote.lollipop.R;
import com.lumivote.lollipop.TinyDB;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HistoryFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    private List<Illness> illnesses;

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    LinearLayoutManager llm;
    RVAdapter adapter;

    public static HistoryFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        HistoryFragment fragment = new HistoryFragment();
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
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        ButterKnife.bind(this, view);
        initializeRecyclerView();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void initializeRecyclerView() {
        initializeData();
        adapter = new RVAdapter(illnesses, mPage);
        recyclerView.setAdapter(adapter);
        llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
    }

    private void initializeData() {
        illnesses = new ArrayList<>();
        TinyDB tinyDB = new TinyDB(getActivity());
        ArrayList<String> photoPaths = tinyDB.getList(getActivity().getString(R.string.photoPaths));
        ArrayList<String> photoDates = tinyDB.getList(getActivity().getString(R.string.photoDates));
        for(int i=0; i<photoPaths.size(); i++){
            illnesses.add(new Illness(photoDates.get(i), "name", photoPaths.get(i)));
        }
    }

    public static class RVAdapter extends RecyclerView.Adapter<RVAdapter.IllnessEventViewHolder> {

        List<Illness> illnesses;
        int mPage;

        RVAdapter(List<Illness> illnesses, int mPage) {
            this.illnesses = illnesses;
            this.mPage = mPage;
        }

        @Override
        public int getItemCount() {
            return illnesses.size();
        }

        @Override
        public IllnessEventViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            final View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_history, viewGroup, false);

            final IllnessEventViewHolder pvh = new IllnessEventViewHolder(v);
            pvh.setListener(new IllnessEventViewHolder.IIllnessViewHolderClicks() {
                public void onClickItem(View caller) {
                    Log.d("Clicked the illness", "Success");
                }
            });
            return pvh;
        }

        @Override
        public void onBindViewHolder(IllnessEventViewHolder illnessEventViewHolder, int i) {
            String date = illnesses.get(i).getDate();
            String photoPath = illnesses.get(i).getPhotoPath();
            String name = illnesses.get(i).getName();

            illnessEventViewHolder.illnessDate.setText("Time taken: " + date);
            illnessEventViewHolder.illnessName.setText("Illness detected: " + name);
            illnessEventViewHolder.position = illnessEventViewHolder.getAdapterPosition();

            Context context = illnessEventViewHolder.illnessPhoto.getContext();
            Log.v(photoPath, "first image");
            Picasso.with(context).load("file://" + photoPath).into(illnessEventViewHolder.illnessPhoto);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        public static class IllnessEventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            int position;
            RelativeLayout relativeLayout;
            ImageView illnessPhoto;
            TextView illnessDate;
            TextView illnessName;
            public IIllnessViewHolderClicks mListener;

            IllnessEventViewHolder(View itemView) {
                super(itemView);
                relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relative_layout);
                illnessPhoto = ButterKnife.findById(itemView, R.id.illnessPhoto);
                illnessDate = ButterKnife.findById(itemView, R.id.illnessDate);
                illnessName = ButterKnife.findById(itemView, R.id.illnessName);
            }

            private void setListener(IIllnessViewHolderClicks listener) {
                mListener = listener;
                relativeLayout.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mListener.onClickItem(v);

            }

            public interface IIllnessViewHolderClicks {
                void onClickItem(View caller);
            }
        }
    }
}
