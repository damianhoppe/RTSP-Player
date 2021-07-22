package pl.huczeq.rtspplayer.util.validation;


import pl.huczeq.rtspplayer.util.validation.interfaces.FieldRule;
import pl.huczeq.rtspplayer.util.validation.interfaces.ValueTransform;

public abstract class RuleWithValueTransform implements ValueTransform<String>, FieldRule {

    private FieldRule rule;

    public RuleWithValueTransform(FieldRule rule) {
        this.rule = rule;
    }

    @Override
    public Integer checkValidity(String text) {
        String newValue = transform(text);
        Integer optv = onPostTransformValidity();
        if(optv != null)
            return optv;
        return rule.checkValidity(newValue);
    }

    protected Integer onPostTransformValidity() {
        return null;
    }
}