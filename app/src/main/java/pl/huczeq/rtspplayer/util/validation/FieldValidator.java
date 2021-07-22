package pl.huczeq.rtspplayer.util.validation;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.util.LinkedList;

import pl.huczeq.rtspplayer.BR;
import pl.huczeq.rtspplayer.util.validation.interfaces.BasicCondition;
import pl.huczeq.rtspplayer.util.validation.interfaces.FieldRule;
import pl.huczeq.rtspplayer.util.validation.interfaces.ValueProvider;

public class FieldValidator extends BaseObservable {

    private Integer error;
    private final LinkedList<FieldRule> rules;
    private final ValueProvider<String> textProvider;
    private final BasicCondition basicCondition;

    private FieldValidator(LinkedList<FieldRule> rules, ValueProvider<String> textProvider, BasicCondition basicCondition) {
        this.rules = rules;
        this.textProvider = textProvider;
        this.basicCondition = basicCondition;
    }

    public boolean isValid(boolean showError) {
        Integer errorCode = checkValidity();
        if(showError){
            if(basicCondition == null || basicCondition.allows())
                setError(errorCode);
        }
        return errorCode == null;
    }

    private Integer checkValidity() {
        String value = textProvider.provideValue();
        Integer tErrorCode;
        for(FieldRule rule : rules) {
            if ((tErrorCode = rule.checkValidity(value)) != null)
                return tErrorCode;
        }
        return null;
    }

    @Bindable
    public Integer getError() {
        return error;
    }

    public void setError(Integer error) {
        this.error = error;
        notifyPropertyChanged(BR.error);
    }

    public static Builder builder(ValueProvider<String> textProvider) {
        return new Builder(textProvider);
    }

    public static class Builder {
        private ValueProvider<String> textProvider;
        private LinkedList<FieldRule> rules = new LinkedList<>();
        private BasicCondition basicCondition;

        public Builder(ValueProvider<String> textProvider) {
            this.textProvider = textProvider;
        }

        public Builder basicCondition(BasicCondition condition) {
            this.basicCondition = condition;
            return this;
        }

        public Builder rules(FieldRule ...rules) {
            for(FieldRule rule : rules)
                this.rules.add(rule);
            return this;
        }

        public FieldValidator build() {
            return new FieldValidator(this.rules, this.textProvider, this.basicCondition);
        }
    }
}
