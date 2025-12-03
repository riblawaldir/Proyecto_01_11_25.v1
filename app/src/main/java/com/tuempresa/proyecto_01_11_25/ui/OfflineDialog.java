package com.tuempresa.proyecto_01_11_25.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.tuempresa.proyecto_01_11_25.R;

/**
 * Diálogo que se muestra cuando no hay conexión a internet.
 * Informa al usuario que la app funcionará en modo offline.
 */
public class OfflineDialog extends Dialog {
    private boolean isDismissible = true;

    public OfflineDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_offline);
        
        // Hacer el diálogo no cancelable por defecto (solo se cierra cuando vuelve la conexión)
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        TextView tvMessage = findViewById(R.id.tvOfflineMessage);
        Button btnOk = findViewById(R.id.btnOfflineOk);

        if (btnOk != null) {
            btnOk.setOnClickListener(v -> {
                dismiss();
            });
        }
    }

    /**
     * Permite hacer el diálogo dismissible o no
     */
    public void setDismissible(boolean dismissible) {
        this.isDismissible = dismissible;
        setCancelable(dismissible);
        setCanceledOnTouchOutside(dismissible);
    }
}

