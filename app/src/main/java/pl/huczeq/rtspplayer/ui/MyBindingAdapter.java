package pl.huczeq.rtspplayer.ui;

import androidx.databinding.BindingAdapter;

import com.google.android.material.textfield.TextInputLayout;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.ui.views.ProgressFloatingActionButton;
import pl.huczeq.rtspplayer.ui.views.ProgressMaterialButton;
import pl.huczeq.rtspplayer.util.validation.Errors;

public class MyBindingAdapter {

    @BindingAdapter(value = {"progressVisible"})
    public static void setProgressVisible(ProgressFloatingActionButton button, boolean progressVisible) {
        button.setProgressVisible(progressVisible);
    }

    @BindingAdapter(value = {"progressVisible"})
    public static void setProgressVisible(ProgressMaterialButton button, boolean progressVisible) {
        button.setProgressVisible(progressVisible);
    }

    @BindingAdapter(value = {"android:enabled"})
    public static void setEnabled(ProgressFloatingActionButton view, boolean enabled) {
        view.setEnabled(enabled);
    }

    @BindingAdapter("errorMessage")
    public static void setErrorMessage(TextInputLayout view, String errorMessage) {
        view.setError(errorMessage);
    }

    @BindingAdapter("errorMessage")
    public static void setErrorMessage(TextInputLayout view, Integer erroCode) {
        if(erroCode == null) {
            view.setError(null);
            return;
        }
        switch (erroCode) {
            case Errors.IS_REQUIRED:
                view.setError(view.getResources().getString(R.string.error_required));
                break;
            case Errors.EXPRESSION_NOT_CLOSED:
                view.setError(view.getResources().getString(R.string.error_expression_not_closed));
                break;
            case Errors.EXPRESSION_OPEN_SIGN_AFTER_OPEN:
                view.setError(view.getResources().getString(R.string.error_expression_open_sign_after_open));
                break;
            case Errors.EXPRESSION_CLOSED_SIGN_WITHOUT_OPEN:
                view.setError(view.getResources().getString(R.string.error_expression_closed_sign_without_open));
                break;
            default:
                view.setError(view.getResources().getString(R.string.error_incorrect_value));
        }
    }
}
