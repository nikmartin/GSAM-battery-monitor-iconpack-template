# Introduction #

As of version 2.2, GSam Battery Monitor (all editions) now supports third party icon packs.  An icon pack is an app (an installable APK) that anybody can create.  You are free to give away your icon packs or sell them as you see fit.

# Rules #

  * Your icon pack provides icons suitable for showing battery status.
  * Your icon pack shows a standard notification message with a pending intent that launches Badass Battery Monitor.  There should be no need to modify the template code.
  * You are free to distribute your icon packs as you see fit - in any Market/Appstore, and for whatever price.
  * GSam Labs reserves the right to block your icon packs usage via updates to GSam Battery Monitor if you do not follow the above rules.

# Details #

An Icon Pack is an installable APK which provides the following services for GSam Battery Monitor (BBM)

  * Provides one or more sets of battery icons (one icon for each %) and a level-list drawable for each set.  These icons get displayed as Notification messages, and also in the available Widget.
  * Acts as a content provider - telling BBM the name(s) of the icon set(s), and the resource URI to use to access the level-list drawable.
  * Receives broadcast intents from GSam Battery Monitor with details for what to show in the Notification Bar.  Unfortunately, the notification API requires that the calling app own the specified icon resource (can't use URIs), therefore the IconPack app itself must display the notification.  The core GSam Battery Monitor application will supply all the needed text to show.
  * Overlay Icon support is provided by the GSam Labs icon pack, however it has support for overlaying any icon - even in third party icon packs.  So you will need the GSam Labs icon pack installed, at which point your icons can be overlayed.

# Instructions #

  * You need to know the basics of how to develop Android apps.   We'll assume you have Eclipse set up with subversion and with your android SDK, and know how to use it:  http://developer.android.com/sdk/index.html
  * Create a new project by checking out the code from here:
svn checkout http://badass-battery-monitor-iconpack-template.googlecode.com/svn/trunk/ badass-battery-monitor-iconpack-template-read-only
  * Make sure the project builds in Eclipse with no errors
  * Now - let's modify...
    * AndroidManifest.xml:  You MUST modify the following:
      * Package name.  This needs to be unique
      * Provider Authorities.  Should be in the form: com.gsamlabs.bbm.iconpacks.MY.PACKAGE.NAME
      * Intent Filter Actions:  Again - use your package names in the form:  my.package.name.SHOW\_NOTIFICATION

  * res/values/strings.xml:  You MUST modify the following:
    * app\_name:  This is the name of your icon pack
    * icon\_name1/2/etc:  This is the name you want Badass Battery Monitor to use in the theme chooser menu.  Keep it short as there is not much real-estate on phones.  You will need one of these per icon set.

  * res/drawable/batt\_theme1.xml:   If you don't change the icon names, no changes are needed here.  If you want more than one icon set, create batt\_theme2.xml, batt\_theme3.xml, etc. and modify the icon file names appropriately.

  * Charging Animations:  If you want your icons to show an animation while charging (Ice Cream Sandwich and later), put the animated icons at level + 100 in the batt\_theme xml file.  You can either use separate icons for this, or use an animation-list and level-list.  The level-list will combine 2 images.  The animation-list will show each image in order for a specified amount of time. See res/drawable/charging\_batt\_circle\_anim.xml for an example on the animation-list.

  * res/drawable-hdpi:  This contains all of your icons.  A 72x72 pixel icon in the hdpi folder works fairly well.  You're free to provide different sizes for the different resolutions - but there generally isn't a need for this.  There should be one icon per percent.  You can provide as many icon sets as you want - just create new level-list drawables for each set.

  * src/com.yourcompany.bbmiconpack:  Refactor (rename) this to the package name you provided in the AndroidManifest.xml.  If you use the Eclipse refactor, it should take care of the underlying java file package declarations.

  * src/com.yourcompany.bbmiconpack/IconContentProvider.java:  Modify the 'ICONS\_AND\_NAMES' variable if you have more than one set of icons.  See the comments in the file for details. If you only have one icon set, and haven't change the file names, no modifications should be needed.

  * src/com.yourcompany.bbmiconpack/IconBroadcastReceiver.java:  You should NOT modify this file other than it's package name.

  * That's it - now compile, and deploy it.


# Putting it in the Market #

You're free to sell or give away your icon packs in the Android Market or Amazon Appstore (or anywhere else).  The Badass Battery Monitor 'Get More Themes' menu will take the user to the Android Market (or Amazon Appstore if they got the app from there) and will search for 'BadassBatteryMonitorTheme'.  All apps that have this in their description (Android Market) or keywords (Amazon Appstore) will show up - so if you want your icon pack to show up, make sure you include this term.