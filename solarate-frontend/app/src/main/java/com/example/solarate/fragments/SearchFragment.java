package com.example.solarate.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.solarate.R;

public class SearchFragment extends Fragment {

    public SearchFragment() {
        // Diperlukan constructor kosong
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Menghubungkan class ini dengan layout fragment_search.xml
        return inflater.inflate(R.layout.fragment_search, container, false);
    }
}