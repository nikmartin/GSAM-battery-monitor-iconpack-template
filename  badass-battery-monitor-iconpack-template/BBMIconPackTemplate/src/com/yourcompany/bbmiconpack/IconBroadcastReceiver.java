/*   
 * Copyright 2013 GSam Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yourcompany.bbmiconpack;

import java.util.HashMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

/**
 * You shouldn't have to modify this class at all, other
 * than it's Package name.
 * 
 * This class is responsible for receiving the intent from
 * the core Badass Battery Monitor app for showing the Notification
 * message and icon.  It will receive the intent with details 
 * such as what text to show, what the battery level is, which
 * icon drawable to use, etc.  This class then will use that information
 * to create the appropriate Notification.
 * 
 * Ideally this class wouldn't be here at all, and the core app
 * would be responsible for creating the Notification.  However, the
 * Notification API requires a resource ID (vs a Drawable or URI) to use to 
 * show the icon.  Either both apps have to run in the same process,
 * or the app with the icons needs to publish the notification.
 * 
 * @author GSam Labs
 */
public class IconBroadcastReceiver extends BroadcastReceiver {
    
    public static final String TAG = IconBroadcastReceiver.class.getName();
    public static boolean DEBUG = false;
    private static String SHOW_NOTIFICATION = null;
    private static String CANCEL_NOTIFICATION = null;
    private static final int BAT_NOTIFICATION_ID = 1; 

    HashMap<String, Boolean> mSupportsChargingIcon = new HashMap<String, Boolean>();
    
    private NotificationManager mNM = null;

