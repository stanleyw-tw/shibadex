package app.stanw.shibadex.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.media.ExifInterface
import android.support.v4.app.NotificationCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View

class MainActivity : BaseCameraActivity(), HandleFileUpload {

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        return true
    }

    //Upload the captured bitmap to Firebase Storage
    override fun uploadImageToStorage(name: String) {
        //Collapse the sheet after yes/no was tapped
        sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        val baos = ByteArrayOutputStream()
        currentBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos)
        val data = baos.toByteArray()
        if (isNetworkAvailable()) {
            rootRef.child(name)
                    .child("${FirebaseAuth.getInstance().currentUser?.displayName?.split(" ")?.first()}${name.toLowerCase()}${System.currentTimeMillis()}.jpg")
                    .putBytes(data)
                    .addOnSuccessListener {
                        notificationManager.cancel(420)
                        toast(getString(R.string.thanks_for_feedback))
                    }
                    .addOnFailureListener {
                        notificationManager.cancel(420)
                        toast(getString(R.string.feedback_failed))
                    }
            showProgressNotification()
        } else {
            toast("No network connection, please retry later")
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    //Display a notification when the image is uploaded to firebase storage
    private fun showProgressNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_UPLOAD)
                .setContentTitle(getString(R.string.sending_feedback))
                .setContentText(getString(R.string.feedback_in_progress))
                .setSmallIcon(R.drawable.ic_cloud_upload)
                .setProgress(100, 0, true)
                .build()

        notificationManager.notify(420, notification)
    }
}