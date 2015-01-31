
package com.example;

import org.rochlitz.gironotifier.R;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.example.EditNameDialog.EditNameDialogListener;

public class FragmentDialogDemo extends FragmentActivity implements EditNameDialogListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        showEditDialog();
    }

    private void showEditDialog() {
        FragmentManager fm = getSupportFragmentManager();
        EditNameDialog editNameDialog = new EditNameDialog();
        editNameDialog.show(fm, "dlg_edit_name");
    }

    @Override
    public void onFinishEditDialog(String inputText) {
        Toast.makeText(this, "Hi, " + inputText, Toast.LENGTH_SHORT).show();
    }

}
