package pl.huczeq.rtspplayer.ui;

import androidx.databinding.BindingAdapter;

import com.google.android.material.textfield.TextInputLayout;

import pl.huczeq.rtspplayer.ui.views.ProgressFloatingActionButton;
import pl.huczeq.rtspplayer.util.validation.Errors;

public class MyBindingAdapter {

    @BindingAdapter(value = {"progressVisible"})
    public static void setProgressVisible(ProgressFloatingActionButton button, boolean progressVisible) {
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
                view.setError("This field is required");
                break;
            case Errors.EXPRESSION_NOT_CLOSED:
                view.setError("Nie zapomnij o zamknięciu wyrażenia");
                break;
            case Errors.EXPRESSION_OPENSIGN_AFTER_OPEN:
                view.setError("Musisz najpierw zamknąc poprzednie wyrazenie");
                break;
            case Errors.EXPRESSION_CLOSEDSIGN_WITHOUT_OPEN:
                view.setError("Musisz najpierw otworzyc wyrazenie");
                break;
            default:
                view.setError("Incorrect value");
        }
    }
}
