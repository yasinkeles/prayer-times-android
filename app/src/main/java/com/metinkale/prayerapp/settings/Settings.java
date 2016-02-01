package com.metinkale.prayerapp.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager.OnActivityResultListener;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.MainIntentService;
import com.metinkale.prayerapp.Utils;
import com.metinkale.prayerapp.vakit.SoundPreference.SoundPreferenceContext;
import com.metinkale.prayerapp.vakit.WidgetService;


public class Settings extends BaseActivity implements SoundPreferenceContext
{

    private static final String DONATE_LINK_EN = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=ADFQEQS8A6ZK6";
    private static final String DONATE_LINK_DE = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=GV6N4NZ6RELTU";
    private static final String DONATE_LINK_TR = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=HPW6ZNL8GK77Y";
    private OnActivityResultListener mList;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.settings);

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Prefs.reset();
        Utils.init();
    }

    @Override
    public void setActivityResultListener(OnActivityResultListener list)
    {
        mList = list;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(mList != null && !mList.onActivityResult(requestCode, resultCode, data))
        {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    public boolean setNavBar()
    {
        return false;
    }

    public static class SettingsFragment extends PreferenceFragment implements OnPreferenceClickListener, OnPreferenceChangeListener
    {

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings);

            findPreference("mail").setOnPreferenceClickListener(this);

            findPreference("language").setOnPreferenceChangeListener(this);

            findPreference("backupRestore").setOnPreferenceClickListener(this);

            findPreference("calendarIntegration").setOnPreferenceChangeListener(this);

            findPreference("donate").setOnPreferenceClickListener(this);

            findPreference("ongoingIcon").setOnPreferenceClickListener(this);

        }

        @Override
        public boolean onPreferenceClick(Preference preference)
        {
            if(preference.getKey().equals("mail"))
            {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "metinkale38@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Namaz Vakti uygulamasi");
                String versionCode = "Undefined";
                try
                {
                    versionCode = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode + "";
                } catch(PackageManager.NameNotFoundException e)
                {
                    e.printStackTrace();
                }
                emailIntent.putExtra(Intent.EXTRA_TEXT, "===Device Information===\nManufacturer: " + Build.MANUFACTURER + "\nModel: " + Build.MODEL + "\nAndroid Version: " + Build.VERSION.RELEASE + "\nApp Version Code: " + versionCode);
                startActivity(Intent.createChooser(emailIntent, getString(R.string.sendemail)));
            } else if(preference.getKey().equals("backupRestore"))
            {
                startActivity(new Intent(this.getActivity(), BackupRestoreActivity.class));
            } else if(preference.getKey().equals("donate"))
            {
                Builder builder = new AlertDialog.Builder(this.getActivity());
                builder.setMessage(R.string.donateDlg);
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.ok, new OnClickListener()
                {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1)
                    {
                        arg0.cancel();

                        String lang = Prefs.getLanguage();
                        String url = DONATE_LINK_EN;
                        if(lang.equals("tr")) url = DONATE_LINK_TR;
                        else if(lang.equals("de")) url = DONATE_LINK_DE;

                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });
                builder.setNegativeButton(R.string.cancel, new OnClickListener()
                {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else if(preference.getKey().equals("ongoingIcon")) WidgetService.updateOngoing();
            return true;

        }

        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue)
        {
            Prefs.reset();
            if(pref.getKey().equals("language"))
            {

                Activity act = getActivity();
                act.finish();
                Intent i = new Intent(act, act.getClass());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                act.startActivity(i);
            } else if(pref.getKey().equals("calendarIntegration"))
            {
                MainIntentService.startCalendarIntegration(this.getActivity());
            }
            return true;
        }
    }

}