    @Override
    public void onReceive(Context context, Intent intent) {
   
        if (SHOW_NOTIFICATION == null)
        {
            // These are the intent actions.  They are unique to each icon pack
            // to ensure only the iconpack that cares is ever notified.
            SHOW_NOTIFICATION = context.getPackageName() + ".SHOW_NOTIFICATION";
            CANCEL_NOTIFICATION = context.getPackageName() + ".CANCEL_NOTIFICATION";
        }
        if (mNM == null)
        {
            mNM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (DEBUG)
        {
            Log.d(TAG, intent.toString());
        }
        if (intent.getAction().equals(SHOW_NOTIFICATION))
        {
            StringBuilder sb = new StringBuilder();
            String iconToShow = intent.getStringExtra("ICON_TO_SHOW");
            sb.append("ICON_TO_SHOW: ").append(iconToShow).append("\n");
            String headText = intent.getStringExtra("HEAD_TEXT");
            sb.append("HEAD_TEXT: ").append(headText).append("\n");
            String detailText = intent.getStringExtra("DETAIL_TEXT");
            sb.append("DETAIL_TEXT: ").append(detailText).append("\n");
            String pkgName = intent.getStringExtra("ORIGINATING_PACKAGE");
            sb.append("ORIGINATING_PACKAGE: ").append(pkgName).append("\n");
            String intentClass = intent.getStringExtra("PENDING_INTENT_CLASS");
            sb.append("PENDING_INTENT_CLASS: ").append(intentClass).append("\n");
            int iconLevel = intent.getIntExtra("ICON_LEVEL", 0);
            sb.append("ICON_LEVEL: ").append(iconLevel).append("\n");
            boolean isCharging = intent.getBooleanExtra("IS_CHARGING", false);
            sb.append("IS_CHARGING: ").append(isCharging).append("\n");
            if (isCharging && (iconLevel != 0))
            {
                Uri icon = Uri.parse(iconToShow);
                if (!icon.getAuthority().equals(context.getPackageName()))
                {

                    Boolean supportsCharging = mSupportsChargingIcon.get(iconToShow.toString());
                    if (supportsCharging == null)
                    {
                        supportsCharging = false;
                        try
                        {                           
                            Resources res = context.getPackageManager().getResourcesForApplication(icon.getAuthority());
                            String path = icon.getPath();
                            int iconId = Integer.parseInt(path.substring(path.lastIndexOf('/')+1));                        
                            Drawable myDrawable = res.getDrawable(iconId);
                            if (myDrawable != null)
                            {
                                myDrawable.setLevel(iconLevel + 100);                        
                                if (myDrawable.getCurrent() != null)
                                {
                                    supportsCharging = true;
                                }
                            }
                        } catch (Exception e)
                        {
                            Log.e(TAG, "Malformed Notification Icon URI");
                        } finally
                        {
                            mSupportsChargingIcon.put(iconToShow, supportsCharging);
                        }
                    }
                    iconLevel = supportsCharging ? iconLevel + 100 : iconLevel;
                } else {
                    iconLevel += 100;
                }
            }
            long createTime = intent.getLongExtra("CREATE_TIME", 0);
            sb.append("CREATE_TIME: ").append(createTime).append("\n");
            boolean showNotification = intent.getBooleanExtra("SHOW_NOTIFICATION", true);
            sb.append("SHOW_NOTIFICATION: ").append(showNotification).append("\n");
            boolean showNotificationOnly = intent.getBooleanExtra("SHOW_NOTIFICATION_ONLY", false);
            sb.append("SHOW_NOTIFICATION_ONLY: ").append(showNotificationOnly).append("\n");

            if (DEBUG)
            {
                Log.d(TAG, "Extras: "+sb.toString());
            }
            
            if ((iconToShow == null) || (headText == null) || (detailText == null) || (pkgName == null) || (intentClass == null))
            {
                Log.e(TAG, "Malformed Broadcast Intent: "+intent);
                return;
            }     
            Intent theIntent = new Intent()
                .setComponent(new ComponentName(pkgName, intentClass))
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,  theIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // If this is jelly bean, we can set the notification priority so the icon doesn't show. If it's not, then we just don't show the notification at all!
            if (showNotification || showNotificationOnly)
            {
                Notification not = createNotificationObject(context, Uri.parse(iconToShow), headText, detailText, iconLevel, createTime, contentIntent);

                if (!showNotification  && showNotificationOnly && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN))
                {
                    not.priority = Notification.PRIORITY_MIN;
                }
                mNM.notify(BAT_NOTIFICATION_ID, not);
            } else
            {
                mNM.cancelAll();
            }
            
        } else if (intent.getAction().equals(CANCEL_NOTIFICATION))
        {
            mNM.cancelAll();
            return;
        } else
        {
            // Should never get here unless you didn't set up the intent filter correctly
            Log.e("TAG", "Malformed Broadcast Intent - Invalid Action: "+intent.getAction());
            return;
        }
    }
    
    private Notification createNotificationObject(Context context, Uri iconToShow, String headText, String detailText, int iconLevel, long createTime, PendingIntent pendingIntent)
    {      
        
        Notification notification = null;
        int iconId = R.drawable.batt_theme1;
        try
        {
            String path = iconToShow.getPath();
            path = path.substring(path.lastIndexOf('/')+1);
            iconId = Integer.parseInt(path);
        } catch (Exception e)
        {
            Log.e(TAG, "Malformed Notification Icon URI");
        }
        int sl = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        if (sl < Configuration.SCREENLAYOUT_SIZE_LARGE)
        {
            notification = new Notification(iconId, headText, 0);          
        } else 
        {
            notification = new Notification(iconId, headText, createTime); 
        }
        
        notification.setLatestEventInfo(context, headText, detailText, pendingIntent);        
        notification.flags |= Notification.FLAG_ONGOING_EVENT; 
        notification.tickerText = null;
        notification.iconLevel = iconLevel;

        // Make sure the icon we're showing in the notification entry is the right level...
        int iconResId = Resources.getSystem().getIdentifier("icon", "id", "android");
        notification.contentView.setInt(iconResId, "setImageLevel", iconLevel);
                
        return notification;
    }

}
