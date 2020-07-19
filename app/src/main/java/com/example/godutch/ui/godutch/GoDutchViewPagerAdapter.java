package com.example.godutch.ui.godutch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.godutch.ui.godutch.party.GoDutchPartyFragment;

public class GoDutchViewPagerAdapter extends FragmentStateAdapter {
    private final Fragment[] fragments = new Fragment[] {
            new GoDutchMainFragment(),
            new GoDutchPartyFragment()
    };

    private final String[] fragmentNames = new String[] {
            "Home",
            "Parties"
    };

    public GoDutchViewPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments[position];
    }

    @Override
    public int getItemCount() {
        return fragments.length;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public String getFragmentName(int position) {
        return this.fragmentNames[position];
    }
}
