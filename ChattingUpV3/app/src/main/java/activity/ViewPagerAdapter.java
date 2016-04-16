package activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TabHost;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created
    int userID;
    int friendID;

    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm,CharSequence mTitles[], int mNumbOfTabsumb, int userID, int friendID) {
        super(fm);

        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;
        this.userID = userID;
        this.friendID = friendID;
    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        /*switch (position){
            case(0):
                position=2;
                break;
            case(1):
                position=3;
                break;
            case(2):
                position=4;
                break;
        }*/
        Bundle b = new Bundle();
        b.putInt("userID",userID);
        b.putInt("friendID",friendID);
        Fragment tab1;
        if(position == 0) // if the position is 0 we are returning the First tab
        {
            //MessagesFragment tab1 = new MessagesFragment();
            tab1 = new MessagesFragment();
            /*tab1.setArguments(b);
            return tab1;*/
        }
        /*else if(position == 1)            // As we are having 2 tabs if the position is now 0 it must be 1 so we are returning second tab
        {
            FilesFragment tab1 = new FilesFragment();
            //Bundle b = new Bundle();
            //b.putInt("Players",position);
            //tab1.setArguments(b);
            return tab1;;
        }*/
        else if(position==1){
            //FilesFragment tab1 = new FilesFragment();
            tab1 = new FilesFragment();
            //Bundle b = new Bundle();
            //b.putInt("Players",position);
            //tab1.setArguments(b);
            /*tab1.setArguments(b);
            return tab1;*/
        }
        else{
            //FragmentDrawer tab1 = new FragmentDrawer();
            tab1 = new FragmentDrawer();
            //Bundle b = new Bundle();
            //b.putInt("Players",position);
            //tab1.setArguments(b);
            /*tab1.setArguments(b);
            return tab1;*/
        }
        tab1.setArguments(b);
        return tab1;
    }

    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return NumbOfTabs;
    }
}