package com.android.camera;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tct.camera.R;

public class AboutFragment extends Fragment {
	private static final String KEY_PAGE = "AboutFragment:Page";

	public static AboutFragment newInstance(int page) {
		AboutFragment fragment = new AboutFragment();

		fragment.mPage = page;

		return fragment;
	}

	private int mPage = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if ((savedInstanceState != null)
				&& savedInstanceState.containsKey(KEY_PAGE)) {
			mPage = savedInstanceState.getInt(KEY_PAGE);
		}
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.aboutfragment, container, false);

        TextView title = (TextView)v.findViewById(R.id.title);
        TextView secondTitle = (TextView) v.findViewById(R.id.secondTitle);

        TextView content = (TextView)v.findViewById(R.id.content);
        TextView secondContent = (TextView)v.findViewById(R.id.secondContent);
        ImageView imageView = (ImageView)v.findViewById(R.id.companyMark);
        switch (mPage) {
        case 0:
            title.setText(R.string.about_one_life_focus);
            content.setText(R.string.about_one_fast_face_sharp);
            secondTitle.setText(R.string.about_one_fdaf);
            secondTitle.setVisibility(View.VISIBLE);
            secondContent.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            break;
        case 1:
            title.setText(R.string.about_two_asd);
            content.setText(R.string.about_two_optimizes_photos);
            secondContent.setText(R.string.about_two_burst_asd);
            secondTitle.setVisibility(View.GONE);
            secondContent.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            break;
        case 2:
            title.setText(R.string.about_three_face_beauty);
            content.setText(R.string.about_three_enhance_portraits);
            secondTitle.setVisibility(View.GONE);
            secondContent.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            break;
        }

		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_PAGE, mPage);
	}
}
