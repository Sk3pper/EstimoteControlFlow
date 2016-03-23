package estimote.andrea.com.estimotedemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by Andrea on 23/03/2016.
 */
public class AlertDialogManager {
    /**
     * Function to display simple Alert Dialog
     * @param context - application context
     * @param title - alert dialog title
     * @param message - alert message
     * @param status - success/failure (used to set icon)
     *               - pass null if you don't want icon
     * */
    private String m;
    private AppCompatActivity con;
    private TextView t;
    public void showAlertDialog(final Context context, String title, String message,
                                Boolean status,AppCompatActivity c) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        this.con = c;


        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        if(status != null)
            // Setting alert dialog icon
            //alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

            // Setting OK Button
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //((AppCompatActivity)context).finish();
                    // stop executing code by return
                  /*  Intent demoAct = new Intent(((AppCompatActivity)context), DemoActivity.class);
                    ((AppCompatActivity)context).startActivity(demoAct);
                    ((AppCompatActivity)context).finish();*/
                    DemoActivity da = (DemoActivity) context;
                    da.Client();
                }
            });

        // Showing Alert Message
        alertDialog.show();
    }

    public void showAlertDialogDestroy(final Context context, String title, String message,
                                Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        if(status != null)
            // Setting alert dialog icon
            //alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

            // Setting OK Button
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //((AppCompatActivity)context).finish();
                    // stop executing code by return
                  /*  Intent demoAct = new Intent(((AppCompatActivity)context), DemoActivity.class);
                    ((AppCompatActivity)context).startActivity(demoAct);
                    ((AppCompatActivity)context).finish();*/

                }
            });

        // Showing Alert Message
        alertDialog.show();
    }
}
